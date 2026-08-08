package dev.kindling.library.utils.method.format.time

import kotlinx.datetime.*
import kotlinx.datetime.format.*

// ─── FormatDate ───────────────────────────────────────────────────────────────

/**
 * Formats a [LocalDate] as `"YYYY-MM-DD"` (ISO 8601).
 * Example: `LocalDate(2024, 3, 5).toIsoString()` → `"2024-03-05"`
 */
fun LocalDate.toIsoString(): String =
    format(LocalDate.Format { date(LocalDate.Formats.ISO) })

/**
 * Formats a [LocalDate] as `"DD/MM/YYYY"`.
 * Example: `LocalDate(2024, 3, 5).toDayMonthYear()` → `"05/03/2024"`
 */
fun LocalDate.toDayMonthYear(): String =
    format(LocalDate.Format {
        dayOfMonth(); char('/'); monthNumber(); char('/'); year()
    })

/**
 * Formats a [LocalDate] as `"MM/DD/YYYY"` (US style).
 * Example: `LocalDate(2024, 3, 5).toMonthDayYear()` → `"03/05/2024"`
 */
fun LocalDate.toMonthDayYear(): String =
    format(LocalDate.Format {
        monthNumber(); char('/'); dayOfMonth(); char('/'); year()
    })

/**
 * Formats a [LocalDate] as `"Month D, YYYY"`.
 * Example: `LocalDate(2024, 3, 5).toLongDate()` → `"March 5, 2024"`
 */
fun LocalDate.toLongDate(): String {
    val month = Month.entries[monthNumber - 1].name
        .lowercase().replaceFirstChar { it.uppercase() }
    return "$month $dayOfMonth, $year"
}

/**
 * Formats a [LocalDate] as `"Mon DD, YYYY"` (abbreviated month).
 * Example: `LocalDate(2024, 3, 5).toShortDate()` → `"Mar 05, 2024"`
 */
fun LocalDate.toShortDate(): String {
    val abbr = Month.entries[monthNumber - 1].name.take(3)
        .lowercase().replaceFirstChar { it.uppercase() }
    val day = dayOfMonth.toString().padStart(2, '0')
    return "$abbr $day, $year"
}

/**
 * Returns the day-of-week name for this date.
 * Example: `LocalDate(2024, 3, 5).dayOfWeekName()` → `"Tuesday"`
 */
fun LocalDate.dayOfWeekName(): String =
    dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

/**
 * Returns the abbreviated day-of-week name for this date.
 * Example: `LocalDate(2024, 3, 5).dayOfWeekAbbr()` → `"Tue"`
 */
fun LocalDate.dayOfWeekAbbr(): String = dayOfWeekName().take(3)

/**
 * Returns the full month name for this date.
 * Example: `LocalDate(2024, 3, 5).monthName()` → `"March"`
 */
fun LocalDate.monthName(): String =
    Month.entries[monthNumber - 1].name.lowercase().replaceFirstChar { it.uppercase() }

/**
 * Returns true if this date falls on a weekend (Saturday or Sunday).
 */
fun LocalDate.isWeekend(): Boolean =
    dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

/**
 * Returns true if this date falls on a weekday.
 */
fun LocalDate.isWeekday(): Boolean = !isWeekend()

/**
 * Returns the number of days in the month of this date.
 * Example: `LocalDate(2024, 2, 1).daysInMonth()` → `29` (2024 is a leap year)
 */
fun LocalDate.daysInMonth(): Int {
    val next = if (monthNumber == 12) LocalDate(year + 1, 1, 1)
               else LocalDate(year, monthNumber + 1, 1)
    return next.toEpochDays() - LocalDate(year, monthNumber, 1).toEpochDays()
}

/**
 * Returns true if the year of this date is a leap year.
 */
fun LocalDate.isLeapYear(): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

/**
 * Returns the ISO week number (1–53) for this date.
 */
fun LocalDate.weekOfYear(): Int {
    // ISO week number is based on the week containing the first Thursday of the year.
    // An ISO week starts on Monday.
    val target = this.plus(3 - (this.dayOfWeek.isoDayNumber - 1), DateTimeUnit.DAY) // Thursday of this week
    val jan1 = LocalDate(target.year, 1, 1)
    val startDow = jan1.dayOfWeek.isoDayNumber
    val firstThursday = if (startDow <= 4) jan1.plus(4 - startDow, DateTimeUnit.DAY)
                        else jan1.plus(11 - startDow, DateTimeUnit.DAY)
    val firstMonday = firstThursday.plus(-3, DateTimeUnit.DAY)
    
    return ((target.toEpochDays() - firstMonday.toEpochDays()) / 7) + 1
}

/**
 * Returns the quarter (1–4) this date belongs to.
 * Example: `LocalDate(2024, 8, 15).quarter()` → `3`
 */
fun LocalDate.quarter(): Int = (monthNumber + 2) / 3