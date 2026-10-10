package dev.kindling.showcase.docs.playground

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonSize
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.showcase.docs.highlight
import kotlinx.coroutines.delay

/** Entry point used by the docs block renderer. `id` is `theme` or `utils`. */
@Composable
fun PlaygroundHost(id: String, siteDark: Boolean) {
    when (id) {
        "theme" -> ThemePlayground(siteDark)
        "utils" -> UtilsPlayground(siteDark)
        else -> Text("Unknown playground: $id", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
internal fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    KButton(
        text = text,
        onClick = onClick,
        variant = if (selected) KButtonVariant.Default else KButtonVariant.Outline,
        size = KButtonSize.Sm,
    )
}

@Composable
internal fun ControlLabel(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
    )
}

/** Highlighted, copyable code. Uses the site's colours (not the previewed theme). */
@Composable
internal fun CodePanel(code: String, siteDark: Boolean, title: String = "kotlin", copyLabel: String = "Copy code") {
    val colors = MaterialTheme.colorScheme
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }
    val text = remember(code, siteDark) { highlight(code, siteDark) }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceVariant.copy(alpha = if (siteDark) 0.5f else 0.6f),
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(title, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = colors.onSurfaceVariant)
                KButton(
                    text = if (copied) "Copied" else copyLabel,
                    onClick = {
                        copyToClipboard(code)
                        copied = true
                    },
                    variant = KButtonVariant.Secondary,
                    size = KButtonSize.Sm,
                )
            }
            Box(Modifier.horizontalScroll(rememberScrollState())) {
                Text(
                    text = text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = colors.onBackground,
                    softWrap = false,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
    }
}
