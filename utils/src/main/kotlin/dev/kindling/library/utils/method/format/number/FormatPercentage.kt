package dev.kindling.library.utils.method.format.number

// ─── FormatPercentage ─────────────────────────────────────────────────────────

/**
 * Formats a [Double] in the range [0.0, 1.0] as a percentage string.
 * Example: `0.753.toPercent()` → `"75.3%"`
 */
fun Double.toPercent(decimals: Int = 1): String =
    "${"%.${decimals}f".format(this * 100)}%"

/**
 * Formats a [Double] already in percentage scale (0–100) as a percentage string.
 * Example: `75.3.toPercentLabel()` → `"75.3%"`
 */
fun Double.toPercentLabel(decimals: Int = 1): String =
    "${"%.${decimals}f".format(this)}%"

/**
 * Formats a percentage with an explicit sign (useful for change indicators).
 * Example: `(-0.05).toSignedPercent()` → `"-5.0%"`
 * Example: `0.12.toSignedPercent()` → `"+12.0%"`
 */
fun Double.toSignedPercent(decimals: Int = 1): String {
    val value = this * 100
    val sign  = if (value >= 0) "+" else ""
    return "$sign${"%.${decimals}f".format(value)}%"
}

/**
 * Computes the percentage of [part] out of [total], returning 0.0 if [total] is zero.
 * Example: `25.0.percentOf(200.0)` → `12.5`
 */
fun Double.percentOf(total: Double): Double =
    if (total == 0.0) 0.0 else (this / total) * 100.0

/**
 * Computes the percentage change from [previous] to this value.
 * Returns `null` if [previous] is zero.
 * Example: `120.0.percentageChange(100.0)` → `20.0`
 */
fun Double.percentageChange(previous: Double): Double? =
    if (previous == 0.0) null else ((this - previous) / previous) * 100.0

/**
 * Formats the percentage change from [previous] to this value as a signed string.
 * Returns `"N/A"` if [previous] is zero.
 * Example: `120.0.toChangeLabel(100.0)` → `"+20.0%"`
 */
fun Double.toChangeLabel(previous: Double, decimals: Int = 1): String {
    val pct = percentageChange(previous) ?: return "N/A"
    val sign = if (pct >= 0) "+" else ""
    return "$sign${"%.${decimals}f".format(pct)}%"
}

/**
 * Linearly interpolates a [Double] in [[fromMin], [fromMax]] to [[toMin], [toMax]].
 * Example: `50.0.mapRange(0.0, 100.0, 0.0, 1.0)` → `0.5`
 */
fun Double.mapRange(
    fromMin: Double, fromMax: Double,
    toMin: Double,   toMax: Double
): Double {
    if (fromMax == fromMin) return toMin
    return toMin + (this - fromMin) / (fromMax - fromMin) * (toMax - toMin)
}
