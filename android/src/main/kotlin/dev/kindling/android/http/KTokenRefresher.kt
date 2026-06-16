package dev.kindling.android.http

import io.ktor.client.request.HttpRequestBuilder

/**
 * Contract for automatic token refresh on 401 responses.
 *
 * Kindling provides a default implementation via [KDefaultTokenRefresher].
 * Implement this interface directly when your refresh logic requires custom
 * behaviour (different API shape, multi-server setup, custom storage, etc.).
 *
 * The implementation is passed to [KHttpConfig.tokenRefresher] and invoked
 * automatically by the internal `KTokenRefreshPlugin` on every 401 response
 * outside of [KHttpConfig.authPaths].
 *
 * Flow triggered on 401:
 * ```
 * shouldRefresh() == true
 *   └─ refresh()
 *        ├─ success → applyToken(request) → retry
 *        └─ failure → onRefreshFailed()
 * ```
 *
 * Concurrency is handled by Kindling (Mutex) — [refresh] will never be called
 * concurrently from multiple coroutines.
 *
 * Minimal custom implementation:
 * ```kotlin
 * class CynaTokenRefresher(
 *     private val session: SessionManager,
 *     private val authApi: AuthApi,   // your own API, not necessarily Kindling's
 * ) : KTokenRefresher {
 *
 *     override suspend fun shouldRefresh(): Boolean =
 *         session.refreshToken.value != null
 *
 *     override suspend fun refresh(): Boolean = runCatching {
 *         val response = authApi.refresh(session.refreshToken.value!!)
 *         session.saveTokens(response.accessToken, response.refreshToken)
 *     }.isSuccess
 *
 *     override suspend fun applyToken(request: HttpRequestBuilder) {
 *         session.token.value?.let { request.bearerAuth(it) }
 *     }
 *
 *     override suspend fun onRefreshFailed() {
 *         session.clearSession()
 *     }
 * }
 * ```
 */
interface KTokenRefresher {

    /**
     * Called before attempting a refresh.
     *
     * Return `false` to skip refresh entirely (e.g. no refresh token available).
     * Kindling will propagate the original 401 without retrying.
     */
    suspend fun shouldRefresh(): Boolean

    /**
     * Performs the token refresh.
     *
     * This is where you call your refresh endpoint — it can be on a completely
     * different base URL or API than the main [KHttpConfig.baseUrl].
     *
     * @return `true` if new tokens were successfully obtained and persisted,
     *         `false` otherwise.
     */
    suspend fun refresh(): Boolean

    /**
     * Applies the fresh credential to the request that will be retried.
     *
     * Called only when [refresh] returned `true`.
     *
     * The implementation should mirror whatever [KAuthProvider] does —
     * inject a Bearer header, an ApiKey header, or leave the request untouched
     * if the auth scheme is cookie-based (Ktor picks up the updated storage automatically).
     *
     * @param request The original failed request, ready to be mutated before retry.
     */
    suspend fun applyToken(request: HttpRequestBuilder)

    /**
     * Called when [refresh] returned `false` or threw an exception.
     *
     * Use this to clear the local session and/or navigate to the login screen.
     */
    suspend fun onRefreshFailed()
}