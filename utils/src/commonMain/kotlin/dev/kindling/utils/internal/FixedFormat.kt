package dev.kindling.utils.internal

/**
 * Multiplatform replacement for `String.format(Locale.US, "%.Nf", value)`.
 *
 * Kotlin common code has no `String.format`, so this reproduces the JVM
 * behaviour exactly: always `.` as decimal separator, no grouping, HALF_UP
 * rounding applied to the shortest decimal representation of the double
 * (so `1.005.toFixed(2)` is `"1.01"`, like Java), and a `-` sign is kept
 * even when the value rounds to zero (`(-0.001).toFixed(2)` is `"-0.00"`).
 */
internal fun Double.toFixed(decimals: Int): String {
    require(decimals >= 0) { "decimals must be non-negative: $decimals" }
    if (isNaN()) return "NaN"
    if (isInfinite()) return if (this > 0) "Infinity" else "-Infinity"

    val negative = this < 0.0 || (this == 0.0 && 1.0 / this < 0.0)
    val (digits, pointPos) = shortestDigits(kotlin.math.abs(this))

    // Split into integer / fractional digit strings.
    val intDigits = when {
        pointPos <= 0 -> "0"
        pointPos >= digits.length -> digits + "0".repeat(pointPos - digits.length)
        else -> digits.substring(0, pointPos)
    }
    val fracDigits = when {
        pointPos <= 0 -> "0".repeat(-pointPos) + digits
        pointPos >= digits.length -> ""
        else -> digits.substring(pointPos)
    }

    // Keep `decimals` fractional digits, round HALF_UP on the next one.
    val kept = (fracDigits + "0".repeat(decimals)).substring(0, decimals)
    val roundUp = fracDigits.length > decimals && fracDigits[decimals] >= '5'
    var all = (intDigits + kept).toCharArray()
    if (roundUp) {
        var i = all.lastIndex
        while (i >= 0) {
            if (all[i] == '9') { all[i] = '0'; i-- } else { all[i] = all[i] + 1; break }
        }
        if (i < 0) all = charArrayOf('1') + all
    }
    val s = all.concatToString()
    val intPart = s.substring(0, s.length - decimals).trimStart('0').ifEmpty { "0" }
    val fracPart = s.substring(s.length - decimals)

    val body = if (decimals == 0) intPart else "$intPart.$fracPart"
    return if (negative) "-$body" else body
}

/** Zero-pads this integer to at least [width] digits (`5.padZero(2)` -> `"05"`). */
internal fun Int.padZero(width: Int): String = toString().padStart(width, '0')

/** Zero-pads this long to at least [width] digits. */
internal fun Long.padZero(width: Int): String = toString().padStart(width, '0')

/**
 * Returns the shortest round-trip decimal digits of a non-negative finite
 * [value] and the position of the decimal point relative to the first digit,
 * independent of how each platform renders `Double.toString()` (plain,
 * `1.0E10`, `1e+21`, `1e-7`...).
 */
private fun shortestDigits(value: Double): Pair<String, Int> {
    val str = value.toString()
    val ePos = str.indexOfFirst { it == 'e' || it == 'E' }
    val mantissa = if (ePos >= 0) str.substring(0, ePos) else str
    val exp = if (ePos >= 0) str.substring(ePos + 1).removePrefix("+").toInt() else 0
    val dot = mantissa.indexOf('.')
    val digits = mantissa.replace(".", "")
    val pointPos = (if (dot >= 0) dot else mantissa.length) + exp
    return digits to pointPos
}

// ─── Math helpers (java.lang.Math is JVM-only) ────────────────────────────────

/** Replacement for `Math.toRadians`. */
internal fun toRadians(degrees: Double): Double = degrees / 180.0 * kotlin.math.PI

/** Replacement for `Math.addExact`: throws [ArithmeticException] on overflow. */
internal fun addExact(a: Long, b: Long): Long {
    val r = a + b
    if (((a xor r) and (b xor r)) < 0) throw ArithmeticException("long overflow")
    return r
}

/** Replacement for `Math.multiplyExact`: throws [ArithmeticException] on overflow. */
internal fun multiplyExact(a: Long, b: Long): Long {
    if (a == 0L || b == 0L) return 0L
    val r = a * b
    if (a == Long.MIN_VALUE && b == -1L || b == Long.MIN_VALUE && a == -1L || r / b != a) {
        throw ArithmeticException("long overflow")
    }
    return r
}
