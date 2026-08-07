package dev.kindling.android.natif

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

// ─────────────────────────────────────────────
//  BiometricConfig
// ─────────────────────────────────────────────

/**
 * Configuration pour le dialogue biométrique.
 *
 * Note : Pour utiliser un [CryptoObject] (nécessaire à la sécurité Keystore),
 * [allowedAuthenticators] doit utiliser `Authenticators.BIOMETRIC_STRONG`.
 */
data class BiometricConfig(
    val title: String,
    val subtitle: String                = "",
    val description: String             = "",
    val negativeButtonText: String      = "Annuler",
    val allowedAuthenticators: Int      = Authenticators.BIOMETRIC_STRONG,
    val keyAlias: String                = "biometric_auth_key"
) {
    companion object {
        fun strong(
            title: String,
            subtitle: String = "",
            description: String = "",
            keyAlias: String = "biometric_auth_key"
        ) = BiometricConfig(
            title                 = title,
            subtitle              = subtitle,
            description           = description,
            negativeButtonText    = "Annuler",
            allowedAuthenticators = Authenticators.BIOMETRIC_STRONG,
            keyAlias              = keyAlias
        )
    }
}

// ─────────────────────────────────────────────
//  BiometricResult
// ─────────────────────────────────────────────

sealed class BiometricResult {
    data object Success                         : BiometricResult()
    data class  SuccessWithEncrypted(val data: EncryptedData) : BiometricResult()
    data class  SuccessWithDecrypted(val plaintext: String)   : BiometricResult()
    data class  Error(val message: String)      : BiometricResult()
    data object Failed                          : BiometricResult()
    data object Unavailable                     : BiometricResult()
    data object NoneEnrolled                    : BiometricResult()
}

// ─────────────────────────────────────────────
//  BiometricHelper
// ─────────────────────────────────────────────

/**
 * Helper biométrique réutilisant [KeystoreHelper] pour garantir
 * la sécurité des opérations via `CryptoObject`.
 */
@RequiresApi(Build.VERSION_CODES.M)
class BiometricHelper(
    context: android.content.Context,
    private val keystoreHelper: KeystoreHelper = KeystoreHelper()
) {

    internal val appContext       = context.applicationContext
    internal val biometricManager = BiometricManager.from(appContext)

    fun canAuthenticate(config: BiometricConfig): Boolean =
        biometricManager.canAuthenticate(config.allowedAuthenticators) ==
                BiometricManager.BIOMETRIC_SUCCESS

    fun availabilityStatus(config: BiometricConfig): Int =
        biometricManager.canAuthenticate(config.allowedAuthenticators)

    /**
     * Exécute une authentification biométrique sécurisée liée à un [CryptoObject].
     *
     * - Si [plaintextToEncrypt] est fourni : Chiffre la donnée après authentification biométrique.
     * - Si [dataToDecrypt] est fourni : Déchiffre la donnée après authentification biométrique.
     * - Si aucun n'est fourni : Effectue une opération de validation (jeton par défaut) pour déverrouiller la clé.
     */
    fun authenticate(
        activity: FragmentActivity,
        config: BiometricConfig,
        plaintextToEncrypt: String? = null,
        dataToDecrypt: EncryptedData? = null,
        onResult: (BiometricResult) -> Unit
    ) {
        when (val status = biometricManager.canAuthenticate(config.allowedAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> { /* proceed */ }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onResult(BiometricResult.NoneEnrolled)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                onResult(BiometricResult.Unavailable)
                return
            }
            else -> {
                onResult(BiometricResult.Error("Unknown status: $status"))
                return
            }
        }

        // 1. Initialisation du KeystoreConfig
        val keyConfig = KeystoreConfig.biometricProtected(config.keyAlias)

        // 2. Création du Cipher en réutilisant KeystoreHelper
        val cipher: Cipher = try {
            if (dataToDecrypt != null) {
                keystoreHelper.createDecryptCipher(keyConfig, dataToDecrypt.iv)
            } else {
                keystoreHelper.createEncryptCipher(keyConfig)
            }
        } catch (e: Exception) {
            onResult(BiometricResult.Error("Failed to initialize cipher: ${e.localizedMessage}"))
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        // 3. Callback biométrique consommant le CryptoObject (exigé par CodeQL)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val authenticatedCipher = result.cryptoObject?.cipher
                if (authenticatedCipher == null) {
                    onResult(BiometricResult.Error("Missing CryptoObject in result."))
                    return
                }

                try {
                    when {
                        // Operational Mode A: Déchiffrement
                        dataToDecrypt != null -> {
                            val decryptedText = keystoreHelper.decrypt(authenticatedCipher, dataToDecrypt)
                            onResult(BiometricResult.SuccessWithDecrypted(decryptedText))
                        }
                        // Operational Mode B: Chiffrement
                        plaintextToEncrypt != null -> {
                            val encryptedData = keystoreHelper.encrypt(authenticatedCipher, plaintextToEncrypt)
                            onResult(BiometricResult.SuccessWithEncrypted(encryptedData))
                        }
                        // Operational Mode C: Authentification standard (Validation cryptographique)
                        else -> {
                            keystoreHelper.encrypt(authenticatedCipher, "biometric_validation_payload")
                            onResult(BiometricResult.Success)
                        }
                    }
                } catch (e: Exception) {
                    onResult(BiometricResult.Error("Crypto operation failed: ${e.localizedMessage}"))
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(BiometricResult.Error(errString.toString()))
            }

            override fun onAuthenticationFailed() {
                onResult(BiometricResult.Failed)
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(config.title)
            .setSubtitle(config.subtitle)
            .setDescription(config.description)
            .setAllowedAuthenticators(config.allowedAuthenticators)
            .setNegativeButtonText(config.negativeButtonText)
            .build()

        // 4. Lancement avec le CryptoObject
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}
