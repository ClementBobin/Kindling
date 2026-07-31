package dev.kindling.android.http

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponse

/**
 * Hook for observing every response globally.
 *
 * **Request-side mutations** (headers, auth, trace IDs) belong in [KAuthProvider.Custom],
 * not here — keeping a single, ordered injection point before the request is sent.
 *
 * Use [onResponse] for side effects that don't alter the request:
 * analytics, structured logging, response-level metrics, etc.
 *
 * Usage:
 * ```kotlin
 * KHttpConfig(
 *     interceptor = KRequestInterceptor(
 *         onResponse = { response ->
 *             analytics.track(
 *                 "http_response",
 *                 mapOf(
 *                     "status" to response.status.value,
 *                     "path"   to response.request.url.encodedPath,
 *                 )
 *             )
 *         },
 *     )
 * )
 * ```
 *
 * For global request headers, use [KAuthProvider.Custom] instead:
 * ```kotlin
 * KHttpConfig(
 *     authProvider = KAuthProvider.Custom { request ->
 *         request.header("X-App-Version", BuildConfig.VERSION_NAME)
 *         request.header("X-Trace-Id", UUID.randomUUID().toString())
 *         session.accessToken.value?.let { request.bearerAuth(it) }
 *     }
 * )
 * ```
 */
data class KRequestInterceptor(
    val onResponse: (suspend (HttpResponse) -> Unit)? = null,
)

internal fun createInterceptorPlugin(interceptor: KRequestInterceptor) =
    createClientPlugin("KRequestInterceptor") {
        interceptor.onResponse?.let { hook ->
            onResponse { response -> hook(response) }
        }
    }