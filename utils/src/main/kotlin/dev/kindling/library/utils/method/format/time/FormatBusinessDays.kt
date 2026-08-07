package dev.kindling.library.utils.method.format.time

import kotlinx.datetime.*

// ─── FormatBusinessDays ───────────────────────────────────────────────────────

/**
 * Returns the number of business days (Mon–Fri) between this date and [to].
 * Example: `LocalDate(2024, 3, 4).businessDaysUntil(LocalDate(2024, 3, 11))` → `5`
 */
fun LocalDate.businessDaysUntil(to: LocalDate): Int {
    var count = 0
    var current = this
    val step = if (to >= this) 1 else -1
    while (current != to) {
        current = current.plus(step, DateTimeUnit.DAY)
        if (current.dayOfWeek != DayOfWeek.SATURDAY && current.dayOfWeek != DayOfWeek.SUNDAY) {
            count++
        }
    }
    return count * step
}

/**
 * Adds [days] business days to this date, skipping weekends.
 * Example: `LocalDate(2024, 3, 1).plusBusinessDays(3)` → `LocalDate(2024, 3, 6)` (skips Sat/Sun)
 */
fun LocalDate.plusBusinessDays(days: Int): LocalDate {
    var result = this
    var remaining = days
    val step = if (days >= 0) 1 else -1
    while (remaining != 0) {
        result = result.plus(step, DateTimeUnit.DAY)
        if (result.dayOfWeek != DayOfWeek.SATURDAY && result.dayOfWeek != DayOfWeek.SUNDAY) {
            remaining -= step
        }
    }
    return result
}

/**
 * Returns a human-readable business-day label.
 * - 0  → `"Today"`
 * - 1  → `"Tomorrow"`
 * - -1 → `"Yesterday"`
 * - N  → `"In N business days"` / `"N business days ago"`
 *
 * Example: `LocalDate(2024, 3, 4).toBusinessDayLabel(LocalDate(2024, 3, 6))` → `"In 2 business days"`
 */
fun LocalDate.toBusinessDayLabel(from: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): String {
    val delta = from.businessDaysUntil(this)
    return when (delta) {
        0    -> "Today"
        1    -> "Tomorrow"
        -1   -> "Yesterday"
        else -> if (delta > 0) "In $delta business days" else "${-delta} business days ago"
    }
}

/**
 * Returns the next business day after this date (skips weekends).
 * Example: `LocalDate(2024, 3, 8).nextBusinessDay()` → `LocalDate(2024, 3, 11)` (skips weekend)
 */
fun LocalDate.nextBusinessDay(): LocalDate = plusBusinessDays(1)

/**
 * Returns the previous business day before this date.
 */
fun LocalDate.previousBusinessDay(): LocalDate = plusBusinessDays(-1)

/**
 * Returns true if this date is a business day (Mon–Fri).
 */
fun LocalDate.isBusinessDay(): Boolean =
    dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
