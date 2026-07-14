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
import java.util.Collections

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
 * @param path      Route pattern. Supports `:param` segments (e.g. `"/products/:id"`).
 * @param status    HTTP status code returned on match. Defaults to [HttpStatusCode.OK].
 * @param resolver  Suspend lambda called with merged params and body.
 *                  Returns `Any?` serialised to JSON.
 */
data class KMockHandler(
    val method: HttpMethod,
    val path: String,
    val status: HttpStatusCode = HttpStatusCode.OK,
    val resolver: suspend (params: Map<String, String>, body: String?) -> Any?,
)

/**
 * Reified helper to create a [KMockHandler] that captures the return type's serializer.
 * This avoids reflective lookup and correctly handles generic types in the mock response.
 */
@KMockEngineApi
inline fun <reified T> kMockHandler(
    method: HttpMethod,
    path: String,
    status: HttpStatusCode = HttpStatusCode.OK,
    noinline resolver: suspend (params: Map<String, String>, body: String?) -> T,
) = KMockHandler(method, path, status) { params, body ->
    val result = resolver(params, body)
    if (result == null || result is Unit) result
    else mockJson.encodeToJsonElement(mockJson.serializersModule.serializer<T>(), result)
}

// ── KMockRegistry ─────────────────────────────────────────────────────────────

/**
 * Thread-safe singleton registry storing all [KMockHandler] entries.
 */
object KMockRegistry {

    private val handlers = Collections.synchronizedList(mutableListOf<KMockHandler>())

    /** Registers a single [KMockHandler]. */
    fun register(handler: KMockHandler): KMockRegistry {
        handlers.add(handler)
        return this
    }

    /** Registers multiple [KMockHandler]s at once. */
    fun registerMany(vararg newHandlers: KMockHandler): KMockRegistry {
        handlers.addAll(newHandlers)
        return this
    }

    /** Registers multiple [KMockHandler]s at once. */
    fun registerMany(newHandlers: List<KMockHandler>): KMockRegistry {
        handlers.addAll(newHandlers)
        return this
    }

    /** Removes all registered handlers. */
    fun clear(): KMockRegistry {
        handlers.clear()
        return this
    }

    /**
     * Finds the first handler matching [method] and [path].
     * Matches against a stable snapshot of the handlers list.
     */
    fun resolve(method: HttpMethod, path: String): Pair<KMockHandler, Map<String, String>>? {
        val snapshot = synchronized(handlers) { handlers.toList() }
        for (handler in snapshot) {
            if (handler.method != method) continue
            val params = matchPattern(handler.path, path) ?: continue
            return handler to params
        }
        return null
    }

    /** Returns a list of registered routes for debugging. */
    fun listRoutes(): List<String> = synchronized(handlers) {
        handlers.map { "${it.method.value.padEnd(7)} ${it.path}" }
    }

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

val mockJson = Json {
    encodeDefaults    = true
    ignoreUnknownKeys = true
}

private fun Any?.toJsonString(): String = when (this) {
    null -> "null"
    is Unit -> "{}"
    is String -> mockJson.encodeToString(this)
    is JsonElement -> mockJson.encodeToString(this)
    else -> {
        // Fallback for types not wrapped by kMockHandler helper.
        // Still uses reflective lookup but kMockHandler is the preferred path.
        @Suppress("UNCHECKED_CAST")
        val serializer = mockJson.serializersModule.serializer(this::class.java)
        mockJson.encodeToString(serializer, this)
    }
}
