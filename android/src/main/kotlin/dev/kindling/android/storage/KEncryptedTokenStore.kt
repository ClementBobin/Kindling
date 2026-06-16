package dev.kindling.android.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

/**
 * [KTokenStore] backed by [EncryptedSharedPreferences].
 *
 * Tokens are encrypted at rest using AES-256 GCM (key stored in Android Keystore).
 * Recommended for production use.
 *
 * Requires the Jetpack Security dependency:
 * ```kotlin
 * implementation("androidx.security:security-crypto:1.1.0-alpha06")
 * ```
 *
 * @param context         Android [Context] used to open EncryptedSharedPreferences.
 * @param prefsName       Encrypted SharedPreferences file name.
 * @param accessTokenKey  Key used to store the access token.
 * @param refreshTokenKey Key used to store the refresh token.
 */
class KEncryptedTokenStore(
    context: Context,
    prefsName: String            = "kindling_secure_tokens",
    private val accessTokenKey:  String = "access_token",
    private val refreshTokenKey: String = "refresh_token",
) : KTokenStore {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        prefsName,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override suspend fun getAccessToken():  String? = prefs.getString(accessTokenKey,  null)
    override suspend fun getRefreshToken(): String? = prefs.getString(refreshTokenKey, null)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString(accessTokenKey, accessToken)
                .putString(refreshTokenKey, refreshToken)
        }
    }

    override suspend fun clear() {
        prefs.edit {
            remove(accessTokenKey)
                .remove(refreshTokenKey)
        }
    }
}