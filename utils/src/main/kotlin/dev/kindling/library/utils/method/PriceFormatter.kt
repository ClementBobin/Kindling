import java.text.NumberFormat
import java.util.Locale

/**
 * Formats a monthly price as a currency string using the device's default locale,
 * with no decimal/fraction digits.
 *
 * Example output (en-US locale): `$12`
 *
 * @param price The monthly price as a [Double] value.
 * @return A locale-aware currency string with no fractional digits (e.g. `"$12"` or `"€12"`).
 */
fun formatMonthlyPrice(price: Double): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }.format(price)
