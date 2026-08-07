package dev.kindling.library.utils.method.format.time

import kotlinx.datetime.*

// ─── FormatTimeDelta ──────────────────────────────────────────────────────────

/**
 * Represents the components of a time delta.
 */
data class TimeDelta(
    val years: Int,
    val months: Int,
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val totalSeconds: Long
)

/**
 * Computes the [TimeDelta] between two [Instant]s.
 * [other] defaults to now.
 * Example: `startInstant.deltaTo()` → `TimeDelta(years=0, months=2, days=5, ...)`
 */
fun Instant.deltaTo(other: Instant = Clock.System.now()): TimeDelta {
    val totalSeconds = kotlin.math.abs((other - this).inWholeSeconds)
    var remaining = totalSeconds
    val years   = (remaining / 31536000).toInt(); remaining %= 31536000
    val months  = (remaining / 2592000).toInt();  remaining %= 2592000
    val days    = (remaining / 86400).toInt();     remaining %= 86400
    val hours   = (remaining / 3600).toInt();      remaining %= 3600
    val minutes = (remaining / 60).toInt();        remaining %= 60
    val seconds = remaining.toInt()
    return TimeDelta(years, months, days, hours, minutes, seconds, totalSeconds)
}

/**
 * Formats a [TimeDelta] as a verbose string showing all non-zero components.
 * Example: `delta.toLongString()` → `"2 months, 5 days, 3 hours"`
 */
fun TimeDelta.toLongString(): String {
    val parts = mutableListOf<String>()
    if (years > 0)   parts += "$years ${if (years == 1) "year" else "years"}"
    if (months > 0)  parts += "$months ${if (months == 1) "month" else "months"}"
    if (days > 0)    parts += "$days ${if (days == 1) "day" else "days"}"
    if (hours > 0)   parts += "$hours ${if (hours == 1) "hour" else "hours"}"
    if (minutes > 0) parts += "$minutes ${if (minutes == 1) "minute" else "minutes"}"
    if (seconds > 0) parts += "$seconds ${if (seconds == 1) "second" else "seconds"}"
    return parts.joinToString(", ").ifEmpty { "0 seconds" }
}

/**
 * Formats a [TimeDelta] showing only the two most significant components.
 * Example: `delta.toShortString()` → `"2 months, 5 days"`
 */
fun TimeDelta.toShortString(): String =
    toLongString().split(", ").take(2).joinToString(", ")

/**
 * Formats the delta between two epoch-second timestamps as a verbose string.
 * Example: `startEpoch.deltaStringTo(endEpoch)` → `"1 year, 3 months"`
 */
fun Long.deltaStringTo(other: Long): String {
    val start = Instant.fromEpochSeconds(this)
    val end   = Instant.fromEpochSeconds(other)
    return start.deltaTo(end).toLongString()
}
