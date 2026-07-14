package dev.kindling.android.helper.natif

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import androidx.annotation.RequiresApi
import android.security.keystore.KeyInfo
import javax.crypto.SecretKeyFactory

// ─────────────────────────────────────────────
//  KeystoreConfig
// ─────────────────────────────────────────────

/**
 * Décrit la configuration d'une clé dans l'Android Keystore.
 *
 * Presets :
 * - [KeystoreConfig.Default]             → AES-256 GCM, pas d'auth requise
 * - [KeystoreConfig.BiometricProtected]  → nécessite une auth biométrique à chaque usage
 *
 * Personnalisé :
 * ```kotlin
 * val config = KeystoreConfig(
 *     alias              = "my_key",
 *     requireBiometric   = false,
 *     invalidatedByBiometricEnrollment = true
 * )
 * ```
 */
data class KeystoreConfig(
    val alias: String,
    val requireBiometric: Boolean                    = false,
    val invalidatedByBiometricEnrollment: Boolean    = true,
    val keySize: Int                                  = 256
) {
    companion object {
        fun default(alias: String) = KeystoreConfig(alias = alias)

        fun biometricProtected(alias: String) = KeystoreConfig(
            alias            = alias,
            requireBiometric = true
        )
    }
}

// ─────────────────────────────────────────────
//  EncryptedData
// ─────────────────────────────────────────────

/** Résultat d'un chiffrement : données chiffrées + IV encodés en Base64. */
data class EncryptedData(val ciphertext: String, val iv: String)

// ─────────────────────────────────────────────
//  KeystoreHelper
// ─────────────────────────────────────────────

/**
 * Helper Android Keystore centralisé (AES-256-GCM).
 *
 * Aucune permission manifest requise — le Keystore est un service système.
 * Les clés ne quittent jamais le matériel sécurisé (TEE / StrongBox).
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { KeystoreHelper() }
 * ```
 *
 * ## Clés sans biométrie
 * ```kotlin
 * val config = KeystoreConfig.default("user_token")
 * val encrypted = keystoreHelper.encrypt(config, "mon_token_secret")
 * val plain     = keystoreHelper.decrypt(config, encrypted)
 * ```
 *
 * ## Clés protégées par biométrie
 * Les clés [KeystoreConfig.biometricProtected] requièrent un [Cipher] déjà
 * authentifié via `BiometricPrompt.CryptoObject` — un appel direct à
 * [encrypt]/[decrypt] lèverait une [android.security.keystore.UserNotAuthenticatedException].
 *
 * Flux recommandé :
 * ```kotlin
 * val config  = KeystoreConfig.biometricProtected("secure_key")
 * val cipher  = keystoreHelper.createEncryptCipher(config)   // avant la prompt
 *
 * // Passer cipher à BiometricPrompt :
 * biometricPrompt.authenticate(
 *     BiometricPrompt.CryptoObject(cipher),
 *     cancellationSignal,
 *     executor,
 *     object : BiometricPrompt.AuthenticationCallback() {
 *         override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
 *             val authenticatedCipher = result.cryptoObject!!.cipher!!
 *             val encrypted = keystoreHelper.encrypt(authenticatedCipher, "secret")
 *         }
 *     }
 * )
 *
 * // Déchiffrement :
 * val decryptCipher = keystoreHelper.createDecryptCipher(config, encrypted.iv)
 * biometricPrompt.authenticate(BiometricPrompt.CryptoObject(decryptCipher), ...)
 * // Dans onAuthenticationSucceeded :
 * val plain = keystoreHelper.decrypt(result.cryptoObject!!.cipher!!, encrypted)
 * ```
 */
@RequiresApi(Build.VERSION_CODES.M)
class KeystoreHelper {

    internal val keystore: KeyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

    // ── Key management ────────────────────────────────────────────────────────

    /** Génère ou retourne la clé existante pour [config]. */
    fun getOrCreateKey(config: KeystoreConfig): SecretKey {
            keystore.getKey(config.alias, null)?.let { existing ->
                    val secret = existing as SecretKey
                    val keyInfo = SecretKeyFactory.getInstance(secret.algorithm, PROVIDER)
                        .getKeySpec(secret, KeyInfo::class.java) as KeyInfo
                    require(keyInfo.isUserAuthenticationRequired == config.requireBiometric) {
                            "Key policy mismatch for alias '${config.alias}'. Use a distinct alias or rotate key material."
                        }
                    return secret
                }
        return generateKey(config)
    }

    /** Supprime la clé identifiée par [config.alias]. */
    fun deleteKey(config: KeystoreConfig) {
        if (keystore.containsAlias(config.alias)) {
            keystore.deleteEntry(config.alias)
        }
    }

