package dev.kindling.library.utils.method.format.time

import kotlinx.datetime.*

// ─── FormatRelativeTime ───────────────────────────────────────────────────────

/**
 * Returns a human-readable relative time string from an epoch-second timestamp
 * compared to [now] (defaults to the current instant).
 *
 * Examples:
 * - 30 seconds ago → `"just now"`
 * - 5 minutes ago  → `"5 minutes ago"`
 * - 2 hours ago    → `"2 hours ago"`
 * - 3 days ago     → `"3 days ago"`
 * - 6 months ago   → `"6 months ago"`
 * - 2 years ago    → `"2 years ago"`
 * - in 10 minutes  → `"in 10 minutes"`
 */
fun Long.toRelativeTime(now: Instant = Clock.System.now()): String {
    val then = Instant.fromEpochSeconds(this)
    val diffSeconds = (now - then).inWholeSeconds
    val abs = kotlin.math.abs(diffSeconds)
    val future = diffSeconds < 0

    val label = when {
        abs < 45          -> "just now"
        abs < 90          -> "${if (future) "in " else ""}1 minute${if (!future) " ago" else ""}"
        abs < 2700        -> "${if (future) "in " else ""}${abs / 60} minutes${if (!future) " ago" else ""}"
        abs < 5400        -> "${if (future) "in " else ""}1 hour${if (!future) " ago" else ""}"
        abs < 79200       -> "${if (future) "in " else ""}${abs / 3600} hours${if (!future) " ago" else ""}"
        abs < 129600      -> "${if (future) "in " else ""}1 day${if (!future) " ago" else ""}"
        abs < 2160000     -> "${if (future) "in " else ""}${abs / 86400} days${if (!future) " ago" else ""}"
        abs < 3888000     -> "${if (future) "in " else ""}1 month${if (!future) " ago" else ""}"
        abs < 31104000    -> "${if (future) "in " else ""}${abs / 2592000} months${if (!future) " ago" else ""}"
        abs < 46656000    -> "${if (future) "in " else ""}1 year${if (!future) " ago" else ""}"
        else              -> "${if (future) "in " else ""}${abs / 31536000} years${if (!future) " ago" else ""}"
    }
    return if (abs < 45) "just now" else label
}

/**
 * Returns a relative time string from an [Instant] compared to [now].
 * Example: `someInstant.toRelativeTime()` → `"3 hours ago"`
 */
fun Instant.toRelativeTime(now: Instant = Clock.System.now()): String =
    epochSeconds.toRelativeTime(now)

/**
 * Returns a concise relative time string (e.g. for feed timestamps).
 * Uses abbreviated units: `"s"`, `"m"`, `"h"`, `"d"`, `"w"`, `"mo"`, `"y"`.
 * Example: `someInstant.toShortRelativeTime()` → `"3h"`
 */
fun Instant.toShortRelativeTime(now: Instant = Clock.System.now()): String {
    val abs = kotlin.math.abs((now - this).inWholeSeconds)
    return when {
        abs < 60        -> "${abs}s"
        abs < 3600      -> "${abs / 60}m"
        abs < 86400     -> "${abs / 3600}h"
        abs < 604800    -> "${abs / 86400}d"
        abs < 2592000   -> "${abs / 604800}w"
        abs < 31536000  -> "${abs / 2592000}mo"
        else            -> "${abs / 31536000}y"
    }
}
