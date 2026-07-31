package dev.kindling.android.http

import dev.kindling.utils.SingleFlight
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.discardRemaining
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CancellationException

/**
 * Key used to mark requests that should bypass token refresh handling
 * (e.g. the refresh request itself, or retries after a successful refresh).
 */
internal val RefreshRetryKey = AttributeKey<Boolean>("KTokenRefreshRetry")

/**
 * Ktor client plugin that intercepts 401 responses on protected routes and
 * automatically attempts a token refresh before retrying the original request.
 *
 * Concurrency is handled by [SingleFlight] from `kindling-utils`:
 * multiple simultaneous 401s trigger exactly one refresh call — all waiters
 * share the result rather than stampeding the refresh endpoint.
 */
internal fun createTokenRefreshPlugin(
    refresher: KTokenRefresher,
    authPaths: List<String>,
) = createClientPlugin("KTokenRefresh") {

    val refreshFlight = SingleFlight<Boolean>()

    if (refresher is KDefaultTokenRefresher) {
        refresher.httpClient = client
    }

    on(Send) { request ->
        val originalCall = proceed(request)

        if (originalCall.response.status != HttpStatusCode.Unauthorized) return@on originalCall
        
        // Avoid infinite refresh loops
        if (request.attributes.contains(RefreshRetryKey)) return@on originalCall
        
        val path = originalCall.response.request.url.encodedPath
        if (authPaths.any { path.endsWith(it) }) return@on originalCall
        if (!refresher.shouldRefresh()) return@on originalCall

        // SingleFlight ensures only one concurrent refresh call
        val refreshed = refreshFlight.get {
            runCatching { refresher.refresh() }
                .onFailure { if (it is CancellationException) throw it }
                .getOrElse { false }
                .also { success -> if (!success) refresher.onRefreshFailed() }
        }

        if (!refreshed) return@on originalCall

        // Safely discard the body of the 401 response before retrying
        originalCall.response.discardRemaining()

        val retryRequest = HttpRequestBuilder().takeFrom(request)
        retryRequest.attributes.put(RefreshRetryKey, true)

        // Clear stale Authorization header before applying the new token
        retryRequest.headers.remove(HttpHeaders.Authorization)
        refresher.applyToken(retryRequest)

        proceed(retryRequest)
    }
}