    /** `true` si une clé existe pour cet alias. */
    fun hasKey(config: KeystoreConfig): Boolean =
        keystore.containsAlias(config.alias)

    // ── Cipher factories (biometric flow) ────────────────────────────────────

    /**
     * Crée un [Cipher] initialisé en mode chiffrement pour la clé [config].
     *
     * Pour les clés [KeystoreConfig.requireBiometric], ce cipher doit être passé
     * à `BiometricPrompt.CryptoObject` et authentifié avant utilisation.
     * Voir la documentation de la classe pour le flux complet.
     */
    fun createEncryptCipher(config: KeystoreConfig): Cipher {
        val key = getOrCreateKey(config)
        return Cipher.getInstance(TRANSFORMATION).also {
            it.init(Cipher.ENCRYPT_MODE, key)
        }
    }

    /**
     * Crée un [Cipher] initialisé en mode déchiffrement pour la clé [config],
     * en utilisant l'IV extrait de [ivBase64].
     *
     * Pour les clés [KeystoreConfig.requireBiometric], ce cipher doit être passé
     * à `BiometricPrompt.CryptoObject` et authentifié avant utilisation.
     */
    fun createDecryptCipher(config: KeystoreConfig, ivBase64: String): Cipher {
        val key     = keystore.getKey(config.alias, null) as? SecretKey
            ?: error("Clé introuvable pour l'alias '${config.alias}'")
        val ivBytes = Base64.decode(ivBase64, Base64.NO_WRAP)
        return Cipher.getInstance(TRANSFORMATION).also {
            it.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, ivBytes))
        }
    }

    // ── Encrypt / Decrypt (cipher pré-authentifié) ───────────────────────────

    /**
     * Chiffre [plaintext] avec un [Cipher] déjà initialisé (et authentifié si
     * la clé est biométrique).
     *
     * Obtenir le cipher via [createEncryptCipher] puis
     * `BiometricPrompt.CryptoObject` pour les clés protégées.
     */
    fun encrypt(cipher: Cipher, plaintext: String): EncryptedData {
        val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return EncryptedData(
            ciphertext = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            iv         = Base64.encodeToString(cipher.iv,   Base64.NO_WRAP)
        )
    }

    /**
     * Déchiffre [data] avec un [Cipher] déjà initialisé (et authentifié si
     * la clé est biométrique).
     *
     * Obtenir le cipher via [createDecryptCipher] puis
     * `BiometricPrompt.CryptoObject` pour les clés protégées.
     */
    fun decrypt(cipher: Cipher, data: EncryptedData): String {
        val plainBytes = cipher.doFinal(Base64.decode(data.ciphertext, Base64.NO_WRAP))
        return String(plainBytes, Charsets.UTF_8)
    }

    // ── Encrypt / Decrypt (clés sans biométrie uniquement) ───────────────────

    /**
     * Chiffre [plaintext] avec la clé identifiée par [config].
     *
     * ⚠️ Ne pas utiliser avec [KeystoreConfig.requireBiometric] = `true` :
     * utiliser [createEncryptCipher] + `BiometricPrompt.CryptoObject` à la place.
     */
    fun encrypt(config: KeystoreConfig, plaintext: String): EncryptedData {
        require(!config.requireBiometric) {
            "encrypt(config, …) ne supporte pas les clés biométriques. " +
                    "Utiliser createEncryptCipher() + BiometricPrompt.CryptoObject."
        }
        return encrypt(createEncryptCipher(config), plaintext)
    }

    /**
     * Déchiffre [data] avec la clé identifiée par [config].
     * Retourne `null` si la clé n'existe pas.
     *
     * ⚠️ Ne pas utiliser avec [KeystoreConfig.requireBiometric] = `true` :
     * utiliser [createDecryptCipher] + `BiometricPrompt.CryptoObject` à la place.
     */
    fun decrypt(config: KeystoreConfig, data: EncryptedData): String? {
        require(!config.requireBiometric) {
            "decrypt(config, …) ne supporte pas les clés biométriques. " +
                    "Utiliser createDecryptCipher() + BiometricPrompt.CryptoObject."
        }
        if (!keystore.containsAlias(config.alias)) return null
        return decrypt(createDecryptCipher(config, data.iv), data)
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun generateKey(config: KeystoreConfig): SecretKey {
        val spec = KeyGenParameterSpec.Builder(
            config.alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(config.keySize)
            .setUserAuthenticationRequired(config.requireBiometric)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setInvalidatedByBiometricEnrollment(config.invalidatedByBiometricEnrollment)
                }
            }
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
            .apply { init(spec) }
            .generateKey()
    }

    companion object {
        private const val PROVIDER       = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }
}