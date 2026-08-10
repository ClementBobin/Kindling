package dev.kindling.utils.method.format.communication

// ─── FormatPhoneNumber ────────────────────────────────────────────────────────

/**
 * Strips all non-digit ASCII characters from a phone number string.
 * Example: `"+1 (800) 555-0199".digitsOnly()` → `"18005550199"`
 */
fun String.digitsOnly(): String = filter { it in '0'..'9' }

/**
 * Formats a 10-digit US phone number as `"(XXX) XXX-XXXX"`.
 * Returns the original string if it cannot be parsed.
 * Example: `"8005550199".formatUsPhone()` → `"(800) 555-0199"`
 */
fun String.formatUsPhone(): String {
    val digits = digitsOnly().let { if (it.length == 11 && it.startsWith("1")) it.drop(1) else it }
    if (digits.length != 10) return this
    return "(${digits.take(3)}) ${digits.substring(3, 6)}-${digits.takeLast(4)}"
}

/**
 * Formats a phone number in E.164 international format.
 * Assumes `+1` country code if none provided and digits length is 10.
 * Example: `"8005550199".toE164()` → `"+18005550199"`
 */
fun String.toE164(countryCode: String = "1"): String {
    val normalizedCC = countryCode.digitsOnly()
    require(normalizedCC.length in 1..3) { "Invalid country code" }
    val digits = digitsOnly()
    require(digits.isNotEmpty()) { "Input contains no digits" }

    val result = when {
        digits.length == 10 -> "+$normalizedCC$digits"
        digits.length == 11 && digits.startsWith(normalizedCC) -> "+$digits"
        else -> "+$digits"
    }
    require(result.length <= 16) { "Result exceeds E.164 limit of 15 digits" }
    return result
}

/**
 * Returns true if the string looks like a valid 10-digit US phone number.
 * Example: `"(800) 555-0199".isValidUsPhone()` → `true`
 */
fun String.isValidUsPhone(): Boolean {
    val digits = digitsOnly().let { if (it.length == 11 && it.startsWith("1")) it.drop(1) else it }
    if (digits.length != 10) return false
    val areaCode = digits[0] - '0'
    val centralOffice = digits[3] - '0'
    return areaCode in 2..9 && centralOffice in 2..9
}

/**
 * Masks a phone number, showing only the last [visible] digits.
 * Example: `"8005550199".maskPhone(4)` → `"******0199"`
 */
fun String.maskPhone(visible: Int = 4): String {
    require(visible >= 0) { "visible count must be non-negative" }
    val digits = digitsOnly()
    if (digits.length <= visible) return digits
    return "*".repeat(digits.length - visible) + digits.takeLast(visible)
}

/**
 * Returns a `tel:` URI for use in links and intents.
 * Example: `"+18005550199".toTelUri()` → `"tel:+18005550199"`
 */
fun String.toTelUri(): String = "tel:${digitsOnly().let { if (!it.startsWith("+")) "+$it" else it }}"

/**
 * Formats a phone number with a custom separator pattern.
 * Example: `"0612345678".formatPhonePattern("XX XX XX XX XX")` → `"06 12 34 56 78"` (French style)
 */
fun String.formatPhonePattern(pattern: String): String {
    val digits = digitsOnly()
    val result = StringBuilder()
    var digitIdx = 0
    for (ch in pattern) {
        if (digitIdx >= digits.length) break
        if (ch == 'X') { result.append(digits[digitIdx++]) }
        else           { result.append(ch) }
    }
    return result.toString()
}