package dev.kindling.android.http

import dev.kindling.utils.CircularBuffer
import dev.kindling.utils.KMap
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/**
 * Cache strategy controlling when cached responses are used.
 *
 * - [NetworkFirst]  — try network, fall back to cache on failure.
 * - [CacheFirst]    — return cache if fresh, fetch network otherwise.
 * - [CacheOnly]     — always return cache; throw [KHttpException.ClientError] (504) on miss.
 * - [NetworkOnly]   — never cache (same as no cache config).
 */
enum class KCacheStrategy { NetworkFirst, CacheFirst, CacheOnly, NetworkOnly }

/**
 * Configuration for the in-memory HTTP response cache.
 *
 * Only GET requests are cached. Cache keys are the full URL including query parameters.
 *
 * Internally backed by [CircularBuffer] (LRU key eviction) and [KMap] (reactive store),
 * both from `kindling-utils` — no additional dependencies required.
 *
 * @param maxAgeSeconds  How long a cached response is considered fresh.
 * @param maxEntries     Maximum number of responses kept in memory.
 *                       Oldest entries are evicted when the limit is reached.
 * @param strategy       Cache strategy. See [KCacheStrategy].
 *
 * Usage:
 * ```kotlin
 * KHttpConfig(
 *     cacheConfig = KCacheConfig(
 *         maxAgeSeconds = 300,
 *         maxEntries    = 50,
 *         strategy      = KCacheStrategy.NetworkFirst,
 *     )
 * )
 * ```
 */
data class KCacheConfig(
    val maxAgeSeconds: Long           = 300L,
    val maxEntries:    Int            = 100,
    val strategy:      KCacheStrategy = KCacheStrategy.NetworkFirst,
) {
    init {
        require(maxAgeSeconds > 0) { "maxAgeSeconds must be greater than 0" }
        require(maxEntries > 0) { "maxEntries must be greater than 0" }
    }
}

internal data class KCacheEntry(
    val body:      String,
    val timestamp: Long = System.currentTimeMillis(),
)

internal fun createCachePlugin(config: KCacheConfig) =
    createClientPlugin("KCache") {

        // CircularBuffer tracks insertion order for LRU eviction
        val keyBuffer = CircularBuffer<String>(capacity = config.maxEntries)
        // KMap is the actual store: URL → CacheEntry
        val store     = KMap<String, KCacheEntry>()

        fun getCached(key: String): KCacheEntry? {
            val entry = store.get(key) ?: return null
            val ageSeconds = (System.currentTimeMillis() - entry.timestamp) / 1000
            if (ageSeconds > config.maxAgeSeconds) {
                store.remove(key)
                return null
            }
            return entry
        }

        suspend fun putCached(key: String, body: String) {
            if (store.size >= config.maxEntries) {
                // Evict the oldest key tracked by the buffer
                keyBuffer.oldest()?.let { store.remove(it) }
            }
            keyBuffer.add(key)
            store.set(key, KCacheEntry(body))
        }

        suspend fun KCacheEntry.toCall(request: HttpRequestBuilder): HttpClientCall {
            val mockEngine = MockEngine {
                respond(
                    content = body,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            val mockClient = HttpClient(mockEngine)
            return mockClient.get(request.url.buildString()).call
        }

        on(Send) { request ->
            if (request.method != HttpMethod.Get) return@on proceed(request)

            val key    = request.url.buildString()
            val cached = getCached(key)

            when (config.strategy) {
                KCacheStrategy.NetworkOnly -> proceed(request)

                KCacheStrategy.CacheOnly -> {
                    val entry = cached ?: throw KHttpException.ClientError(504, "Cache miss: $key")
                    entry.toCall(request)
                }

                KCacheStrategy.CacheFirst -> {
                    if (cached != null) return@on cached.toCall(request)
                    val call = proceed(request)
                    putCached(key, call.response.bodyAsText())
                    call
                }

                KCacheStrategy.NetworkFirst -> {
                    val call = runCatching { proceed(request) }.getOrNull()
                    if (call != null) {
                        putCached(key, call.response.bodyAsText())
                        call
                    } else if (cached != null) {
                        cached.toCall(request)
                    } else {
                        // Fallback to original network call to propagate error
                        proceed(request)
                    }
                }
            }
        }
    }
