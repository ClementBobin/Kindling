package dev.kindling.library.utils.method.format.text

// ─── TruncateText ─────────────────────────────────────────────────────────────

/**
 * Truncates a [String] to [maxLength] characters, appending [ellipsis] if truncated.
 * Example: `"Hello, World!".truncate(8)` → `"Hello..."`
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    if (length <= maxLength) return this
    val cutAt = (maxLength - ellipsis.length).coerceAtLeast(0)
    return take(cutAt) + ellipsis
}

/**
 * Truncates a [String] at the nearest word boundary within [maxLength].
 * Example: `"Hello beautiful world".truncateWords(14)` → `"Hello..."`
 */
fun String.truncateWords(maxLength: Int, ellipsis: String = "..."): String {
    if (length <= maxLength) return this
    val limit = maxLength - ellipsis.length
    val cut   = lastIndexOf(' ', limit).takeIf { it > 0 } ?: limit
    return take(cut) + ellipsis
}

/**
 * Truncates a [String] in the middle, preserving start and end.
 * Example: `"abcdefghij".truncateMiddle(7)` → `"ab...ij"`
 */
fun String.truncateMiddle(maxLength: Int, ellipsis: String = "..."): String {
    if (length <= maxLength) return this
    val keepTotal = (maxLength - ellipsis.length).coerceAtLeast(0)
    val start     = keepTotal / 2 + keepTotal % 2
    val end       = keepTotal / 2
    return take(start) + ellipsis + takeLast(end)
}

/**
 * Truncates to [maxLines] lines, appending [ellipsis] on the last line if truncated.
 * Example: `"a\nb\nc\nd".truncateLines(2)` → `"a\nb..."`
 */
fun String.truncateLines(maxLines: Int, ellipsis: String = "..."): String {
    val lines = lines()
    if (lines.size <= maxLines) return this
    return lines.take(maxLines).joinToString("\n").trimEnd() + ellipsis
}

/**
 * Collapses multiple consecutive whitespace characters to a single space.
 * Example: `"hello   world\t!".collapseWhitespace()` → `"hello world !"`
 */
fun String.collapseWhitespace(): String = replace(Regex("\\s+"), " ").trim()

/**
 * Wraps a [String] to [lineWidth] characters, breaking at spaces.
 * Example: `"Hello world foo bar".wordWrap(10)` → `"Hello\nworld foo\nbar"`
 */
fun String.wordWrap(lineWidth: Int): String {
    val words   = split(" ")
    val lines   = mutableListOf<String>()
    var current = StringBuilder()
    for (word in words) {
        if (current.isNotEmpty() && current.length + 1 + word.length > lineWidth) {
            lines += current.toString()
            current = StringBuilder(word)
        } else {
            if (current.isNotEmpty()) current.append(' ')
            current.append(word)
        }
    }
    if (current.isNotEmpty()) lines += current.toString()
    return lines.joinToString("\n")
}
