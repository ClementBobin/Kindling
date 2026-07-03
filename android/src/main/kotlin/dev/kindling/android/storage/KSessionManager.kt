package dev.kindling.android.storage

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Generic session manager for token-based authentication.
 *
 * Delegates persistence to a [KTokenStore] — swap the implementation freely:
 * - [KSharedPrefsTokenStore]  — cleartext (dev/low-sensitivity)
 * - [KEncryptedTokenStore]    — AES-256 encrypted (production)
 * - [KDataStoreTokenStore]    — Jetpack DataStore
 * - [KInMemoryTokenStore]     — tests / mock
 *
 * Tokens are exposed as [StateFlow]s so any observer (ViewModel, nav graph)
 * reacts automatically on login / logout / refresh.
 *
 * Works with [dev.kindling.android.http.buildKHttpClient] in two ways:
 * - **Bearer / JWT** → `KAuthProvider.Bearer { session.accessToken.value }`
 * - **Cookie**       → `KSessionCookieStorage(session)` as `cookieStorage`
 *
 * Usage:
 * ```kotlin
 * // Production (encrypted)
 * single { KSessionManager(KEncryptedTokenStore(androidContext())) }
 *
 * // Tests
 * val session = KSessionManager(KInMemoryTokenStore())
 * ```
 *
 * @param store  Token persistence backend.
 * @param scope  Coroutine scope used for the initial load. Defaults to a
 *               [SupervisorJob] + [Dispatchers.IO] scope.
 */
class KSessionManager(
    private val store: KTokenStore,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) {
    private val _accessToken  = MutableStateFlow<String?>(null)
    private val _refreshToken = MutableStateFlow<String?>(null)

    /** Current access token. `null` means unauthenticated. */
    val accessToken:  StateFlow<String?> = _accessToken.asStateFlow()

    /** Current refresh token. `null` means unauthenticated. */
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val mutex = Mutex()

    init {
        scope.launch {
            try {
                val access  = store.getAccessToken()
                val refresh = store.getRefreshToken()
                mutex.withLock {
                    _accessToken.value  = access
                    _refreshToken.value = refresh
                }
            } catch (e: Exception) {
                Log.e("KSessionManager", "Failed to load tokens from store", e)
                mutex.withLock {
                    _accessToken.value  = null
                    _refreshToken.value = null
                }
            }
        }
    }

    /**
     * Persists both tokens and updates the [StateFlow]s.
     *
     * Call after a successful login or token refresh.
     * Passing `null` for a token will remove it from the store and the in-memory state.
     */
    suspend fun saveTokens(accessToken: String?, refreshToken: String?) = mutex.withLock {
        store.saveTokens(accessToken, refreshToken)
        _accessToken.value  = accessToken
        _refreshToken.value = refreshToken
    }

    /**
     * Clears all stored tokens and resets the [StateFlow]s to `null`.
     *
     * Any observer of [accessToken] will react immediately (e.g. navigate to login).
     */
    suspend fun clearSession() = mutex.withLock {
        store.clear()
        _accessToken.value  = null
        _refreshToken.value = null
    }

    /** Returns `true` if an access token is currently held in memory. */
    fun isAuthenticated(): Boolean = _accessToken.value != null
}