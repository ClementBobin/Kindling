package dev.kindling.library.utils.method.format.time

// ─── FormatDuration ───────────────────────────────────────────────────────────

/**
 * Formats a duration given in seconds as `"HH:MM:SS"`.
 * Example: `3661L.secondsToHhMmSs()` → `"01:01:01"`
 */
fun Long.secondsToHhMmSs(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

/**
 * Formats a duration in seconds as `"HH:MM:SS"`.
 * Example: `3661.secondsToHhMmSs()` → `"01:01:01"`
 */
fun Int.secondsToHhMmSs(): String = toLong().secondsToHhMmSs()

/**
 * Formats a duration in milliseconds as `"HH:MM:SS.mmm"`.
 * Example: `3661500L.msToHhMmSsMs()` → `"01:01:01.500"`
 */
fun Long.msToHhMmSsMs(): String {
    val ms = this % 1000
    val total = this / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return "%02d:%02d:%02d.%03d".format(h, m, s, ms)
}

/**
 * Formats a duration in seconds as a human-readable string.
 * Example: `3750L.secondsToHuman()` → `"1h 2m 30s"`
 */
fun Long.secondsToHuman(): String {
    if (this == 0L) return "0s"
    val parts = mutableListOf<String>()
    val d = this / 86400
    val h = (this % 86400) / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    if (d > 0) parts += "${d}d"
    if (h > 0) parts += "${h}h"
    if (m > 0) parts += "${m}m"
    if (s > 0) parts += "${s}s"
    return parts.joinToString(" ")
}

/**
 * Formats a duration in milliseconds as a human-readable string.
 * Example: `90500L.msToHuman()` → `"1m 30s"`
 */
fun Long.msToHuman(): String = (this / 1000L).secondsToHuman()

/**
 * Formats a duration in minutes as `"Xh Ym"`.
 * Example: `90.minutesToHuman()` → `"1h 30m"`
 */
fun Int.minutesToHuman(): String {
    val h = this / 60
    val m = this % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

/**
 * Converts a duration string `"HH:MM:SS"` to total seconds.
 * Example: `"01:30:00".hhMmSsToSeconds()` → `5400`
 */
fun String.hhMmSsToSeconds(): Long {
    val parts = split(":").map { it.toLongOrNull() ?: 0L }
    return when (parts.size) {
        3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
        2 -> parts[0] * 60 + parts[1]
        1 -> parts[0]
        else -> 0L
    }
}

/**
 * Formats a number of hours as a decimal string with the `"h"` suffix.
 * Example: `1.5.hoursToLabel()` → `"1.5h"`
 */
fun Double.hoursToLabel(): String =
    if (this == kotlin.math.floor(this)) "${this.toInt()}h" else "${this}h"
