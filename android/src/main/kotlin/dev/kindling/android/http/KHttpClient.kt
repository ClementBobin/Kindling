package dev.kindling.android.http

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpCallValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Builds a pre-configured Ktor [HttpClient] following the Kindling convention.
 *
 * ## Features
 * - Base URL prefix on every request
 * - JSON content negotiation (lenient: ignores unknown keys, coerces values)
 * - Connect + request + socket timeouts
 * - Structured error handling: 4xx → [KHttpException.ClientError],
 *   5xx → [KHttpException.ServerError], unexpected status → [KHttpException.NotAccepted]
 * - 401 differentiation: auth endpoints = wrong credentials, others = expired session
 * - Auth header injection via [KAuthProvider] (Bearer, JWT, Basic, ApiKey, Custom)
 *   — also the correct place for global request headers (trace IDs, app version, etc.)
 * - Automatic token refresh via [KTokenRefresher], deduplicated by [dev.kindling.utils.SingleFlight]
 * - Response-side hooks via [KRequestInterceptor] (analytics, metrics)
 * - In-memory GET cache via [KCacheConfig], backed by [dev.kindling.utils.CircularBuffer]
 *   and [dev.kindling.utils.KMap]
 * - Optional cookie storage for cookie-based auth
 * - Configurable logging
 *
 * ## What is intentionally NOT in this client
 * - **Retry logic** → use [dev.kindling.utils.RetryRunner] at the repository layer
 *   for per-call retry with observable state (isLoading, attempt, error).
 * - **Rate limiting** → use [dev.kindling.utils.Throttler] or [dev.kindling.utils.KThrottle]
 *   at the ViewModel/UseCase layer for per-action throttling.
 * - **Request mutation** → use [KAuthProvider.Custom] instead of a separate interceptor.
 *
 * ## Plugin installation order (matters for 401 interception)
 * 1. ContentNegotiation  — JSON (de)serialisation
 * 2. HttpTimeout         — connect + request + socket timeouts
 * 3. Logging             — structured request/response logging
 * 4. KRequestInterceptor — response-side hooks (analytics)
 * 5. KCache              — in-memory GET cache
 * 6. KAuthProvider       — auth header injection (also for global headers)
 * 7. KTokenRefresh       — 401 intercept via Send hook + SingleFlight deduplication
 * 8. HttpCallValidator   — 4xx/5xx → KHttpException (sees final response after retry)
 * 9. HttpCookies         — optional cookie storage
 *
 * ## Usage
 * ```kotlin
 * // Standard Bearer JWT with auto-refresh and response analytics
 * val client = buildKHttpClient(
 *     KHttpConfig(
 *         baseUrl      = "https://api.example.com/",
 *         authProvider = KAuthProvider.Custom { request ->
 *             session.accessToken.value?.let { request.bearerAuth(it) }
 *             request.header("X-App-Version", BuildConfig.VERSION_NAME)
 *             request.header("X-Trace-Id", UUID.randomUUID().toString())
 *         },
 *         tokenRefresher = KDefaultTokenRefresher(
 *             refreshUrl       = "https://auth.example.com/refresh",
 *             getRefreshToken  = { session.refreshToken.value },
 *             onTokenRefreshed = { access, refresh -> session.saveTokens(access, refresh) },
 *             onFailed         = { session.clearSession() },
 *             authProvider     = authProvider,
 *         ),
 *         interceptor = KRequestInterceptor(
 *             onResponse = { analytics.track(it.status.value) },
 *         ),
 *         cacheConfig = KCacheConfig(
 *             maxAgeSeconds = 300,
 *             strategy      = KCacheStrategy.NetworkFirst,
 *         ),
 *         onSessionExpired = { session.clearSession() },
 *         onClientError    = { code, msg -> toast("Error $code: $msg") },
 *         onServerError    = { code, msg -> toast("Server $code: $msg") },
 *     )
 * )
 *
 * // Repository layer: retry with RetryRunner
 * class ProductRepository(private val api: ProductAPI) {
 *     private val retryRunner = RetryRunner<List<ProductDto>>(
 *         scope         = CoroutineScope(SupervisorJob() + Dispatchers.IO),
 *         retries       = 3,
 *         delay         = 500.milliseconds,
 *         backoffFactor = 2.0,
 *     )
 *     suspend fun getProducts() = retryRunner.run { api.getProducts() }
 *     val isLoading = retryRunner.isLoading
 * }
 *
 * // ViewModel layer: throttle user actions with Throttler
 * class SearchViewModel : ViewModel() {
 *     private val throttler = KThrottle<String>(viewModelScope, 500.milliseconds) { query ->
 *         repository.search(query)
 *     }
 *     fun onSearchInput(query: String) = throttler.emit(query)
 * }
 * ```
 */
