package dev.kindling.library.utils.method.format.text

// ─── FormatPunycode ───────────────────────────────────────────────────────────
// Pure-Kotlin Punycode encoder/decoder (RFC 3492) with IDN helpers.
// No java.net dependency — safe for KMP targets.

private const val BASE         = 36
private const val TMIN         = 1
private const val TMAX         = 26
private const val SKEW         = 38
private const val DAMP         = 700
private const val INITIAL_BIAS = 72
private const val INITIAL_N    = 128
private const val DELIMITER    = '-'

private fun adapt(delta: Int, numPoints: Int, firstTime: Boolean): Int {
    var d = if (firstTime) delta / DAMP else delta / 2
    d += d / numPoints
    var k = 0
    while (d > (BASE - TMIN) * TMAX / 2) { d /= BASE - TMIN; k += BASE }
    return k + (BASE - TMIN + 1) * d / (d + SKEW)
}

private fun digitToChar(d: Int): Char =
    if (d < 26) 'a' + d else '0' + (d - 26)

private fun charToDigit(c: Char): Int = when {
    c in 'a'..'z' -> c - 'a'
    c in 'A'..'Z' -> c - 'A'
    c in '0'..'9' -> c - '0' + 26
    else           -> BASE
}

/**
 * Encodes a Unicode label to Punycode (without the `xn--` prefix).
 * Example: `"münchen".encodePunycode()` → `"mnchen-3ya"`
 */
fun String.encodePunycode(): String {
    val input  = toCodePoints()
    val output = StringBuilder()
    val basic  = input.filter { it < INITIAL_N }
    basic.forEach { output.append(it.toChar()) }
    val basicLen = basic.size
    val inputLen = input.size
    if (basicLen > 0 && basicLen < inputLen) output.append(DELIMITER)
    var n    = INITIAL_N
    var delta = 0
    var bias  = INITIAL_BIAS
    var h    = basicLen
    while (h < inputLen) {
        val m = input.filter { it >= n }.minOrNull() ?: break
        delta += (m - n) * (h + 1)
        n = m
        for (c in input) {
            if (c < n) delta++
            if (c == n) {
                var q = delta; var k = BASE
                while (true) {
                    val t = when { k <= bias -> TMIN; k >= bias + TMAX -> TMAX; else -> k - bias }
                    if (q < t) break
                    output.append(digitToChar(t + (q - t) % (BASE - t)))
                    q = (q - t) / (BASE - t); k += BASE
                }
                output.append(digitToChar(q))
                bias  = adapt(delta, h + 1, h == basicLen)
                delta = 0; h++
            }
        }
        delta++; n++
    }
    return output.toString()
}

/**
 * Decodes a Punycode-encoded label (without the `xn--` prefix) to Unicode.
 * Example: `"mnchen-3ya".decodePunycode()` → `"münchen"`
 */
fun String.decodePunycode(): String {
    val input   = this
    val delimIdx = input.lastIndexOf(DELIMITER)
    val output  = if (delimIdx >= 0) input.take(delimIdx).toCodePoints().toMutableList()
                  else mutableListOf<Int>()
    var i    = 0
    var n    = INITIAL_N
    var bias = INITIAL_BIAS
    var pos  = if (delimIdx >= 0) delimIdx + 1 else 0
    while (pos < input.length) {
        val oldI = i; var w = 1; var k = BASE
        while (true) {
            if (pos >= input.length) break
            val d = charToDigit(input[pos++])
            i += d * w
            val t = when { k <= bias -> TMIN; k >= bias + TMAX -> TMAX; else -> k - bias }
            if (d < t) break
            w *= BASE - t; k += BASE
        }
        bias = adapt(i - oldI, output.size + 1, oldI == 0)
        n += i / (output.size + 1)
        i %= output.size + 1
        output.add(i, n)
        i++
    }
    val sb = StringBuilder()
    output.forEach { cp ->
        if (cp < 0x10000) {
            sb.append(cp.toChar())
        } else {
            sb.append(((cp - 0x10000) shr 10 or 0xD800).toChar())
            sb.append(((cp - 0x10000) and 0x3FF or 0xDC00).toChar())
        }
    }
    return sb.toString()
}

private fun String.toCodePoints(): List<Int> {
    val result = mutableListOf<Int>()
    var i = 0
    while (i < length) {
        val cp = codePointAt(i)
        result.add(cp)
        i += if (Character.isSupplementaryCodePoint(cp)) 2 else 1
    }
    return result
}

/**
 * Converts a Unicode domain name to its ASCII-compatible encoding (ACE / IDN).
 * Example: `"münchen.de".toAce()` → `"xn--mnchen-3ya.de"`
 */
fun String.toAce(): String =
    lowercase().split('.').joinToString(".") { label ->
        if (label.all { it.code < INITIAL_N }) label
        else "xn--${label.encodePunycode()}"
    }

/**
 * Converts an ACE (xn--) domain name back to Unicode.
 * Example: `"xn--mnchen-3ya.de".fromAce()` → `"münchen.de"`
 */
fun String.fromAce(): String =
    split('.').joinToString(".") { label ->
        if (label.startsWith("xn--", ignoreCase = true))
            label.drop(4).decodePunycode()
        else label
    }

/**
 * Returns true if this string is a valid IDN (contains non-ASCII labels).
 */
fun String.isIdn(): Boolean = split('.').any { label -> label.any { it.code >= INITIAL_N } }
