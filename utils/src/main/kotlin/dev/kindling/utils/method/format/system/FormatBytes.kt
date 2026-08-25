@file:Suppress("TooManyFunctions")

package dev.kindling.utils.method.format.system

import java.util.Base64  // ← remplace android.util.Base64

// ─── FormatBytes ──────────────────────────────────────────────────────────────

fun Long.bytesToHuman(decimals: Int = 1): String {
    val units = listOf("B", "KiB", "MiB", "GiB", "TiB", "PiB")
    var value = toDouble()
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) {
        value /= 1024.0
        index++
    }
    val fmt = if (value == kotlin.math.floor(value)) value.toLong().toString()
    else "%.${decimals}f".format(value)
    return "$fmt ${units[index]}"
}

fun Int.bytesToHuman(decimals: Int = 1): String = toLong().bytesToHuman(decimals)

fun Long.bytesToHumanSi(decimals: Int = 1): String {
    val units = listOf("B", "KB", "MB", "GB", "TB", "PB")
    var value = toDouble()
    var index = 0
    while (value >= 1000.0 && index < units.lastIndex) {
        value /= 1000.0
        index++
    }
    val fmt = if (value == kotlin.math.floor(value)) value.toLong().toString()
    else "%.${decimals}f".format(value)
    return "$fmt ${units[index]}"
}

fun Long.toKiB(): Double = this / 1024.0
fun Long.toMiB(): Double = this / (1024.0 * 1024.0)
fun Long.toGiB(): Double = this / (1024.0 * 1024.0 * 1024.0)
fun Long.toTiB(): Double = this / (1024.0 * 1024.0 * 1024.0 * 1024.0)

fun Double.kiBToBytes(): Long = (this * 1024).toLong()
fun Double.miBToBytes(): Long = (this * 1024 * 1024).toLong()
fun Double.giBToBytes(): Long = (this * 1024 * 1024 * 1024).toLong()

fun Long.storageTier(): String = bytesToHumanSi(0)

fun Long.diskUsageLabel(total: Long): String =
    "${bytesToHuman()} / ${total.bytesToHuman()}"

fun Long.usageRatio(total: Long): Double =
    if (total <= 0L) 0.0 else (this.toDouble() / total.toDouble()) * 100.0

/**
 * Encodes a [ByteArray] to a Base64 string (no line wrapping).
 */
fun ByteArray.toBase64(): String =
    Base64.getEncoder().withoutPadding().encodeToString(this)   // ← java.util.Base64

/**
 * Decodes a Base64 string back to a [ByteArray].
 */
fun String.fromBase64(): ByteArray =
    Base64.getDecoder().decode(this)                             // ← java.util.Base64

/**
 * Converts an [Int] to a 4-byte big-endian [ByteArray].
 */
fun Int.toBytesBigEndian(): ByteArray = byteArrayOf(
    (this shr 24).toByte(),
    (this shr 16).toByte(),
    (this shr  8).toByte(),
    (this        ).toByte()
)

/**
 * Reads a 4-byte big-endian [ByteArray] back to an [Int].
 */
fun ByteArray.fromBytesBigEndian(): Int =
    ((this[0].toInt() and 0xFF) shl 24) or
    ((this[1].toInt() and 0xFF) shl 16) or
    ((this[2].toInt() and 0xFF) shl  8) or
    ((this[3].toInt() and 0xFF)       )