fun buildKHttpClient(config: KHttpConfig): HttpClient {
    val engineBlock: (HttpClientEngine?) -> HttpClient = { engine ->
        if (engine != null) HttpClient(engine) { configure(config) }
        else HttpClient { configure(config) }
    }
    return engineBlock(config.engine)
}

private fun HttpClientConfig<*>.configure(config: KHttpConfig) {

    defaultRequest { url(config.baseUrl) }

    // 1. JSON (de)serialisation
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }

    // 2. Timeouts
    install(HttpTimeout) {
        connectTimeoutMillis = config.connectTimeoutMs
        requestTimeoutMillis = config.requestTimeoutMs
        socketTimeoutMillis  = config.requestTimeoutMs
    }

    // 3. Logging
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                println("[KHttp] $message")
            }
        }
        level = config.logLevel
        sanitizeHeader(HttpHeaders.Authorization) { true }
        sanitizeHeader(HttpHeaders.Cookie) { true }
        sanitizeHeader(HttpHeaders.SetCookie) { true }
    }

    // 4. Response-side hooks (analytics, metrics)
    config.interceptor?.let { install(createInterceptorPlugin(it)) }

    // 5. In-memory GET cache (CircularBuffer + KMap)
    config.cacheConfig?.let { install(createCachePlugin(it)) }

    // 6. Auth header injection — KAuthProvider.Custom also handles global headers
    config.authProvider?.let { provider ->
        install(createClientPlugin("KAuthProvider") {
            onRequest { request, _ -> provider.apply(request) }
        })
    }

    // 7. Token refresh — intercepts 401 via Send hook, before HttpCallValidator.
    //    Concurrency handled by SingleFlight from kindling-utils:
    //    multiple simultaneous 401s trigger exactly one refresh call.
    config.tokenRefresher?.let {
        install(createTokenRefreshPlugin(it, config.authPaths))
    }

    // 8. Error validation — only sees the final response after refresh retry.
    //    401 reached here means either no tokenRefresher configured, or refresh failed.
    install(HttpCallValidator) {
        validateResponse { response ->
            when (val status = response.status.value) {
                in 200..299 -> Unit
                401 -> {
                    val path       = response.call.request.url.encodedPath
                    val isAuthPath = config.authPaths.any { path.endsWith(it) }
                    val msg        = response.parseErrorMessage()
                    if (!isAuthPath) config.onSessionExpired?.invoke()
                    config.onClientError?.invoke(status, msg)
                    throw KHttpException.ClientError(status, msg)
                }
                in 400..499 -> {
                    val msg = response.parseErrorMessage()
                    config.onClientError?.invoke(status, msg)
                    throw KHttpException.ClientError(status, msg)
                }
                in 500..599 -> {
                    val msg = response.parseErrorMessage()
                    config.onServerError?.invoke(status, msg)
                    throw KHttpException.ServerError(status, msg)
                }
            }
        }

        handleResponseExceptionWithRequest { exception, _ ->
            if (exception is KHttpException) return@handleResponseExceptionWithRequest
            config.onServerError?.invoke(-1, exception.message ?: "Network error")
        }
    }

    // 9. Cookie storage
    config.cookieStorage?.let { storage ->
        install(HttpCookies) { this.storage = storage }
    }
}

// ── Extension helpers ─────────────────────────────────────────────────────────

/** Serialises [body] as JSON and sets `Content-Type: application/json`. */
inline fun <reified T> HttpRequestBuilder.setBodyJson(body: T) {
    contentType(ContentType.Application.Json)
    setBody(body)
}

/**
 * Asserts the response status is one of [codes].
 *
 * @throws KHttpException.NotAccepted if the status does not match.
 * @return The same [HttpResponse] for chaining.
 */
fun HttpResponse.accept(vararg codes: HttpStatusCode): HttpResponse = apply {
    if (status !in codes)
        throw KHttpException.NotAccepted(
            "Unexpected status $status. Expected: ${codes.joinToString()}"
        )
}

// ── Internal ──────────────────────────────────────────────────────────────────

/**
 * Attempts to parse an error message from the response body.
 *
 * Resolution order:
 * 1. JSON field `message`
 * 2. JSON field `error`
 * 3. Raw body text (truncated to 200 chars)
 * 4. `"No details provided"` if all else fails
 */
private suspend fun HttpResponse.parseErrorMessage(): String =
    runCatching {
        val map = body<Map<String, String?>>()
        map["message"] ?: map["error"] ?: bodyAsText().take(200)
    }.recoverCatching {
        bodyAsText().take(200)
    }.getOrDefault("No details provided")
