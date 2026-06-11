package dev.kindling.android.natif

import android.content.Context
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
 * single { KeystoreHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * val config = KeystoreConfig.default("user_token")
 *
 * // Chiffrer
 * val encrypted = keystoreHelper.encrypt(config, "mon_token_secret")
 *
 * // Déchiffrer
 * val plain = keystoreHelper.decrypt(config, encrypted)
 *
 * // Supprimer la clé
 * keystoreHelper.deleteKey(config)
 * ```
 */
@RequiresApi(Build.VERSION_CODES.M)
class KeystoreHelper(context: Context) {

    internal val appContext = context.applicationContext

    internal val keystore: KeyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

    // ── Key management ────────────────────────────────────────────────────────

    /** Génère ou retourne la clé existante pour [config]. */
    fun getOrCreateKey(config: KeystoreConfig): SecretKey {
        keystore.getKey(config.alias, null)?.let { return it as SecretKey }
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

    // ── Encrypt / Decrypt ─────────────────────────────────────────────────────

    /**
     * Chiffre [plaintext] avec la clé identifiée par [config].
     * Retourne un [EncryptedData] contenant ciphertext + IV, tous deux en Base64.
     */
    fun encrypt(config: KeystoreConfig, plaintext: String): EncryptedData {
        val key    = getOrCreateKey(config)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return EncryptedData(
            ciphertext = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            iv         = Base64.encodeToString(cipher.iv,   Base64.NO_WRAP)
        )
    }

    /**
     * Déchiffre [data] avec la clé identifiée par [config].
     * Retourne le texte clair, ou `null` si la clé n'existe pas.
     */
    fun decrypt(config: KeystoreConfig, data: EncryptedData): String? {
        val key = keystore.getKey(config.alias, null) as? SecretKey ?: return null

        val cipher    = Cipher.getInstance(TRANSFORMATION)
        val ivBytes   = Base64.decode(data.iv, Base64.NO_WRAP)
        val spec      = GCMParameterSpec(GCM_TAG_LENGTH, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val plainBytes = cipher.doFinal(Base64.decode(data.ciphertext, Base64.NO_WRAP))
        return String(plainBytes, Charsets.UTF_8)
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