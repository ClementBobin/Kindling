package dev.kindling.android.http

import dev.kindling.utils.SingleFlight
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CancellationException

private val RefreshRetryKey = AttributeKey<Boolean>("KTokenRefreshRetry")

/**
 * Ktor client plugin that intercepts 401 responses on protected routes and
 * automatically attempts a token refresh before retrying the original request.
 *
 * Concurrency is handled by [SingleFlight] from `kindling-utils`:
 * multiple simultaneous 401s trigger exactly one refresh call — all waiters
 * share the result rather than stampeding the refresh endpoint.
 *
 * Flow triggered on 401 outside [authPaths]:
 * ```
 * SingleFlight.get {
 *     refresher.shouldRefresh() == false → return false
 *     refresher.refresh()
 *         ├─ true  → tokens saved → return true
 *         └─ false → refresher.onRefreshFailed() → return false
 * }
 * ├─ true  → applyToken(request) → retry
 * └─ false → propagate original 401
 * ```
 */
internal fun createTokenRefreshPlugin(
    refresher: KTokenRefresher,
    authPaths: List<String>,
) = createClientPlugin("KTokenRefresh") {

    // SingleFlight from kindling-utils deduplicates concurrent refresh calls.
    // Declared inside the plugin factory so it's scoped to this specific HttpClient instance.
    val refreshFlight = SingleFlight<Boolean>()

    if (refresher is KDefaultTokenRefresher) {
        refresher.httpClient = client
    }

    on(Send) { request ->
        val originalCall = proceed(request)

        if (originalCall.response.status != HttpStatusCode.Unauthorized) return@on originalCall
        
        // Avoid infinite refresh loops if the retried request also returns 401
        if (request.attributes.contains(RefreshRetryKey)) return@on originalCall
        
        val path = originalCall.response.request.url.encodedPath
        if (authPaths.any { path.endsWith(it) }) return@on originalCall
        if (!refresher.shouldRefresh()) return@on originalCall

        // SingleFlight: only one coroutine refreshes; others await the shared result
        val refreshed = try {
            refreshFlight.get {
                runCatching { refresher.refresh() }
                    .onFailure { if (it is CancellationException) throw it }
                    .getOrElse { false }
                    .also { success -> if (!success) refresher.onRefreshFailed() }
            }
        } catch (e: CancellationException) {
            throw e
        }

        if (!refreshed) return@on originalCall

        val retryRequest = HttpRequestBuilder().takeFrom(request)
        retryRequest.attributes.put(RefreshRetryKey, true)
        refresher.applyToken(retryRequest)
        proceed(retryRequest)
    }
}
