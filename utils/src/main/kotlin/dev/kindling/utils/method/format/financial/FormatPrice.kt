package dev.kindling.utils.method.format.financial

// ─── FormatPrice ──────────────────────────────────────────────────────────────

/**
 * Formats a [Double] as a price string with a currency symbol.
 * Example: `19.99.toPrice()` → `"$19.99"`
 * Example: `1234.5.toPrice("€", thousandsSep = ".")` → `"€1.234,50"`
 */
fun Double.toPrice(
    symbol: String = "$",
    decimals: Int = 2,
    thousandsSep: String = ",",
    decimalSep: String = ".",
    symbolAfter: Boolean = false
): String {
    require(this.isFinite()) { "Value must be finite: $this" }
    require(decimals >= 0) { "Decimals must be non-negative: $decimals" }

    val formatted = String.format(java.util.Locale.US, "%.${decimals}f", this)
    val parts     = formatted.split(".")
    val intPart   = parts[0].trimStart('-')
        .reversed().chunked(3).joinToString(thousandsSep).reversed()
    val decPart   = if (decimals > 0) "$decimalSep${parts.getOrElse(1) { "0".repeat(decimals) }}" else ""
    val sign      = if (this < 0) "-" else ""
    val amount    = "$sign$intPart$decPart"
    return if (symbolAfter) "$amount $symbol" else "$symbol$amount"
}

/**
 * Formats a [Long] amount in minor currency units (cents) as a price string.
 * Example: `1999L.centsToPrice()` → `"$19.99"`
 */
fun Long.centsToPrice(symbol: String = "$", thousandsSep: String = ","): String =
    (this / 100.0).toPrice(symbol, 2, thousandsSep)

/**
 * Formats a price with compact notation for large values.
 * Example: `1_500_000.0.toCompactPrice()` → `"$1.5M"`
 */
fun Double.toCompactPrice(symbol: String = "$"): String {
    val abs  = kotlin.math.abs(this)
    val sign = if (this < 0) "-" else ""
    val (value, suffix) = when {
        abs >= 1_000_000_000_000 -> abs / 1_000_000_000_000 to "T"
        abs >= 1_000_000_000     -> abs / 1_000_000_000 to "B"
        abs >= 1_000_000         -> abs / 1_000_000 to "M"
        abs >= 1_000             -> abs / 1_000 to "K"
        else                     -> abs to ""
    }
    val formatted = if (value == kotlin.math.floor(value)) value.toInt().toString()
                    else "%.1f".format(value)
    return "$symbol$sign$formatted$suffix"
}

/**
 * Returns a discount label string.
 * Example: `100.0.discountLabel(80.0)` → `"-20%"`
 */
fun Double.discountLabel(salePrice: Double, decimals: Int = 0): String {
    if (this <= 0.0) return ""
    val pct = ((this - salePrice) / this) * 100.0
    return "-${"%.${decimals}f".format(pct)}%"
}

/**
 * Formats a price range.
 * Example: `9.99.toPriceRange(49.99)` → `"$9.99 – $49.99"`
 */
fun Double.toPriceRange(max: Double, symbol: String = "$"): String =
    "${toPrice(symbol)} – ${max.toPrice(symbol)}"

/**
 * Rounds a price to the nearest multiple of [precision].
 * Example: `14.3.roundToNearest(0.99)` → `13.86`
 */
fun Double.roundToNearest(precision: Double): Double {
    require(precision.isFinite() && precision > 0) { "Precision must be positive and finite: $precision" }
    val factor = 1.0 / precision
    return kotlin.math.round(this * factor) / factor
}

/**
 * Applies a VAT/tax rate to a price and returns the total.
 * Example: `100.0.withTax(0.20)` → `120.0`
 */
fun Double.withTax(rate: Double): Double = this * (1.0 + rate)

/**
 * Returns the pre-tax price from a tax-inclusive price.
 * Example: `120.0.withoutTax(0.20)` → `100.0`
 */
fun Double.withoutTax(rate: Double): Double = this / (1.0 + rate)