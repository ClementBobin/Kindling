package dev.kindling.utils.method.format.network

import org.json.JSONException
import org.json.JSONObject

// ─── FormatJwt ────────────────────────────────────────────────────────────────
// Pure-Kotlin JWT inspection helpers (no signing/verification — use a dedicated
// library such as `nimbus-jose-jwt` for that).

/**
 * Represents a decoded JWT split into its three parts.
 */
data class JwtParts(
    val header: String,
    val payload: String,
    val signature: String
)

/**
 * Splits a JWT string into its three Base64URL-encoded parts.
 * Returns `null` if the token does not have exactly 3 segments.
 * Example: `"eyJ...".splitJwt()` → `JwtParts(header, payload, signature)`
 */
fun String.splitJwt(): JwtParts? {
    val parts = split(".")
    if (parts.size != 3) return null
    return JwtParts(parts[0], parts[1], parts[2])
}

/**
 * Decodes a Base64URL-encoded JWT segment to a UTF-8 string.
 * Pads to a multiple of 4 as required by the Base64 spec.
 * Example: `"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9".decodeJwtSegment()` → `{"alg":"HS256","typ":"JWT"}`
 */
fun String.decodeJwtSegment(): String {
    val padded = this
        .replace('-', '+')
        .replace('_', '/')
        .let { it + "=".repeat((4 - it.length % 4) % 4) }
    return java.util.Base64.getDecoder().decode(padded).toString(Charsets.UTF_8)
}

@Suppress("SwallowedException") // Intentional: malformed Base64 segments map to null instead of throwing
private fun String.decodeJwtSegmentOrNull(): String? = try {
    decodeJwtSegment()
} catch (e: IllegalArgumentException) {
    null
}

/**
 * Decodes the header segment of a JWT and returns it as a raw JSON string.
 * Returns `null` if the token is malformed.
 * Example: `"eyJ...".jwtHeader()` → `{"alg":"HS256","typ":"JWT"}`
 */
fun String.jwtHeader(): String? = splitJwt()?.header?.decodeJwtSegmentOrNull()

/**
 * Decodes the payload segment of a JWT and returns it as a raw JSON string.
 * Returns `null` if the token is malformed.
 * Example: `"eyJ...".jwtPayload()` → `{"sub":"1234","iat":1710000000}`
 */
fun String.jwtPayload(): String? = splitJwt()?.payload?.decodeJwtSegmentOrNull()

/**
 * Extracts the expiry timestamp (`exp` claim) from a JWT payload.
 * Returns `null` if not present or the token is malformed.
 * Example: `"eyJ...".jwtExpiry()` → `1720000000L`
 */
@Suppress("SwallowedException")
fun String.jwtExpiry(): Long? {
    val payload = jwtPayload() ?: return null
    return try {
        val json = JSONObject(payload)
        if (!json.has("exp")) return null
        val exp = json.get("exp")
        if (exp is Number) exp.toLong() else null
    } catch (e: JSONException) {
        null
    }
}

/**
 * Returns true if the JWT has expired based on its `exp` claim.
 * Uses current system time. Does NOT verify the signature.
 * Example: `"eyJ...".isJwtExpired()` → `true`
 */
fun String.isJwtExpired(): Boolean {
    val exp = jwtExpiry() ?: return false
    return System.currentTimeMillis() / 1000L >= exp
}

/**
 * Extracts the `sub` claim from the JWT payload.
 * Example: `"eyJ...".jwtSubject()` → `"user_123"`
 */
fun String.jwtSubject(): String? {
    val payload = jwtPayload() ?: return null
    return Regex("\"sub\"\\s*:\\s*\"([^\"]+)\"").find(payload)?.groupValues?.get(1)
}

/**
 * Returns the algorithm from the JWT header (`alg` claim).
 * Example: `"eyJ...".jwtAlgorithm()` → `"HS256"`
 */
fun String.jwtAlgorithm(): String? {
    val header = jwtHeader() ?: return null
    return Regex("\"alg\"\\s*:\\s*\"([^\"]+)\"").find(header)?.groupValues?.get(1)
}

/**
 * Returns true if this string looks like a syntactically valid JWT (3 Base64URL segments).
 */
fun String.isJwtFormat(): Boolean {
    val parts = split(".")
    if (parts.size != 3) return false
    val b64url = Regex("^[A-Za-z0-9_-]*$")
    return parts[0].matches(b64url) && parts[1].matches(b64url) && parts[2].matches(b64url)
}