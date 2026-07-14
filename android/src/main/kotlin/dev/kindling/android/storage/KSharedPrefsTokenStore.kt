package dev.kindling.android.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    private val context: Context,
    private val prefsName: String        = "kindling_tokens",
    private val accessTokenKey:  String = "access_token",
    private val refreshTokenKey: String = "refresh_token",
) : KTokenStore {

    private var _prefs: SharedPreferences? = null

    private suspend fun getPrefs(): SharedPreferences = withContext(Dispatchers.IO) {
        _prefs ?: context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).also { _prefs = it }
    }

    override suspend fun getAccessToken():  String? = withContext(Dispatchers.IO) {
        getPrefs().getString(accessTokenKey,  null)
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        getPrefs().getString(refreshTokenKey, null)
    }

    override suspend fun saveTokens(accessToken: String?, refreshToken: String?) = withContext(Dispatchers.IO) {
        getPrefs().edit {
            if (accessToken != null) putString(accessTokenKey, accessToken) else remove(accessTokenKey)
            if (refreshToken != null) putString(refreshTokenKey, refreshToken) else remove(refreshTokenKey)
        }
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        getPrefs().edit {
            remove(accessTokenKey)
            remove(refreshTokenKey)
        }
    }
}