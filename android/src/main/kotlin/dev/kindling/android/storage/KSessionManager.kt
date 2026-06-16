package dev.kindling.android.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    init {
        scope.launch {
            _accessToken.value  = store.getAccessToken()
            _refreshToken.value = store.getRefreshToken()
        }
    }

    /**
     * Persists both tokens and updates the [StateFlow]s.
     *
     * Call after a successful login or token refresh.
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        store.saveTokens(accessToken, refreshToken)
        _accessToken.value  = accessToken
        _refreshToken.value = refreshToken
    }

    /**
     * Clears all stored tokens and resets the [StateFlow]s to `null`.
     *
     * Any observer of [accessToken] will react immediately (e.g. navigate to login).
     */
    suspend fun clearSession() {
        store.clear()
        _accessToken.value  = null
        _refreshToken.value = null
    }

    /** Returns `true` if an access token is currently held in memory. */
    fun isAuthenticated(): Boolean = _accessToken.value != null
}