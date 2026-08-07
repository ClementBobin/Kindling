package dev.kindling.library.utils.method.format.time

import kotlinx.datetime.*

// ─── FormatFiscalQuarter ──────────────────────────────────────────────────────

/**
 * Returns the fiscal quarter (1–4) for a [LocalDate] given a fiscal year start month.
 * [fiscalYearStartMonth] defaults to `1` (January = calendar year).
 * Example: `LocalDate(2024, 10, 15).fiscalQuarter(startMonth = 10)` → `1`
 */
fun LocalDate.fiscalQuarter(fiscalYearStartMonth: Int = 1): Int {
    val adjusted = ((monthNumber - fiscalYearStartMonth + 12) % 12)
    return (adjusted / 3) + 1
}

/**
 * Returns the fiscal year for a [LocalDate] given a fiscal year start month.
 * Example: `LocalDate(2024, 10, 15).fiscalYear(startMonth = 10)` → `2025`
 */
fun LocalDate.fiscalYear(fiscalYearStartMonth: Int = 1): Int =
    if (monthNumber >= fiscalYearStartMonth && fiscalYearStartMonth != 1) year + 1 else year

/**
 * Returns a formatted fiscal quarter label like `"Q1 FY2025"`.
 * Example: `LocalDate(2024, 10, 15).toFiscalQuarterLabel(startMonth = 10)` → `"Q1 FY2025"`
 */
fun LocalDate.toFiscalQuarterLabel(fiscalYearStartMonth: Int = 1): String {
    val q  = fiscalQuarter(fiscalYearStartMonth)
    val fy = fiscalYear(fiscalYearStartMonth)
    return "Q$q FY$fy"
}

/**
 * Returns the first day of the fiscal quarter this date belongs to.
 * Example: `LocalDate(2024, 11, 20).fiscalQuarterStart(startMonth = 10)` → `LocalDate(2024, 10, 1)`
 */
fun LocalDate.fiscalQuarterStart(fiscalYearStartMonth: Int = 1): LocalDate {
    val q = fiscalQuarter(fiscalYearStartMonth)
    val startMonth = ((fiscalYearStartMonth - 1 + (q - 1) * 3) % 12) + 1
    val startYear  = if (startMonth > monthNumber) year - 1 else year
    return LocalDate(startYear, startMonth, 1)
}

/**
 * Returns the last day of the fiscal quarter this date belongs to.
 */
fun LocalDate.fiscalQuarterEnd(fiscalYearStartMonth: Int = 1): LocalDate {
    val start     = fiscalQuarterStart(fiscalYearStartMonth)
    val endMonth  = ((start.monthNumber - 1 + 3) % 12) + 1
    val endYear   = if (endMonth < start.monthNumber) start.year + 1 else start.year
    val lastDay   = LocalDate(endYear, endMonth, 1).daysInMonth()
    return LocalDate(endYear, endMonth, lastDay)
}
