package dev.kindling.library.utils.method.format.text

// ─── Initials ─────────────────────────────────────────────────────────────────

/**
 * Extracts initials from a full name string.
 * Example: `"Jean-Claude Van Damme".toInitials()` → `"JVD"`
 */
fun String.toInitials(maxLetters: Int = 3): String =
    trim()
        .split(Regex("[\\s\\-]+"))
        .filter { it.isNotBlank() }
        .take(maxLetters)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

/**
 * Extracts exactly the first and last initials from a full name.
 * Example: `"Jean-Claude Van Damme".toFirstLastInitials()` → `"JD"`
 */
fun String.toFirstLastInitials(): String {
    val parts = trim().split(Regex("[\\s\\-]+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return ""
    val first = parts.first().firstOrNull()?.uppercaseChar() ?: return ""
    val last  = parts.last().firstOrNull()?.uppercaseChar()
    return if (last != null && parts.size > 1) "$first$last" else "$first"
}

/**
 * Converts a [String] to title case.
 * Example: `"hello world".toTitleCase()` → `"Hello World"`
 */
fun String.toTitleCase(): String =
    split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }

/**
 * Converts a camelCase or PascalCase string to a human-readable label.
 * Example: `"firstName".camelToTitle()` → `"First Name"`
 */
fun String.camelToTitle(): String =
    replace(Regex("([A-Z])")) { " ${it.value}" }
        .trim()
        .replaceFirstChar { it.uppercase() }

/**
 * Converts a snake_case string to a human-readable label.
 * Example: `"first_name".snakeToTitle()` → `"First Name"`
 */
fun String.snakeToTitle(): String =
    split("_").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

/**
 * Converts a kebab-case string to a human-readable label.
 * Example: `"first-name".kebabToTitle()` → `"First Name"`
 */
fun String.kebabToTitle(): String =
    split("-").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

/**
 * Converts any string to snake_case.
 * Example: `"Hello World".toSnakeCase()` → `"hello_world"`
 */
fun String.toSnakeCase(): String =
    trim()
        .replace(Regex("([a-z])([A-Z])"), "$1_$2")
        .replace(Regex("[\\s\\-]+"), "_")
        .lowercase()

/**
 * Converts any string to kebab-case.
 * Example: `"Hello World".toKebabCase()` → `"hello-world"`
 */
fun String.toKebabCase(): String = toSnakeCase().replace('_', '-')

/**
 * Converts any string to camelCase.
 * Example: `"hello world".toCamelCase()` → `"helloWorld"`
 */
fun String.toCamelCase(): String {
    val parts = trim().split(Regex("[\\s_\\-]+"))
    return parts.first().lowercase() +
           parts.drop(1).joinToString("") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
}
