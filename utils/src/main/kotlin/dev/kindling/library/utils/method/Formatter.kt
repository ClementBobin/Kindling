package dev.kindling.utils.method

import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Extracts up to two uppercase initials from a full name.
 *
 * Examples:
 * - `"John Doe"` → `"JD"`
 * - `"Alice"` → `"A"`
 * - `"  bob smith jones"` → `"BS"`
 *
 * @param name The full name string to extract initials from.
 * @return A string of up to two uppercase initial characters.
 */
fun initials(name: String) =
    name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")

/**
 * Formats a price as a currency string using the device's default locale.
 *
 * Example output (en-US locale): `"$12.99"` or with `maximumFractionDigits = 0`: `"$12"`
 *
 * @param price The price amount as a [Double] value.
 * @param locale The locale used for currency formatting. Defaults to [Locale.getDefault].
 * @param maximumFractionDigits The maximum number of digits after the decimal point. Defaults to `0`.
 * @return A locale-aware currency string (e.g. `"$12.99"` or `"€12,99"`).
 */
fun formatPrice(
    price: Double,
    locale: Locale = Locale.getDefault(),
    maximumFractionDigits: Int = 0,
): String =
    NumberFormat.getCurrencyInstance(locale).apply {
        this.maximumFractionDigits = maximumFractionDigits
    }.format(price)

/**
 * Formats an ISO-8601 date string into a human-readable date.
 *
 * Example output: `"Jan 5, 2024"`
 *
 * Falls back to the original [isoDate] string if parsing fails.
 *
 * @param isoDate An ISO-8601 date-time string (e.g. `"2024-01-05T10:00:00Z"`).
 * @param pattern The [DateTimeFormatter] pattern to use. Defaults to `"MMM d, yyyy"`.
 * @param locale The locale used for month/day name formatting. Defaults to [Locale.getDefault].
 * @param zoneId The time zone used to convert the instant to a local date. Defaults to [ZoneId.systemDefault].
 * @return A formatted date string, or [isoDate] if parsing fails.
 */
fun formatDate(
    isoDate: String,
    pattern: String = "MMM d, yyyy",
    locale: Locale = Locale.getDefault(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = runCatching {
    val inst = Instant.parse(isoDate)
    DateTimeFormatter.ofPattern(pattern)
        .withLocale(locale)
        .format(inst.atZone(zoneId))
}.getOrDefault(isoDate)

/**
 * Extracts the year from an ISO-8601 date string.
 *
 * Example: `"2024-01-05T10:00:00Z"` → `2024`
 *
 * Falls back to `0` if parsing fails.
 *
 * @param isoDate An ISO-8601 date-time string (e.g. `"2024-01-05T10:00:00Z"`).
 * @param zoneId The time zone used to determine the local year. Defaults to [ZoneId.systemDefault].
 * @return The year as an [Int], or `0` if parsing fails.
 */
fun getYear(
    isoDate: String,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Int = runCatching {
    Instant.parse(isoDate).atZone(zoneId).year
}.getOrDefault(0)