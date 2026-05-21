package dev.kindling.core.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render a shadcn/ui-style label.
 *
 * A medium-weight text element typically placed above form fields that dims itself when
 * [disabled] is `true`.
 *
 * ```kotlin
 * KLabel("Email address")
 * KLabel("Password", disabled = true)
 * ```
 *
 * @param text Label text to display.
 * @param modifier Applied to the text layout.
 * @param disabled When `true`, reduces contrast for a disabled appearance.
 * @param style Text style applied to the label.
 * @param color Colour applied to the text.
 */
@Composable
fun KLabel(
    text: String,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    style: TextStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize      = 14.sp,
        fontWeight    = FontWeight.Medium,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    color: Color = if (disabled)
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    else
        MaterialTheme.colorScheme.onBackground
) {
    Text(text = text, style = style, color = color, modifier = modifier)
}

@Preview(name = "KLabel — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KLabel — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKLabel() {
    KindlingPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KLabel("Default label")
            KLabel("Disabled label", disabled = true)
        }
    }
}
