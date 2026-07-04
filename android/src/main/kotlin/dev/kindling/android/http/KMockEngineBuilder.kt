package dev.kindling.android.http

import android.annotation.SuppressLint
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer

// ── Opt-in annotation ─────────────────────────────────────────────────────────

@SuppressLint("ExperimentalAnnotationRetention")
@RequiresOptIn(
    message = "buildKMockEngine requires 'io.ktor:ktor-client-mock' in your debugImplementation dependencies.",
    level   = RequiresOptIn.Level.ERROR,
)
annotation class KMockEngineApi

// ── KMockHandler ──────────────────────────────────────────────────────────────

/**
 * Represents a registered mock route handler.
 *
 * @param method    HTTP method (e.g. [HttpMethod.Get], [HttpMethod.Post]).
 * @param path      Route pattern. Supports `:param` segments
 *                  (e.g. `"/products/:id"`).
 * @param status    HTTP status code returned on match. Defaults to [HttpStatusCode.OK].
 * @param resolver  Suspend lambda called with:
 *                  - `params` — merged path + query params (path wins on collision)
 *                  - `body`   — raw request body string, or null
 *                  Returns `Any?` serialised to JSON by [buildKMockEngine].
 *                  Return `null` for empty body (e.g. 204 No Content).
 *
 * Example:
 * ```kotlin
 * KMockHandler(
 *     method   = HttpMethod.Get,
 *     path     = "/products/:id",
 *     resolver = { params, _ -> MyFactories.makeProduct(id = params["id"]) }
 * )
 * ```
 */
data class KMockHandler(
    val method: HttpMethod,
    val path: String,
    val status: HttpStatusCode = HttpStatusCode.OK,
    val resolver: suspend (params: Map<String, String>, body: String?) -> Any?,
)

// ── KMockRegistry ─────────────────────────────────────────────────────────────

/**
 * Singleton registry storing all [KMockHandler] entries.
 *
 * Register handlers before the [io.ktor.client.HttpClient] is built (e.g. in `Application.onCreate`
 * or your DI module). The registry is queried on every intercepted request by
 * [buildKMockEngine].
 *
 * Usage:
 * ```kotlin
 * KMockRegistry
 *     .register(KMockHandler(HttpMethod.Get, "/products") { _, _ ->
 *         MyFactories.makeMany(12) { MyFactories.makeProduct() }
 *     })
 *     .register(KMockHandler(HttpMethod.Post, "/auth/login") { _, _ ->
 *         MyFactories.makeAuthResponse()
 *     })
 * ```
 */
object KMockRegistry {

    private val handlers = mutableListOf<KMockHandler>()

    // ── Registration ──────────────────────────────────────────────────────────

    /** Registers a single [KMockHandler]. Returns `this` for chaining. */
    fun register(handler: KMockHandler): KMockRegistry {
        handlers.add(handler)
        return this
    }

    /** Registers multiple [KMockHandler]s at once. Returns `this` for chaining. */
    fun registerMany(vararg newHandlers: KMockHandler): KMockRegistry {
        handlers.addAll(newHandlers)
        return this
    }

    /** @see registerMany */
    fun registerMany(newHandlers: List<KMockHandler>): KMockRegistry {
        handlers.addAll(newHandlers)
        return this
    }

    /** Removes all registered handlers. Useful between tests. */
    fun clear(): KMockRegistry {
        handlers.clear()
        return this
    }

    // ── Resolution ────────────────────────────────────────────────────────────

    /**
     * Finds the first handler matching [method] and [path].
     *
     * @return A [Pair] of the matched [KMockHandler] and extracted path params,
     *         or `null` if no handler matches.
     */
    fun resolve(method: HttpMethod, path: String): Pair<KMockHandler, Map<String, String>>? {
        for (handler in handlers) {
            if (handler.method != method) continue
            val params = matchPattern(handler.path, path) ?: continue
            return handler to params
        }
        return null
    }

    /** Returns a human-readable list of registered routes (useful for debug logs). */
    fun listRoutes(): List<String> =
        handlers.map { "${it.method.value.padEnd(7)} ${it.path}" }

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Matches a concrete [url] against a [pattern] that may contain `:param` segments.
     *
     * @return Extracted path params, or `null` if the pattern does not match.
     *
     * Example: `matchPattern("/products/:id", "/products/abc")` → `mapOf("id" to "abc")`
     */
    private fun matchPattern(pattern: String, url: String): Map<String, String>? {
        val cleanUrl      = url.substringBefore("?")
        val patternParts  = pattern.split("/")
        val urlParts      = cleanUrl.split("/")
        if (patternParts.size != urlParts.size) return null

        val extracted = mutableMapOf<String, String>()
        for (i in patternParts.indices) {
            val p = patternParts[i]
            val u = urlParts[i]
            when {
                p.startsWith(":") -> extracted[p.drop(1)] = u
                p != u            -> return null
            }
        }
        return extracted
    }
}

