package dev.kindling.core.components.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Shadcn/ui-style Label — mirrors `label.tsx`.
 *
 * A specialized text component used for labeling form fields or other UI elements.
 * It automatically applies standard styling and handles the [disabled] state by
 * adjusting its color to match the theme's disabled surface color.
 *
 * Respects [LocalLayoutDirection] for RTL text alignment.
 *
 * ### Example usage:
 * ```kotlin
 * Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
 *     KLabel("Email address")
 *     KInput(value = email, onValueChange = { email = it })
 * }
 * ```
 *
 * @param text The label text to display.
 * @param modifier The modifier to be applied to the text.
 * @param disabled Whether the label should appear in a disabled state.
 * @param style The text style to apply. Defaults to `MaterialTheme.typography.labelLarge` with Kindling adjustments.
 * @param color The text color. Defaults to a color based on the [disabled] state and current theme.
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
    Text(
        text     = text,
        style    = style,
        color    = color,
        modifier = modifier
    )
}