package dev.kindling.android.http

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

/**
 * Defines how authentication headers are injected into every outgoing request.
 *
 * Pass an instance via [KHttpConfig.authProvider]. When `null`, no auth header
 * is added automatically (useful for cookie-based auth or public APIs).
 *
 * Built-in providers:
 * - [Bearer]  — `Authorization: Bearer <token>`
 * - [Jwt]     — alias of [Bearer], explicit for readability
 * - [Basic]   — `Authorization: Basic <base64(user:pass)>`
 * - [ApiKey]  — custom header or `Authorization: ApiKey <key>`
 * - [Custom]  — full control via a lambda
 *
 * Usage:
 * ```kotlin
 * // Bearer / JWT
 * KHttpConfig(
 *     baseUrl      = "https://api.example.com/",
 *     authProvider = KAuthProvider.Bearer { tokenStore.getToken() },
 * )
 *
 * // API key in custom header
 * KHttpConfig(
 *     baseUrl      = "https://api.example.com/",
 *     authProvider = KAuthProvider.ApiKey(header = "X-Api-Key") { "my-secret-key" },
 * )
 *
 * // Custom
 * KHttpConfig(
 *     baseUrl      = "https://api.example.com/",
 *     authProvider = KAuthProvider.Custom { request ->
 *         request.header("X-Tenant-Id", "acme")
 *         request.header("X-Signature", computeSignature())
 *     },
 * )
 * ```
 */
sealed class KAuthProvider {

    /** Called by [buildKHttpClient] before each request to inject auth headers. */
    internal abstract suspend fun apply(request: HttpRequestBuilder)

    // ── Built-in providers ────────────────────────────────────────────────────

    /**
     * Injects `Authorization: Bearer <token>`.
     *
     * @param tokenProvider Suspend lambda returning the current token.
     *                      Called on every request, so token rotation is handled
     *                      automatically (e.g. reading from a `DataStore`).
     */
    class Bearer(private val tokenProvider: suspend () -> String?) : KAuthProvider() {
        override suspend fun apply(request: HttpRequestBuilder) {
            tokenProvider()?.let { request.header(HttpHeaders.Authorization, "Bearer $it") }
        }
    }

    /**
     * Semantic alias for [Bearer] — identical behaviour, clearer intent for JWT flows.
     *
     * @param tokenProvider Suspend lambda returning the current JWT.
     */
    class Jwt(tokenProvider: suspend () -> String?) : KAuthProvider() {
        private val inner = Bearer(tokenProvider)
        override suspend fun apply(request: HttpRequestBuilder) = inner.apply(request)
    }

    /**
     * Injects `Authorization: Basic <base64(username:password)>`.
     *
     * @param credentialsProvider Suspend lambda returning a [Pair] of username to password.
     */
    class Basic(
        private val credentialsProvider: suspend () -> Pair<String, String>?,
    ) : KAuthProvider() {
        override suspend fun apply(request: HttpRequestBuilder) {
            credentialsProvider()?.let { (user, pass) ->
                val encoded = android.util.Base64.encodeToString(
                    "$user:$pass".toByteArray(),
                    android.util.Base64.NO_WRAP,
                )
                request.header(HttpHeaders.Authorization, "Basic $encoded")
            }
        }
    }

    /**
     * Injects an API key either in a custom header or as `Authorization: ApiKey <key>`.
     *
     * @param header      Header name. Defaults to `Authorization`.
     * @param scheme      Scheme prefix when using the `Authorization` header.
     *                    Ignored when [header] is not `Authorization`.
     *                    Defaults to `"ApiKey"`.
     * @param keyProvider Suspend lambda returning the current API key.
     *
     * Examples:
     * ```kotlin
     * // X-Api-Key: my-secret
     * KAuthProvider.ApiKey(header = "X-Api-Key") { "my-secret" }
     *
     * // Authorization: ApiKey my-secret
     * KAuthProvider.ApiKey { "my-secret" }
     *
     * // Authorization: Token my-secret
     * KAuthProvider.ApiKey(scheme = "Token") { "my-secret" }
     * ```
     */
    class ApiKey(
        private val header: String = HttpHeaders.Authorization,
        private val scheme: String = "ApiKey",
        private val keyProvider: suspend () -> String?,
    ) : KAuthProvider() {
        override suspend fun apply(request: HttpRequestBuilder) {
            keyProvider()?.let { key ->
                val value = if (header == HttpHeaders.Authorization) "$scheme $key" else key
                request.header(header, value)
            }
        }
    }

    /**
     * Escape hatch for any auth scheme not covered above.
     *
     * @param block Suspend lambda receiving the [HttpRequestBuilder] directly.
     *              Add any headers or modify the request as needed.
     */
    class Custom(
        private val block: suspend (HttpRequestBuilder) -> Unit,
    ) : KAuthProvider() {
        override suspend fun apply(request: HttpRequestBuilder) = block(request)
    }
}