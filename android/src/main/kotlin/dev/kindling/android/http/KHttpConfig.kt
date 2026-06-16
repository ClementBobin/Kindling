package dev.kindling.android.http

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.logging.LogLevel

/**
 * Configuration for [buildKHttpClient].
 *
 * @param baseUrl          Base URL prepended to every request (must end with `/`).
 * @param engine           Ktor engine. Defaults to platform default when null.
 * @param connectTimeoutMs TCP connect timeout in milliseconds.
 * @param requestTimeoutMs Full request timeout in milliseconds.
 * @param logLevel         Ktor [LogLevel]. Use [LogLevel.NONE] in production.
 * @param onClientError    Called on 4xx responses with status code + parsed message.
 * @param onServerError    Called on 5xx responses with status code + parsed message.
 * @param onSessionExpired Called on 401 outside [authPaths]. Clear your session here.
 * @param authPaths        Path suffixes treated as auth endpoints (401 = wrong credentials).
 * @param cookieStorage    Optional [CookiesStorage] for cookie-based auth.
 * @param authProvider     Optional [KAuthProvider] injecting auth headers on every request.
 *                         Also accepts [KAuthProvider.Custom] for global headers like
 *                         trace IDs or app version — replaces the need for a separate
 *                         request interceptor.
 * @param tokenRefresher   Optional [KTokenRefresher] for automatic 401 token refresh.
 *                         Use [KDefaultTokenRefresher] for standard Bearer JWT flows,
 *                         or implement [KTokenRefresher] directly for custom logic.
 * @param interceptor      Optional [KRequestInterceptor] for global response-side hooks
 *                         (analytics, logging). Request-side mutations belong in [authProvider].
 * @param cacheConfig      Optional [KCacheConfig] for in-memory GET response caching,
 *                         backed by [dev.kindling.utils.CircularBuffer] and [dev.kindling.utils.KMap].
 */
data class KHttpConfig(
    val baseUrl:          String,
    val engine:           HttpClientEngine?        = null,
    val connectTimeoutMs: Long                     = 15_000L,
    val requestTimeoutMs: Long                     = 15_000L,
    val logLevel:         LogLevel                 = LogLevel.NONE,
    val onClientError:    ((Int, String) -> Unit)? = null,
    val onServerError:    ((Int, String) -> Unit)? = null,
    val onSessionExpired: (() -> Unit)?            = null,
    val authPaths:        List<String>             = listOf("/auth/login", "/auth/register"),
    val cookieStorage:    CookiesStorage?          = null,
    val authProvider:     KAuthProvider?           = null,
    val tokenRefresher:   KTokenRefresher?         = null,
    val interceptor:      KRequestInterceptor?     = null,
    val cacheConfig:      KCacheConfig?            = null,
)