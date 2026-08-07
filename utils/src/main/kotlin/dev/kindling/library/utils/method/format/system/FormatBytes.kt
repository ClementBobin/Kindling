package dev.kindling.library.utils.method.format.system

// ─── FormatBytes ──────────────────────────────────────────────────────────────

/**
 * Formats a byte count as a human-readable string using binary (IEC) prefixes.
 * Example: `1_572_864L.bytesToHuman()` → `"1.5 MiB"`
 */
fun Long.bytesToHuman(decimals: Int = 1): String {
    val units = listOf("B", "KiB", "MiB", "GiB", "TiB", "PiB")
    var value = toDouble()
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) { value /= 1024.0; index++ }
    val fmt = if (value == kotlin.math.floor(value)) value.toLong().toString()
              else "%.${decimals}f".format(value)
    return "$fmt ${units[index]}"
}

/** @see Long.bytesToHuman */
fun Int.bytesToHuman(decimals: Int = 1): String = toLong().bytesToHuman(decimals)

/**
 * Formats a byte count using SI (decimal, power-of-1000) prefixes.
 * Example: `1_500_000L.bytesToHumanSi()` → `"1.5 MB"`
 */
fun Long.bytesToHumanSi(decimals: Int = 1): String {
    val units = listOf("B", "KB", "MB", "GB", "TB", "PB")
    var value = toDouble()
    var index = 0
    while (value >= 1000.0 && index < units.lastIndex) { value /= 1000.0; index++ }
    val fmt = if (value == kotlin.math.floor(value)) value.toLong().toString()
              else "%.${decimals}f".format(value)
    return "$fmt ${units[index]}"
}

/**
 * Converts bytes to kibibytes (KiB).
 * Example: `2048L.toKiB()` → `2.0`
 */
fun Long.toKiB(): Double = this / 1024.0
fun Long.toMiB(): Double = this / (1024.0 * 1024.0)
fun Long.toGiB(): Double = this / (1024.0 * 1024.0 * 1024.0)
fun Long.toTiB(): Double = this / (1024.0 * 1024.0 * 1024.0 * 1024.0)

/**
 * Converts a kilobyte value to bytes.
 * Example: `4.0.kiBToBytes()` → `4096L`
 */
fun Double.kiBToBytes(): Long = (this * 1024).toLong()
fun Double.miBToBytes(): Long = (this * 1024 * 1024).toLong()
fun Double.giBToBytes(): Long = (this * 1024 * 1024 * 1024).toLong()

/**
 * Returns a storage tier label based on byte size.
 * Example: `500_000_000L.storageTier()` → `"500 MB"`
 */
fun Long.storageTier(): String = bytesToHumanSi(0)

/**
 * Formats a disk usage label with used and total.
 * Example: `(3L * 1024 * 1024 * 1024).diskUsageLabel(10L * 1024 * 1024 * 1024)` → `"3.0 GiB / 10.0 GiB"`
 */
fun Long.diskUsageLabel(total: Long): String =
    "${bytesToHuman()} / ${total.bytesToHuman()}"

/**
 * Returns the usage percentage as a [Double] in [0.0, 100.0].
 * Example: `(3L * 1024).usageRatio(10L * 1024)` → `30.0`
 */
fun Long.usageRatio(total: Long): Double =
    if (total <= 0L) 0.0 else (this.toDouble() / total.toDouble()) * 100.0
