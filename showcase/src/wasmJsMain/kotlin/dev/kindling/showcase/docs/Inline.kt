package dev.kindling.showcase.docs

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Parses the small inline Markdown subset the generator emits:
 * `code`, **bold**, [label](url) and backslash escapes (\{ \} \< \\).
 * Links go through [onLink]; the caller decides between in-app navigation and opening a new tab.
 */
internal fun parseInline(
    text: String,
    codeBackground: Color,
    linkColor: Color,
    onLink: (String) -> Unit,
): AnnotatedString = buildAnnotatedString {
    var i = 0
    val n = text.length
    fun appendPlain(s: String) = append(s)

    while (i < n) {
        val c = text[i]
        when {
            c == '\\' && i + 1 < n && text[i + 1] in "{}<>\\`*[]()" -> {
                append(text[i + 1])
                i += 2
            }
            c == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end == -1) {
                    append(c); i++
                } else {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, background = codeBackground)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                }
            }
            c == '*' && text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end == -1) {
                    append(c); i++
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(parseInline(text.substring(i + 2, end), codeBackground, linkColor, onLink))
                    }
                    i = end + 2
                }
            }
            c == '[' -> {
                val close = text.indexOf("](", i + 1)
                val urlEnd = if (close == -1) -1 else text.indexOf(')', close + 2)
                if (close == -1 || urlEnd == -1) {
                    append(c); i++
                } else {
                    val label = text.substring(i + 1, close)
                    val url = text.substring(close + 2, urlEnd)
                    val link = LinkAnnotation.Clickable(
                        tag = url,
                        styles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
                        linkInteractionListener = LinkInteractionListener { onLink(url) },
                    )
                    withLink(link) { append(parseInline(label, codeBackground, linkColor, onLink)) }
                    i = urlEnd + 1
                }
            }
            else -> {
                appendPlain(c.toString()); i++
            }
        }
    }
}
