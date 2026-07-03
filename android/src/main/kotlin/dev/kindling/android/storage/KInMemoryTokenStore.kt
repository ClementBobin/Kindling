package dev.kindling.android.storage

/**
 * In-memory [KTokenStore] implementation.
 *
 * Tokens are never persisted — they are lost when the instance is discarded.
 * Intended for unit tests and mock environments where persistence is undesirable.
 *
 * Usage:
 * ```kotlin
 * // In tests
 * val store   = KInMemoryTokenStore()
 * val session = KSessionManager(store)
 *
 * // Pre-seed a token for authenticated tests
 * val store = KInMemoryTokenStore(
 *     initialAccessToken  = "test-access-token",
 *     initialRefreshToken = "test-refresh-token",
 * )
 * ```
 *
 * @param initialAccessToken  Optional pre-seeded access token.
 * @param initialRefreshToken Optional pre-seeded refresh token.
 */
class KInMemoryTokenStore(
    initialAccessToken:  String? = null,
    initialRefreshToken: String? = null,
) : KTokenStore {

    private var accessToken:  String? = initialAccessToken
    private var refreshToken: String? = initialRefreshToken

    override suspend fun getAccessToken():  String? = accessToken
    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun saveTokens(accessToken: String?, refreshToken: String?) {
        this.accessToken  = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun clear() {
        accessToken  = null
        refreshToken = null
    }
}
