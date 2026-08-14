package dev.kindling.core.components.ui.maskInput

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.text.iterator

/**
 * Applies a `#`-placeholder [pattern] to [value].
 *
 * Mirrors `applyMask` from `mask-input.tsx`.
 */
fun applyMask(value: String, pattern: String, allowLetters: Boolean = false): String {
    val clean = if (allowLetters)
        value.filter { it.isLetterOrDigit() }.uppercase()
    else
        value.filter { it.isDigit() }

    val sb = StringBuilder()
    var ci = 0
    for (ch in pattern) {
        if (ci >= clean.length) break
        if (ch == '#') sb.append(clean[ci++]) else sb.append(ch)
    }
    return sb.toString()
}

/**
 * Mirrors `applyCurrencyMask` from `mask-input.tsx`.
 *
 * Formats a raw numeric string as a locale-aware currency display value.
 */
fun applyCurrencyMask(
    value: String,
    currencyCode: String = "USD",
    locale: Locale = Locale.getDefault()
): String {
    if (value.isEmpty()) return ""
    val num = value.toDoubleOrNull() ?: return value
    return try {
        val fmt = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
        }
        fmt.format(num)
    } catch (_: Exception) {
        value
    }
}

/**
 * Mirrors `applyPercentageMask` from `mask-input.tsx`.
 */
fun applyPercentageMask(value: String): String {
    if (value.isEmpty()) return ""
    val clean = value.filter { it.isDigit() || it == '.' }
    val parts = clean.split(".")
    val integer = parts[0]
    val decimal = parts.getOrNull(1)?.take(2) ?: ""
    return if (decimal.isNotEmpty()) "$integer.$decimal%" else "$integer%"
}

/**
 * Strips all non-digit (or non-alphanumeric) chars from [value].
 *
 * Mirrors `getUnmaskedValue` from `mask-input.tsx`.
 */
fun getUnmaskedValue(value: String, allowLetters: Boolean = false): String =
    if (allowLetters) value.filter { it.isLetterOrDigit() }
    else              value.filter { it.isDigit() }

/**
 * Converts a caret position in the masked string to an index in the raw string.
 *
 * Mirrors `toUnmaskedIndex` from `mask-input.tsx`.
 */
fun toUnmaskedIndex(masked: String, pattern: String, caret: Int): Int {
    var idx = 0
    for (i in 0 until minOf(caret, masked.length, pattern.length)) {
        if (pattern[i] == '#') idx++
    }
    return idx
}

/**
 * Converts a raw-string index back to a caret position in the masked string.
 *
 * Mirrors `fromUnmaskedIndex` from `mask-input.tsx`.
 */
fun fromUnmaskedIndex(masked: String, pattern: String, unmaskedIndex: Int): Int {
    var seen = 0
    for (i in masked.indices.take(pattern.length)) {
        if (pattern[i] == '#') {
            seen++
            if (seen == unmaskedIndex) return i + 1
        }
    }
    return masked.length
}