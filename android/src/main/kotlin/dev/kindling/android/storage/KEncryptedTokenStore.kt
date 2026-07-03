package dev.kindling.android.storage

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import dev.kindling.android.natif.EncryptedData
import dev.kindling.android.natif.KeystoreConfig
import dev.kindling.android.natif.KeystoreHelper

/**
 * [KTokenStore] backed by standard [SharedPreferences] with manual AES-GCM encryption.
 *
 * Tokens are encrypted at rest using AES-256 GCM (key stored in Android Keystore).
 * Recommended for production use on Android 6.0+ (API 23).
 *
 * This implementation replaces the deprecated EncryptedSharedPreferences
 * and uses standard platform APIs (Keystore, Cipher, SharedPreferences).
 *
 * @param context         Android [Context] used to open SharedPreferences.
 * @param prefsName       SharedPreferences file name.
 * @param accessTokenKey  Key used to store the access token.
 * @param refreshTokenKey Key used to store the refresh token.
 */
@RequiresApi(Build.VERSION_CODES.M)
class KEncryptedTokenStore(
    context: Context,
    prefsName: String            = "kindling_secure_tokens",
    private val accessTokenKey:  String = "access_token",
    private val refreshTokenKey: String = "refresh_token",
) : KTokenStore {

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val keystoreHelper = KeystoreHelper()
    private val keyConfig      = KeystoreConfig.default("kindling_token_key")

    override suspend fun getAccessToken():  String? = getEncrypted(accessTokenKey)
    override suspend fun getRefreshToken(): String? = getEncrypted(refreshTokenKey)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        saveEncrypted(accessTokenKey, accessToken)
        saveEncrypted(refreshTokenKey, refreshToken)
    }

    override suspend fun clear() {
        prefs.edit {
            remove(accessTokenKey)
            remove("${accessTokenKey}_iv")
            remove(refreshTokenKey)
            remove("${refreshTokenKey}_iv")
        }
    }

    private fun getEncrypted(key: String): String? {
        val ciphertext = prefs.getString(key, null) ?: return null
        val iv         = prefs.getString("${key}_iv", null) ?: return null
        
        return try {
            keystoreHelper.decrypt(keyConfig, EncryptedData(ciphertext, iv))
        } catch (e: Exception) {
            null
        }
    }

    private fun saveEncrypted(key: String, value: String) {
        try {
            val encrypted = keystoreHelper.encrypt(keyConfig, value)
            prefs.edit {
                putString(key, encrypted.ciphertext)
                putString("${key}_iv", encrypted.iv)
            }
        } catch (e: Exception) {
            // Failed to encrypt or save
        }
    }
}
