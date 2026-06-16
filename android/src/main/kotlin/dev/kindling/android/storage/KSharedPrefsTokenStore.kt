package dev.kindling.android.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * [KTokenStore] backed by plain [SharedPreferences].
 *
 * Tokens are stored in cleartext. Suitable for development or low-sensitivity
 * apps. For production, prefer [KEncryptedTokenStore].
 *
 * @param context         Android [Context] used to open SharedPreferences.
 * @param prefsName       SharedPreferences file name.
 * @param accessTokenKey  Key used to store the access token.
 * @param refreshTokenKey Key used to store the refresh token.
 */
class KSharedPrefsTokenStore(
    context: Context,
    prefsName: String        = "kindling_tokens",
    private val accessTokenKey:  String = "access_token",
    private val refreshTokenKey: String = "refresh_token",
) : KTokenStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

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