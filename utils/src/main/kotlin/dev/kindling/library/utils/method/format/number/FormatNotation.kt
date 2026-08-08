package dev.kindling.library.utils.method.format.number

import kotlin.math.*

// ─── FormatNotation ───────────────────────────────────────────────────────────

/**
 * Formats a [Double] in standard scientific notation.
 * Example: `123456.789.toScientific()` → `"1.23e+05"`
 */
fun Double.toScientific(decimals: Int = 2): String {
    require(decimals >= 0) { "decimals must be non-negative" }
    if (this == 0.0) return "0.${"0".repeat(decimals)}e+00"
    var exp  = floor(log10(abs(this))).toInt()
    var mant = this / 10.0.pow(exp)
    
    // Normalize if rounding carries over to 10.0
    val formattedMant = String.format(java.util.Locale.US, "%.${decimals}f", mant)
    if (formattedMant.toDouble().absoluteValue >= 10.0) {
        mant /= 10.0
        exp++
    }
    
    val sign = if (exp >= 0) "+" else "-"
    return "${String.format(java.util.Locale.US, "%.${decimals}f", mant)}e$sign${abs(exp).toString().padStart(2, '0')}"
}

/**
 * Formats a [Double] in engineering notation (exponent multiple of 3).
 * Example: `123456.0.toEngineering()` → `"123.456e+03"`
 */
fun Double.toEngineering(decimals: Int = 3): String {
    require(decimals >= 0) { "decimals must be non-negative" }
    if (this == 0.0) return "0.${"0".repeat(decimals)}e+00"
    val rawExp  = floor(log10(abs(this))).toInt()
    val engExp  = floor(rawExp / 3.0).toInt() * 3
    val mant    = this / 10.0.pow(engExp)
    val sign    = if (engExp >= 0) "+" else "-"
    return "${String.format(java.util.Locale.US, "%.${decimals}f", mant)}e$sign${abs(engExp).toString().padStart(2, '0')}"
}

/**
 * Formats a [Double] with an SI prefix (µ, m, k, M, G, T, P).
 * Example: `0.001234.toSiPrefix()` → `"1.234 m"`
 * Example: `1_500_000.0.toSiPrefix()` → `"1.5 M"`
 */
fun Double.toSiPrefix(decimals: Int = 2): String {
    require(decimals >= 0) { "decimals must be non-negative" }
    val prefixes = listOf(
        1e-12 to "p",
        1e-9  to "n",
        1e-6  to "µ",
        1e-3  to "m",
        1.0   to "",
        1e3   to "k",
        1e6   to "M",
        1e9   to "G",
        1e12  to "T",
        1e15  to "P"
    )
    val absVal = abs(this)
    val (factor, prefix) = prefixes.lastOrNull { absVal >= it.first }
        ?: (1.0 to "")
    val formatted = String.format(java.util.Locale.US, "%.${decimals}f", this / factor)
    return "$formatted${if (prefix.isNotEmpty()) " $prefix" else ""}"
}

/**
 * Formats a [Double] in binary (power-of-two) notation with IEC prefixes (Ki, Mi, Gi…).
 * Example: `1_048_576.0.toBinaryPrefix()` → `"1.00 Mi"`
 */
fun Double.toBinaryPrefix(decimals: Int = 2): String {
    require(decimals >= 0) { "decimals must be non-negative" }
    val prefixes = listOf("", "Ki", "Mi", "Gi", "Ti", "Pi")
    var value = abs(this)
    var index = 0
    while (value >= 1024.0 && index < prefixes.lastIndex) {
        value /= 1024.0
        index++
    }
    val signedValue = if (this < 0) -value else value
    val formatted = String.format(java.util.Locale.US, "%.${decimals}f", signedValue)
    val prefix = prefixes[index]
    return if (prefix.isEmpty()) formatted else "$formatted $prefix"
}

/**
 * Converts a [Double] to its Roman numeral representation (supports 1–3999).
 * Example: `2024.0.toRoman()` → `"MMXXIV"`
 */
fun Int.toRoman(): String {
    require(this in 1..3999) { "Roman numerals only support 1–3999" }
    val vals   = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    val syms   = arrayOf("M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I")
    var n      = this
    val result = StringBuilder()
    for (i in vals.indices) {
        while (n >= vals[i]) { result.append(syms[i]); n -= vals[i] }
    }
    return result.toString()
}

/**
 * Parses a Roman numeral string to an [Int]. Case-insensitive.
 * Example: `"MMXXIV".fromRoman()` → `2024`
 */
fun String.fromRoman(): Int {
    val map = mapOf('I' to 1,'V' to 5,'X' to 10,'L' to 50,
                    'C' to 100,'D' to 500,'M' to 1000)
    val s = uppercase()
    require(s.all { it in map.keys }) { "Invalid Roman numeral character" }
    var result = 0
    for (i in s.indices) {
        val cur  = map[s[i]]!!
        val next = if (i + 1 < s.length) map[s[i + 1]]!! else 0
        result += if (cur < next) -cur else cur
    }
    require(result.toRoman() == s) { "Non-canonical Roman numeral: $s" }
    return result
}
