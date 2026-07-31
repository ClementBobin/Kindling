package dev.kindling.android.http

/**
 * Sealed hierarchy of HTTP-related exceptions thrown by [buildKHttpClient].
 *
 * All subclasses extend [Exception] and carry a human-readable [message]
 * parsed from the response body (fields `message` or `error`), falling back
 * to the raw body (truncated to 200 chars).
 *
 * Typical handling:
 * ```kotlin
 * try {
 *     api.getProducts()
 * } catch (e: KHttpException.ClientError) {
 *     when (e.statusCode) {
 *         401 -> navigateToLogin()
 *         403 -> showPermissionDenied()
 *         else -> toast("Client error ${e.statusCode}: ${e.message}")
 *     }
 * } catch (e: KHttpException.ServerError) {
 *     toast("Server error ${e.statusCode}: ${e.message}")
 * } catch (e: KHttpException.NotAccepted) {
 *     log("Unexpected status: ${e.message}")
 * }
 * ```
 */
sealed class KHttpException(message: String) : Exception(message) {

    /**
     * Thrown by [io.ktor.client.statement.HttpResponse.accept] when the response status does not match
     * any of the expected [io.ktor.http.HttpStatusCode]s.
     *
     * This is a manual assertion — not raised automatically by [buildKHttpClient].
     *
     * @param message Description including the actual status and the expected ones.
     */
    class NotAccepted(message: String) : KHttpException(message)

    /**
     * Thrown on 4xx responses.
     *
     * **401 behaviour:**
     * - On [KHttpConfig.authPaths] → wrong credentials (login/register failed).
     * - Elsewhere → session expired; [KHttpConfig.onSessionExpired] is invoked first.
     *
     * [KHttpConfig.onClientError] is always invoked before this is thrown.
     *
     * @param statusCode HTTP status code (400–499).
     * @param message    Error detail parsed from the response body.
     */
    class ClientError(val statusCode: Int, message: String) : KHttpException(message)

    /**
     * Thrown on 5xx responses, or with `statusCode = -1` for network-level
     * failures (timeout, no connectivity, etc.).
     *
     * [KHttpConfig.onServerError] is always invoked before this is thrown.
     *
     * @param statusCode HTTP status code (500–599), or `-1` for network errors.
     * @param message    Error detail parsed from the response body, or the
     *                   exception message for network errors.
     */
    class ServerError(val statusCode: Int, message: String) : KHttpException(message)
}