package dev.kindling.utils.method.format.financial

// ─── FormatBandwidth ──────────────────────────────────────────────────────────
// Bandwidth / throughput formatting — useful for usage-based billing dashboards,
// storage reports, and network monitoring UIs.

/**
 * Formats a bits-per-second value as a human-readable bandwidth string.
 * Example: `1_500_000.0.bpsToHuman()` → `"1.5 Mbps"`
 */
fun Double.bpsToHuman(decimals: Int = 1): String {
    require(decimals >= 0) { "decimals must be non-negative, was: $decimals" }
    val units = listOf("bps", "Kbps", "Mbps", "Gbps", "Tbps", "Pbps")
    var value = this
    var index = 0
    while (value >= 1000.0 && index < units.lastIndex) { value /= 1000.0; index++ }
    val fmt = if (value == kotlin.math.floor(value)) value.toLong().toString()
              else "%.${decimals}f".format(value)
    return "$fmt ${units[index]}"
}

/**
 * Formats a bytes-per-second value as a human-readable throughput string.
 * Example: `2_097_152.0.bytesPerSecToHuman()` → `"2.0 MB/s"`
 */
fun Double.bytesPerSecToHuman(decimals: Int = 1): String {
    require(decimals >= 0) { "decimals must be non-negative, was: $decimals" }
    val units = listOf("B/s", "KB/s", "MB/s", "GB/s", "TB/s")
    var value = this
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) { value /= 1024.0; index++ }
    val fmt = if (value == kotlin.math.floor(value)) value.toLong().toString()
              else "%.${decimals}f".format(value)
    return "$fmt ${units[index]}"
}

/**
 * Converts bits per second to bytes per second.
 * Example: `8_000_000.0.bpsToBytes()` → `1_000_000.0`
 */
fun Double.bpsToBytes(): Double = this / 8.0

/**
 * Converts bytes per second to bits per second.
 * Example: `1_000_000.0.bytesToBps()` → `8_000_000.0`
 */
fun Double.bytesToBps(): Double = this * 8.0

/**
 * Estimates the transfer time in seconds for a given [fileSizeBytes] at this bandwidth (bps).
 * Example: `10_000_000.0.transferTimeSeconds(100_000_000.0)` → `0.8` seconds
 */
fun Double.transferTimeSeconds(fileSizeBytes: Double): Double {
    require(fileSizeBytes >= 0.0 && fileSizeBytes.isFinite()) {
        "fileSizeBytes must be non-negative and finite, was: $fileSizeBytes"
    }
    return if (this <= 0.0) Double.POSITIVE_INFINITY else (fileSizeBytes * 8.0) / this
}

/**
 * Formats an estimated transfer time from bandwidth (bps) and file size (bytes).
 * Example: `1_000_000.0.transferTimeLabel(5_000_000.0)` → `"40s"`
 */
fun Double.transferTimeLabel(fileSizeBytes: Double): String {
    val secs = transferTimeSeconds(fileSizeBytes)
    if (secs.isInfinite()) return "∞"
    val s = secs.toLong()
    return when {
        s < 60      -> "${s}s"
        s < 3600    -> "${s / 60}m ${s % 60}s"
        else        -> "${s / 3600}h ${(s % 3600) / 60}m"
    }
}

/**
 * Formats a data usage percentage out of a quota.
 * Example: `3_000_000_000.0.usagePercent(10_000_000_000.0)` → `"30.0%"`
 */
fun Double.usagePercent(quota: Double, decimals: Int = 1): String {
    require(decimals >= 0) { "decimals must be non-negative, was: $decimals" }
    if (quota <= 0.0) return "N/A"
    return "${"%.${decimals}f".format((this / quota) * 100.0)}%"
}