package dev.kindling.library.utils.method.format.time

import kotlinx.datetime.*

// ─── FormatTimeZoneOffset ─────────────────────────────────────────────────────

/**
 * Formats a UTC offset in total minutes as `"UTC±HH:MM"`.
 * Example: `330.minutesToUtcOffset()` → `"UTC+05:30"`
 * Example: `(-300).minutesToUtcOffset()` → `"UTC-05:00"`
 */
fun Int.minutesToUtcOffset(): String {
    val sign  = if (this >= 0) "+" else "-"
    val abs   = kotlin.math.abs(this)
    val hours = abs / 60
    val mins  = abs % 60
    return "UTC$sign%02d:%02d".format(hours, mins)
}

/**
 * Formats a UTC offset in total seconds as `"UTC±HH:MM"`.
 * Example: `19800.secondsToUtcOffset()` → `"UTC+05:30"`
 */
fun Int.secondsToUtcOffset(): String = (this / 60).minutesToUtcOffset()

/**
 * Returns the UTC offset string for a named [TimeZone] at a given [Instant].
 * Example: `TimeZone.of("America/New_York").utcOffsetAt()` → `"UTC-05:00"`
 */
fun TimeZone.utcOffsetAt(instant: Instant = Clock.System.now()): String =
    offsetAt(instant).totalSeconds.secondsToUtcOffset()

/**
 * Returns the display name of a [TimeZone] with its current UTC offset.
 * Example: `TimeZone.of("Europe/Paris").displayName()` → `"Europe/Paris (UTC+01:00)"`
 */
fun TimeZone.displayName(instant: Instant = Clock.System.now()): String =
    "${id} (${utcOffsetAt(instant)})"

/**
 * Returns a short label for a [TimeZone] offset.
 * Example: `TimeZone.of("Asia/Kolkata").shortLabel()` → `"UTC+5:30"`
 */
fun TimeZone.shortLabel(instant: Instant = Clock.System.now()): String {
    val totalMins = offsetAt(instant).totalSeconds / 60
    val sign  = if (totalMins >= 0) "+" else "-"
    val abs   = kotlin.math.abs(totalMins)
    val hours = abs / 60
    val mins  = abs % 60
    return if (mins == 0) "UTC$sign$hours" else "UTC$sign$hours:${"%02d".format(mins)}"
}

/**
 * Converts an [Instant] to a formatted datetime string in the given [TimeZone].
 * Example: `someInstant.formatInZone(TimeZone.of("Europe/Paris"))` → `"2024-03-05 14:30:00 UTC+01:00"`
 */
fun Instant.formatInZone(tz: TimeZone): String {
    val local  = toLocalDateTime(tz)
    val offset = tz.utcOffsetAt(this)
    return "%04d-%02d-%02d %02d:%02d:%02d %s".format(
        local.year, local.monthNumber, local.dayOfMonth,
        local.hour, local.minute, local.second, offset
    )
}
