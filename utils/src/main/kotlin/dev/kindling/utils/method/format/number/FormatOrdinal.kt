package dev.kindling.mat.number

// ─── FormatOrdinal ────────────────────────────────────────────────────────────

/**
 * Returns the English ordinal suffix for an [Int] (`"st"`, `"nd"`, `"rd"`, `"th"`).
 * Example: `1.ordinalSuffix()` → `"st"`
 * Example: `11.ordinalSuffix()` → `"th"`
 */
fun Int.ordinalSuffix(): String {
    val abs = kotlin.math.abs(this)
    return when {
        abs % 100 in 11..13 -> "th"
        abs % 10 == 1        -> "st"
        abs % 10 == 2        -> "nd"
        abs % 10 == 3        -> "rd"
        else                 -> "th"
    }
}

/**
 * Returns the full ordinal string for an [Int].
 * Example: `1.toOrdinal()` → `"1st"`
 * Example: `22.toOrdinal()` → `"22nd"`
 * Example: `113.toOrdinal()` → `"113th"`
 */
fun Int.toOrdinal(): String = "$this${ordinalSuffix()}"

/**
 * Returns the written-out ordinal word for numbers 1–20.
 * Falls back to numeric ordinal (e.g. `"21st"`) for larger values.
 * Example: `3.toOrdinalWord()` → `"third"`
 * Example: `25.toOrdinalWord()` → `"25th"`
 */
fun Int.toOrdinalWord(): String {
    val words = mapOf(
        1 to "first", 2 to "second", 3 to "third", 4 to "fourth",
        5 to "fifth", 6 to "sixth", 7 to "seventh", 8 to "eighth",
        9 to "ninth", 10 to "tenth", 11 to "eleventh", 12 to "twelfth",
        13 to "thirteenth", 14 to "fourteenth", 15 to "fifteenth",
        16 to "sixteenth", 17 to "seventeenth", 18 to "eighteenth",
        19 to "nineteenth", 20 to "twentieth"
    )
    return words[this] ?: toOrdinal()
}

/**
 * Converts an [Int] to its spelled-out English word.
 * Supports -999 to 999.
 * Example: `42.toWord()` → `"forty-two"`
 */
fun Int.toWord(): String {
    require(this in -999..999) { "Word conversion only supports -999 to 999" }
    if (this == 0) return "zero"
    if (this < 0) return "negative ${(-this).toWord()}"
    val ones = listOf("", "one", "two", "three", "four", "five", "six",
        "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen",
        "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen")
    val tens = listOf("", "", "twenty", "thirty", "forty", "fifty",
        "sixty", "seventy", "eighty", "ninety")
    return when {
        this < 20  -> ones[this]
        this < 100 -> tens[this / 10] + if (this % 10 != 0) "-${ones[this % 10]}" else ""
        else       -> "${ones[this / 100]} hundred" +
                      if (this % 100 != 0) " ${(this % 100).toWord()}" else ""
    }
}
