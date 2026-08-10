package dev.kindling.utils.method.format.time

// ─── FormatDuration ───────────────────────────────────────────────────────────

/**
 * Formats a duration given in seconds as `"HH:MM:SS"`.
 * Example: `3661L.secondsToHhMmSs()` → `"01:01:01"`
 */
fun Long.secondsToHhMmSs(): String {
    val sign  = if (this < 0) "-" else ""
    val abs   = kotlin.math.abs(this)
    val h = abs / 3600
    val m = (abs % 3600) / 60
    val s = abs % 60
    return "$sign%02d:%02d:%02d".format(h, m, s)
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
    val sign  = if (this < 0) "-" else ""
    val abs   = kotlin.math.abs(this)
    val ms    = abs % 1000
    val total = abs / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return "$sign%02d:%02d:%02d.%03d".format(h, m, s, ms)
}

/**
 * Formats a duration in seconds as a human-readable string.
 * Example: `3750L.secondsToHuman()` → `"1h 2m 30s"`
 */
fun Long.secondsToHuman(): String {
    if (this == 0L) return "0s"
    val sign  = if (this < 0) "-" else ""
    val abs   = kotlin.math.abs(this)
    val parts = mutableListOf<String>()
    val d = abs / 86400
    val h = (abs % 86400) / 3600
    val m = (abs % 3600) / 60
    val s = abs % 60
    if (d > 0) parts += "${d}d"
    if (h > 0) parts += "${h}h"
    if (m > 0) parts += "${m}m"
    if (s > 0) parts += "${s}s"
    return "$sign${parts.joinToString(" ")}"
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
    val sign  = if (this < 0) "-" else ""
    val abs   = kotlin.math.abs(this)
    val h = abs / 60
    val m = abs % 60
    return when {
        h == 0 -> "$sign${m}m"
        m == 0 -> "$sign${h}h"
        else -> "$sign${h}h ${m}m"
    }
}

/**
 * Converts a duration string `"HH:MM:SS"` to total seconds.
 * Example: `"01:30:00".hhMmSsToSeconds()` → `5400`
 */
fun String.hhMmSsToSeconds(): Long {
    val trimmed = trim()
    val (isNegative, raw) = when {
        trimmed.startsWith("-") -> true to trimmed.substring(1)
        trimmed.startsWith("+") -> false to trimmed.substring(1)
        else -> false to trimmed
    }
    val parts = raw.split(":")
    require(parts.size in 1..3) { "Invalid duration format" }
    val longs = parts.map {
        val valLong = it.toLongOrNull() ?: throw IllegalArgumentException("Non-numeric component")
        require(valLong >= 0) { "Duration component must be non-negative" }
        valLong
    }
    
    val totalSeconds = when (longs.size) {
        3 -> {
            require(longs[1] in 0..59 && longs[2] in 0..59) { "Minutes and seconds must be 0-59" }
            longs[0] * 3600 + longs[1] * 60 + longs[2]
        }
        2 -> {
            require(longs[1] in 0..59) { "Minutes must be 0-59" }
            longs[0] * 60 + longs[1]
        }
        1 -> longs[0]
        else -> 0L
    }
    return if (isNegative) -totalSeconds else totalSeconds
}

/**
 * Formats a number of hours as a decimal string with the `"h"` suffix.
 * Example: `1.5.hoursToLabel()` → `"1.5h"`
 */
fun Double.hoursToLabel(): String =
    if (this == kotlin.math.floor(this)) "${this.toInt()}h" else "${this}h"