package dev.kindling.android.storage

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Generic session manager for token-based authentication.
 */
class KSessionManager(
    private val store: KTokenStore,
    providedScope: CoroutineScope? = null,
) {
    private val isInternalScope = providedScope == null
    private val scope = providedScope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _accessToken  = MutableStateFlow<String?>(null)
    private val _refreshToken = MutableStateFlow<String?>(null)
    private val _isReady      = MutableStateFlow(false)

    /** Current access token. `null` means unauthenticated. */
    val accessToken:  StateFlow<String?> = _accessToken.asStateFlow()

    /** Current refresh token. `null` means unauthenticated. */
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    /** `true` once tokens have been hydrated from the store. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val mutex = Mutex()

    init {
        scope.launch {
            try {
                mutex.withLock {
                    val access  = store.getAccessToken()
                    val refresh = store.getRefreshToken()
                    _accessToken.value  = access
                    _refreshToken.value = refresh
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("KSessionManager", "Failed to load tokens from store", e)
            } finally {
                _isReady.value = true
            }
        }
    }

    /** Waits until the initial token load is complete. */
    suspend fun awaitReady() = isReady.first { it }

    private suspend fun performSaveTokens(accessToken: String?, refreshToken: String?) {
        // Save to persistent store first; if it throws (e.g. encryption failure),
        // we don't update the in-memory state.
        store.saveTokens(accessToken, refreshToken)
        _accessToken.value  = accessToken
        _refreshToken.value = refreshToken
    }

    /**
     * Persists both tokens and updates the [StateFlow]s.
     */
    suspend fun saveTokens(accessToken: String?, refreshToken: String?) = mutex.withLock {
        performSaveTokens(accessToken, refreshToken)
    }

    /**
     * Updates only the access token while keeping the refresh token consistent.
     */
    suspend fun updateAccessToken(token: String?) = mutex.withLock {
        performSaveTokens(token, _refreshToken.value)
    }

    /**
     * Updates only the refresh token while keeping the access token consistent.
     */
    suspend fun updateRefreshToken(token: String?) = mutex.withLock {
        performSaveTokens(_accessToken.value, token)
    }

    /**
     * Clears all stored tokens and resets the [StateFlow]s to `null`.
     */
    suspend fun clearSession() = mutex.withLock {
        store.clear()
        _accessToken.value  = null
        _refreshToken.value = null
    }

    /** Returns `true` if an access token is currently held in memory. */
    fun isAuthenticated(): Boolean {
        if (!_isReady.value) {
            Log.w("KSessionManager", "isAuthenticated() called before hydration; result may be stale.")
        }
        return _accessToken.value != null
    }

    /**
     * Cancels the [CoroutineScope] used by this manager, if it was created internally.
     */
    fun close() {
        if (isInternalScope) {
            scope.cancel()
        }
    }
}
