package dev.kindling.android.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * [KTokenStore] backed by Jetpack [DataStore].
 *
 * Requires the DataStore dependency:
 * ```kotlin
 * implementation("androidx.datastore:datastore-preferences:1.1.1")
 * ```
 *
 * Usage:
 * ```kotlin
 * val dataStore = context.createDataStore(name = "kindling_tokens")
 *
 * val session = KSessionManager(
 *     store = KDataStoreTokenStore(dataStore)
 * )
 * ```
 *
 * @param dataStore       Jetpack [DataStore] instance.
 * @param accessTokenKey  Preferences key name for the access token.
 * @param refreshTokenKey Preferences key name for the refresh token.
 */
class KDataStoreTokenStore(
    private val dataStore: DataStore<Preferences>,
    accessTokenKey:  String = "access_token",
    refreshTokenKey: String = "refresh_token",
) : KTokenStore {

    private val keyAccess  = stringPreferencesKey(accessTokenKey)
    private val keyRefresh = stringPreferencesKey(refreshTokenKey)

    override suspend fun getAccessToken(): String? =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { it[keyAccess] }
            .firstOrNull()

    override suspend fun getRefreshToken(): String? =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { it[keyRefresh] }
            .firstOrNull()

    override suspend fun saveTokens(accessToken: String?, refreshToken: String?) {
        dataStore.edit {
            if (accessToken != null) it[keyAccess] = accessToken else it.remove(keyAccess)
            if (refreshToken != null) it[keyRefresh] = refreshToken else it.remove(keyRefresh)
        }
    }

    override suspend fun clear() {
        dataStore.edit {
            it.remove(keyAccess)
            it.remove(keyRefresh)
        }
    }
}