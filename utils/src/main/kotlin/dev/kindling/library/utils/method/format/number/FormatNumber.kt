package dev.kindling.library.utils.method.format.number

// ─── FormatNumber ─────────────────────────────────────────────────────────────

/**
 * Formats a [Double] with [decimals] decimal places.
 * Example: `3.14159.round(2)` → `"3.14"`
 */
fun Double.round(decimals: Int = 2): String {
    require(decimals >= 0) { "decimals must be non-negative" }
    return String.format(java.util.Locale.US, "%.${decimals}f", this)
}

/**
 * Formats a number with thousands separators.
 * Example: `1234567.toThousands()` → `"1,234,567"`
 */
fun Long.toThousands(): String {
    val s = this.toString()
    val neg = s.startsWith("-")
    val digits = if (neg) s.drop(1) else s
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return if (neg) "-$grouped" else grouped
}

/** @see Long.toThousands */
fun Int.toThousands(): String = toLong().toThousands()

/**
 * Formats a [Double] with thousands separators and fixed decimal places.
 * Example: `1234567.89.toThousands(2)` → `"1,234,567.89"`
 */
fun Double.toThousands(decimals: Int = 2): String {
    require(decimals >= 0) { "decimals must be non-negative" }
    val formatted = String.format(java.util.Locale.US, "%.${decimals}f", this)
    val parts     = formatted.split(".")
    val intPart   = parts[0].trimStart('-').reversed().chunked(3).joinToString(",").reversed()
    val signed    = if (this < 0) "-$intPart" else intPart
    return if (parts.size > 1) "$signed.${parts[1]}" else signed
}

/**
 * Abbreviates large numbers with K/M/B/T suffixes.
 * Example: `1_500_000.0.toCompact()` → `"1.5M"`
 */
fun Double.toCompact(decimals: Int = 1): String {
    require(decimals >= 0) { "decimals must be non-negative" }
    val abs = kotlin.math.abs(this)
    val sign = if (this < 0) "-" else ""
    return when {
        abs >= 1_000_000_000_000 -> "$sign${String.format(java.util.Locale.US, "%.${decimals}f", abs / 1_000_000_000_000)}T"
        abs >= 1_000_000_000     -> "$sign${String.format(java.util.Locale.US, "%.${decimals}f", abs / 1_000_000_000)}B"
        abs >= 1_000_000         -> "$sign${String.format(java.util.Locale.US, "%.${decimals}f", abs / 1_000_000)}M"
        abs >= 1_000             -> "$sign${String.format(java.util.Locale.US, "%.${decimals}f", abs / 1_000)}K"
        else                     -> "$sign${String.format(java.util.Locale.US, "%.${decimals}f", abs)}"
    }.trimEnd('0').trimEnd('.')
        .let { if (it.contains('.') || it.last().isDigit()) it else it }
}

/** @see Double.toCompact */
fun Long.toCompact(decimals: Int = 1): String = toDouble().toCompact(decimals)
fun Long.toCompactString(decimals: Int = 1): String = toDouble().toCompact(decimals)
fun Int.toCompactString(decimals: Int = 1): String = toDouble().toCompact(decimals)

/**
 * Clamps a [Double] between [min] and [max].
 * Example: `150.0.clamp(0.0, 100.0)` → `100.0`
 */
fun Double.clamp(min: Double, max: Double): Double = this.coerceIn(min, max)

/**
 * Returns the sign of the number as `"+"`, `"-"`, or `""`.
 * Example: `42.sign()` → `"+"`
 */
fun Number.sign(): String = when {
    toDouble() > 0 -> "+"
    toDouble() < 0 -> "-"
    else            -> ""
}

/**
 * Formats a number with an explicit sign prefix.
 * Example: `42.0.withSign()` → `"+42.0"`
 */
fun Double.withSign(decimals: Int = 0): String {
    require(decimals >= 0) { "decimals must be non-negative" }
    return "${sign()}${String.format(java.util.Locale.US, "%.${decimals}f", kotlin.math.abs(this))}"
}

/**
 * Pads an integer with leading zeros to the given [width].
 * Example: `7.zeroPad(3)` → `"007"`
 */
fun Int.zeroPad(width: Int): String = toString().padStart(width, '0')

/**
 * Returns true if the number is even.
 */
fun Int.isEven(): Boolean = this % 2 == 0

/**
 * Returns true if the number is odd.
 */
fun Int.isOdd(): Boolean = this % 2 != 0

/**
 * Safely divides by [divisor], returning [fallback] when [divisor] is zero.
 * Example: `10.0.safeDivide(0.0)` → `0.0`
 */
fun Double.safeDivide(divisor: Double, fallback: Double = 0.0): Double =
    if (divisor == 0.0) fallback else this / divisor
