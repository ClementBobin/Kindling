package dev.kindling.android.storage

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url

/**
 * A [CookiesStorage] implementation backed by [KSessionManager].
 *
 * Maps the session tokens to named cookies so Ktor sends them automatically
 * on every request. Cookie names are configurable to match your API's expectations.
 *
 * Pass this to [dev.kindling.android.http.KHttpConfig.cookieStorage]:
 * ```kotlin
 * KHttpConfig(
 *     baseUrl       = BuildConfig.BASE_URL,
 *     cookieStorage = KSessionCookieStorage(
 *         session          = sessionManager,
 *         accessTokenName  = "cyna_token",
 *         refreshTokenName = "cyna_refresh_token",
 *     ),
 *     onSessionExpired = { sessionManager.clearSession() },
 * )
 * ```
 *
 * @param session            The [KSessionManager] holding the tokens.
 * @param accessTokenName    Cookie name for the access token.
 * @param refreshTokenName   Cookie name for the refresh token.
 */
class KSessionCookieStorage(
    private val session: KSessionManager,
    private val accessTokenName:  String = "access_token",
    private val refreshTokenName: String = "refresh_token",
) : CookiesStorage {

    override suspend fun get(requestUrl: Url): List<Cookie> = buildList {
        session.accessToken.value?.takeIf  { it.isNotEmpty() }
            ?.let { add(Cookie(name = accessTokenName,  value = it)) }
        session.refreshToken.value?.takeIf { it.isNotEmpty() }
            ?.let { add(Cookie(name = refreshTokenName, value = it)) }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        val value = cookie.value.takeIf { it.isNotEmpty() }
        when (cookie.name) {
            accessTokenName  -> session.saveTokens(
                value,
                session.refreshToken.value,
            )
            refreshTokenName -> session.saveTokens(
                session.accessToken.value,
                value,
            )
        }
    }

    override fun close() {}
}