package dev.kindling.core.utils

import kotlin.math.floor

fun Long.bytesToHuman(decimals: Int = 1): String {
    val units = listOf("B", "KiB", "MiB", "GiB", "TiB", "PiB")
    var value = toDouble()
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) {
        value /= 1024.0
        index++
    }
    val rounded = ((value * 10).toInt() / 10.0)
    val fmt = if (value == floor(value)) value.toLong().toString() else rounded.toString()
    return "$fmt ${units[index]}"
}

fun Int.bytesToHuman(decimals: Int = 1): String = toLong().bytesToHuman(decimals)
