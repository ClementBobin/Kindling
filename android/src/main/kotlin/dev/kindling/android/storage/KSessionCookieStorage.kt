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
 * This storage is restricted to specific hosts to prevent leaking session tokens
 * to unrelated domains (e.g. analytics or 3rd party CDNs).
 *
 * @param session            The [KSessionManager] holding the tokens.
 * @param apiHost            The host (e.g. "api.example.com") to which these cookies
 *                           should be sent. If null, cookies are sent to every host (not recommended).
 * @param accessTokenName    Cookie name for the access token.
 * @param refreshTokenName   Cookie name for the refresh token.
 */
class KSessionCookieStorage(
    private val session: KSessionManager,
    private val apiHost: String? = null,
    private val accessTokenName:  String = "access_token",
    private val refreshTokenName: String = "refresh_token",
) : CookiesStorage {

    override suspend fun get(requestUrl: Url): List<Cookie> {
        // Only emit cookies if host matches apiHost (if specified)
        if (apiHost != null && !requestUrl.host.equals(apiHost, ignoreCase = true)) {
            return emptyList()
        }

        return buildList {
            session.accessToken.value?.takeIf  { it.isNotEmpty() }
                ?.let { add(Cookie(name = accessTokenName,  value = it)) }
            session.refreshToken.value?.takeIf { it.isNotEmpty() }
                ?.let { add(Cookie(name = refreshTokenName, value = it)) }
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        // Only accept cookies from the designated API host
        if (apiHost != null && !requestUrl.host.equals(apiHost, ignoreCase = true)) {
            return
        }

        val value = cookie.value.takeIf { it.isNotEmpty() }
        when (cookie.name) {
            accessTokenName  -> session.updateAccessToken(value)
            refreshTokenName -> session.updateRefreshToken(value)
        }
    }

    override fun close() {}
}
