package dev.kindling.android.natif

import android.content.Context
import android.util.Base64
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper for requesting Play Integrity tokens on Android.
 *
 * Wraps the [IntegrityManagerFactory] API in a coroutine-friendly interface,
 * allowing callers to `await` a signed integrity token without managing
 * callbacks manually.
 *
 * Typical usage:
 * ```kotlin
 * val token = playIntegrityHelper.requestToken()
 * integrityRepository.verify(token)
 * ```
 *
 * @param context An Android [Context] used to initialise the [IntegrityManagerFactory].
 *   An application context is preferred to avoid Activity leaks.
 *
 * @see <a href="https://developer.android.com/google/play/integrity/overview">Play Integrity API</a>
 */
class PlayIntegrityHelper(private val context: Context) {

    /**
     * Generates a cryptographically secure, URL-safe Base64-encoded nonce.
     *
     * The nonce is 16 random bytes (128 bits), which provides sufficient entropy
     * to make each integrity request unique and replay-resistant.
     *
     * The result is encoded with [Base64.URL_SAFE] and [Base64.NO_WRAP] flags
     * so it can be safely embedded in JSON or HTTP headers without padding issues.
     *
     * @return A Base64 URL-safe string with no line wrapping.
     */
    private fun generateNonce(): String {
        val nonce = ByteArray(16)
        SecureRandom().nextBytes(nonce)
        return Base64.encodeToString(nonce, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    /**
     * Requests a Play Integrity token from the Google Play Integrity API.
     *
     * The token is a signed JWT that encodes device integrity, app integrity,
     * and account details. It should be forwarded to your backend server for
     * verification via the Play Integrity API.
     *
     * This function is **coroutine-safe**: it suspends the calling coroutine
     * until the token is available or an error occurs, and supports cooperative
     * cancellation via [suspendCancellableCoroutine].
     *
     * A fresh nonce is generated automatically for each call to prevent replay
     * attacks.
     *
     * Example flow:
     * ```kotlin
     * // Android side
     * val token = playIntegrityHelper.requestToken()
     * api.verifyIntegrity(token)
     *
     * // Backend side (ASP.NET Core / any server)
     * // POST https://playintegrity.googleapis.com/v1/{packageName}:decodeIntegrityToken
     * // { "integrity_token": "<token>" }
     * ```
     *
     * @return A signed Play Integrity token string to be sent to your backend.
     *
     * @throws com.google.android.play.core.integrity.IntegrityServiceException
     *   If the Play Integrity service is unavailable, the app is not recognised
     *   by Google Play, or the device fails integrity checks.
     * @throws kotlinx.coroutines.CancellationException
     *   If the calling coroutine is cancelled before the token is delivered.
     */
    suspend fun requestToken(): String = suspendCancellableCoroutine { cont ->
        val manager = IntegrityManagerFactory.create(context)
        val request = IntegrityTokenRequest.builder()
            .setNonce(generateNonce())
            .build()

        manager.requestIntegrityToken(request)
            .addOnSuccessListener { response -> cont.resume(response.token()) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}