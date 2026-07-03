package dev.kindling.android.http

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

/**
 * Default [KTokenRefresher] implementation provided by Kindling.
 *
 * Sends a `POST` to [refreshUrl] with `{ "refreshToken": "<token>" }` and
 * expects a `{ "accessToken": "...", "refreshToken": "..." }` response.
 *
 * Token application on retry is delegated to [authProvider] — so Bearer, ApiKey,
 * Basic, Cookie, or any custom scheme all work without extra configuration.
 *
 * If your refresh endpoint lives on a different server or expects a different
 * request/response shape, implement [KTokenRefresher] directly instead.
 *
 * @param refreshUrl        Full URL of the refresh endpoint. Can differ from
 *                          [KHttpConfig.baseUrl] (e.g. a dedicated auth server).
 * @param getRefreshToken   Returns the current refresh token.
 * @param onTokenRefreshed  Called with `(accessToken, refreshToken)` on success.
 *                          Persist the new tokens here.
 * @param onFailed          Called when the refresh request fails or returns non-200.
 *                          Clear the session here.
 * @param authProvider      The same [KAuthProvider] passed to [KHttpConfig.authProvider].
 *                          Used to re-apply the auth header on the retried request
 *                          after a successful refresh — works with any auth scheme.
 */
class KDefaultTokenRefresher(
    private val refreshUrl: String,
    private val getRefreshToken: suspend () -> String?,
    private val onTokenRefreshed: suspend (accessToken: String, refreshToken: String) -> Unit,
    private val onFailed: suspend () -> Unit,
    private val authProvider: KAuthProvider? = null,
) : KTokenRefresher {

    // Injected by KTokenRefreshPlugin after client construction
    internal lateinit var httpClient: HttpClient

    override suspend fun shouldRefresh(): Boolean =
        !getRefreshToken().isNullOrBlank()

    override suspend fun refresh(): Boolean {
        if (!::httpClient.isInitialized) {
            throw IllegalStateException(
                "KDefaultTokenRefresher: httpClient is not initialized. " +
                "Ensure this refresher is used within a KTokenRefreshPlugin (part of buildKHttpClient)."
            )
        }

        val refreshToken = getRefreshToken() ?: return false

        val response = runCatching {
            httpClient.post(refreshUrl) {
                contentType(ContentType.Application.Json)
                setBody(RefreshBody(refreshToken))
            }
        }.getOrNull() ?: return false

        if (response.status != HttpStatusCode.OK) return false

        val tokens = runCatching {
            response.body<RefreshResponse>()
        }.getOrNull() ?: return false

        onTokenRefreshed(tokens.accessToken, tokens.refreshToken)
        return true
    }

    /**
     * Re-applies the auth credential to the retried request.
     *
     * Delegates to [authProvider] so the correct scheme is used regardless
     * of auth type (Bearer, ApiKey, Basic, Cookie, Custom).
     *
     * If no [authProvider] is set (e.g. cookie-only auth where Ktor handles
     * cookies automatically via [KHttpConfig.cookieStorage]), this is a no-op —
     * the updated cookie storage is already picked up by [io.ktor.client.plugins.cookies.HttpCookies].
     */
    override suspend fun applyToken(request: HttpRequestBuilder) {
        authProvider?.apply(request)
    }

    override suspend fun onRefreshFailed() = onFailed()

    @Serializable private data class RefreshBody(val refreshToken: String)
    @Serializable private data class RefreshResponse(
        val accessToken: String,
        val refreshToken: String,
    )
}