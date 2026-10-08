package dev.kindling.showcase.docs

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle

private val tokenRegex =
    Regex("""(//[^\n]*|/\*[\s\S]*?\*/)|("(?:\\.|[^"\\\n])*")|(@\w+)|\b(\d[\d_.]*[fFLdD]?)\b|\b(\w+)\b""")

private val keywords = setOf(
    "fun", "val", "var", "class", "interface", "object", "data", "enum", "sealed", "abstract", "open", "override",
    "private", "public", "internal", "protected", "import", "package", "return", "if", "else", "when", "for",
    "while", "do", "in", "is", "as", "null", "true", "false", "this", "super", "by", "suspend", "inline",
    "typealias", "companion", "const", "lateinit", "implementation", "plugins", "repositories", "dependencies",
)

/** Very small Kotlin/Gradle syntax highlighter (comments, strings, annotations, numbers, keywords, types). */
internal fun highlight(code: String, dark: Boolean): AnnotatedString {
    val comment = if (dark) Color(0xFF8B949E) else Color(0xFF6A737D)
    val string = if (dark) Color(0xFFA5D6FF) else Color(0xFF0A3069)
    val keyword = if (dark) Color(0xFFFF7B72) else Color(0xFFCF222E)
    val annotation = if (dark) Color(0xFFD2A8FF) else Color(0xFF8250DF)
    val number = if (dark) Color(0xFF79C0FF) else Color(0xFF0550AE)
    val type = if (dark) Color(0xFFFFA657) else Color(0xFF953800)

    return buildAnnotatedString {
        var last = 0
        for (m in tokenRegex.findAll(code)) {
            if (m.range.first > last) append(code.substring(last, m.range.first))
            val text = m.value
            val style = when {
                m.groups[1] != null -> SpanStyle(color = comment, fontStyle = FontStyle.Italic)
                m.groups[2] != null -> SpanStyle(color = string)
                m.groups[3] != null -> SpanStyle(color = annotation)
                m.groups[4] != null -> SpanStyle(color = number)
                text in keywords -> SpanStyle(color = keyword)
                text.first().isUpperCase() -> SpanStyle(color = type)
                else -> null
            }
            if (style == null) append(text) else withStyle(style) { append(text) }
            last = m.range.last + 1
        }
        if (last < code.length) append(code.substring(last))
    }
}