// ── buildKMockEngine ──────────────────────────────────────────────────────────

/**
 * Builds a Ktor [MockEngine] backed by [KMockRegistry].
 *
 * Every outgoing request is intercepted:
 * 1. The path is matched against registered handlers (supports `:param` segments).
 * 2. Query parameters are extracted and merged with path params (path wins on collision).
 * 3. The resolver is called; its return value is serialised to JSON.
 * 4. If no handler matches, a `404` is returned with an error body.
 *
 * Requires `io.ktor:ktor-client-mock` in your `debugImplementation` dependencies.
 *
 * @param delayMs       Simulated network latency in milliseconds. Pass `0` to disable.
 * @param apiPrefix     URL prefix stripped before matching (default `"/api"`).
 *                      Set to `""` to disable stripping.
 *
 * Usage:
 * ```kotlin
 * // 1. Register handlers (once, e.g. in Application.onCreate or debug AppModule)
 * KMockRegistry
 *     .register(KMockHandler(HttpMethod.Get, "/products") { _, _ ->
 *         MyFactories.makeMany(12) { MyFactories.makeProduct() }
 *     })
 *     .register(KMockHandler(HttpMethod.Post, "/auth/login") { _, _ ->
 *         MyFactories.makeAuthResponse()
 *     })
 *
 * // 2. Pass the engine to KHttpConfig
 * @OptIn(KMockEngineApi::class)
 * val engine = if (BuildConfig.MOCK_API) buildKMockEngine() else CIO.create()
 *
 * val client = buildKHttpClient(
 *     KHttpConfig(baseUrl = BuildConfig.BASE_URL, engine = engine)
 * )
 * ```
 */
@KMockEngineApi
fun buildKMockEngine(
    delayMs: Long   = 300L,
    apiPrefix: String = "/api",
) = MockEngine { request ->
    if (delayMs > 0) delay(delayMs)

    val method = request.method
    val path   = request.url.encodedPath.removePrefix(apiPrefix)

    val queryParams: Map<String, String> = request.url.parameters
        .entries()
        .associate { (k, v) -> k to (v.firstOrNull() ?: "") }

    val body: String? = runCatching {
        (request.body as? OutgoingContent.ByteArrayContent)
            ?.bytes()
            ?.decodeToString()
    }.getOrNull()

    val match = KMockRegistry.resolve(method, path)

    if (match == null) {
        respond(
            content = """{"error":"No mock handler for ${method.value} $path"}""",
            status  = HttpStatusCode.NotFound,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    } else {
        val (handler, pathParams) = match
        val params = queryParams + pathParams

        runCatching { handler.resolver(params, body) }
            .fold(
                onSuccess = { data ->
                    respond(
                        content = data.toJsonString(),
                        status  = handler.status,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
                onFailure = { ex ->
                    val errorJson = JsonObject(mapOf("error" to JsonPrimitive(ex.message ?: "Unknown error")))
                    respond(
                        content = mockJson.encodeToString(errorJson),
                        status  = HttpStatusCode.InternalServerError,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            )
    }
}

// ── Internal serialisation ────────────────────────────────────────────────────

private val mockJson = Json {
    encodeDefaults    = true
    ignoreUnknownKeys = true
}

private fun Any?.toJsonString(): String = when (this) {
    null     -> "null"
    is Unit  -> "{}"
    is String -> mockJson.encodeToString(this)
    is List<*> -> {
        val elements: List<JsonElement> = map { item ->
            if (item == null) {
                JsonPrimitive(null as String?)
            } else {
                mockJson.encodeToJsonElement(
                    mockJson.serializersModule.serializer(item::class.java),
                    @Suppress("UNCHECKED_CAST") item
                )
            }
        }
        mockJson.encodeToString(kotlinx.serialization.builtins.ListSerializer(JsonElement.serializer()), elements)
    }
    else -> mockJson.encodeToString(
        mockJson.serializersModule.serializer(this::class.java),
        @Suppress("UNCHECKED_CAST") this
    )
}