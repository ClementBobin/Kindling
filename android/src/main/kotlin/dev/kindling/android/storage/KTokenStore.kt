package dev.kindling.android.storage

/**
 * Abstraction over token persistence.
 *
 * Kindling provides three built-in implementations:
 * - [KSharedPrefsTokenStore]  — plain SharedPreferences (default, not encrypted)
 * - [KEncryptedTokenStore]    — EncryptedSharedPreferences (recommended for production)
 * - [KDataStoreTokenStore]    — Jetpack DataStore
 * - [KInMemoryTokenStore]     — in-memory only (tests / mock)
 *
 * Implement this interface to plug in any other storage backend
 * (Room, SQLCipher, system Keychain, etc.).
 *
 * All methods are `suspend` — implementations may perform I/O safely.
 *
 * Usage with [KSessionManager]:
 * ```kotlin
 * val session = KSessionManager(
 *     store = KEncryptedTokenStore(context)  // swap freely
 * )
 * ```
 */
interface KTokenStore {

    /** Returns the current access token, or `null` if none is stored. */
    suspend fun getAccessToken(): String?

    /** Returns the current refresh token, or `null` if none is stored. */
    suspend fun getRefreshToken(): String?

    /**
     * Persists both tokens atomically (best-effort).
     *
     * @param accessToken  The new access token.
     * @param refreshToken The new refresh token.
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    /** Removes all stored tokens. */
    suspend fun clear()
}