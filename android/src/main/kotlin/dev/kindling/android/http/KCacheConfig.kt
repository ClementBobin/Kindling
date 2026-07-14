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
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cache strategy controlling when cached responses are used.
 *
 * - [NetworkFirst]  — try network, fall back to cache on failure.
 * - [CacheFirst]    — return cache if fresh, fetch network otherwise.
 * - [CacheOnly]     — always return cache; throw [KHttpException.ServerError] (504) on miss.
 * - [NetworkOnly]   — never cache (same as no cache config).
 */
enum class KCacheStrategy { NetworkFirst, CacheFirst, CacheOnly, NetworkOnly }

/**
 * Configuration for the in-memory HTTP response cache.
 *
 * Only GET requests are cached. Cache keys include the URL and Vary-style headers (Authorization/X-Api-Key).
 *
 * Internally backed by [CircularBuffer] (LRU key eviction) and [KMap] (reactive store),
 * both from `kindling-utils` — no additional dependencies required.
 *
 * @param maxAgeSeconds  How long a cached response is considered fresh.
 * @param maxEntries     Maximum number of responses kept in memory.
 *                       Oldest entries are evicted when the limit is reached.
 * @param strategy       Cache strategy. See [KCacheStrategy].
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

        val keyBuffer = CircularBuffer<String>(capacity = config.maxEntries)
        val store     = KMap<String, KCacheEntry>()
        val mutex     = Mutex()

        fun generateKey(request: HttpRequestBuilder): String {
            val url = request.url.toString()
            val auth = request.headers[HttpHeaders.Authorization] ?: ""
            val apiKey = request.headers["X-Api-Key"] ?: ""
            return "$url|$auth|$apiKey"
        }

        suspend fun getCached(key: String): KCacheEntry? = mutex.withLock {
            val entry = store.get(key) ?: return null
            val ageSeconds = (System.currentTimeMillis() - entry.timestamp) / 1000
            if (ageSeconds > config.maxAgeSeconds) {
                store.remove(key)
                keyBuffer.remove(key)
                return null
            }
            // LRU: refresh recency on read
            keyBuffer.remove(key)
            keyBuffer.add(key)
            return entry
        }

        suspend fun putCached(key: String, body: String) = mutex.withLock {
            if (store.get(key) == null && store.size >= config.maxEntries) {
                keyBuffer.oldest()?.let { store.remove(it) }
            }
            keyBuffer.remove(key)
            keyBuffer.add(key)
            store.set(key, KCacheEntry(body))
        }

        val mockEngine = MockEngine { req ->
            val auth = req.headers[HttpHeaders.Authorization] ?: ""
            val apiKey = req.headers["X-Api-Key"] ?: ""
            val key = "${req.url}|$auth|$apiKey"
            val entry = getCached(key)
            if (entry != null) {
                respond(
                    content = entry.body,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respond("Cache miss", HttpStatusCode.NotFound)
            }
        }
        val mockClient = HttpClient(mockEngine)
        client.coroutineContext[Job]?.invokeOnCompletion {
            mockClient.close()
            mockEngine.close()
        }

        suspend fun forwardToMock(request: HttpRequestBuilder): HttpClientCall {
            return mockClient.get(request.url.toString()) {
                headers.appendAll(request.headers.build())
            }.call
        }

        suspend fun Send.Sender.handleCacheFirst(request: HttpRequestBuilder, key: String, cached: KCacheEntry?): HttpClientCall {
            if (cached != null) return forwardToMock(request)
            val call = proceed(request)
            if (call.response.status.isSuccess()) {
                putCached(key, call.response.bodyAsText())
            }
            return call
        }

        suspend fun Send.Sender.handleNetworkFirst(request: HttpRequestBuilder, key: String, cached: KCacheEntry?): HttpClientCall {
            val result = runCatching { proceed(request) }
            val exception = result.exceptionOrNull()
            if (exception is CancellationException) throw exception

            val call = result.getOrNull()
            if (call != null) {
                if (call.response.status.isSuccess()) {
                    putCached(key, call.response.bodyAsText())
                }
                return call
            }
            return cached?.let { forwardToMock(request) } ?: throw exception!!
        }

        on(Send) { request ->
            if (request.method != HttpMethod.Get) return@on proceed(request)

            val key    = generateKey(request)
            val cached = getCached(key)

            when (config.strategy) {
                KCacheStrategy.NetworkOnly -> proceed(request)

                KCacheStrategy.CacheOnly -> {
                    if (cached == null) throw KHttpException.ServerError(504, "Cache miss: $key")
                    forwardToMock(request)
                }

                KCacheStrategy.CacheFirst -> handleCacheFirst(request, key, cached)

                KCacheStrategy.NetworkFirst -> handleNetworkFirst(request, key, cached)
            }
        }
    }
