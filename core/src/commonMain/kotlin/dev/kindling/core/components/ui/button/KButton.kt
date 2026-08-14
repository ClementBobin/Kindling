package dev.kindling.core.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shadcn/ui-style Button — mirrors `button.tsx`.
 *
 * A versatile button component that supports various visual variants, sizes, and states
 * (including a loading state). It adheres to the Kindling design system and ensures
 * proper accessibility and RTL support.
 *
 * Respects [LocalLayoutDirection] automatically — icon/content order is
 * never reversed programmatically; rely on Compose's RTL mirroring.
 *
 * ### Example usage:
 * ```kotlin
 * // Primary action
 * KButton(onClick = { /* Save */ }) {
 *     Text("Save")
 * }
 *
 * // Outline variant with small size
 * KButton(
 *     onClick = { /* Cancel */ },
 *     variant = KButtonVariant.Outline,
 *     size = KButtonSize.Sm
 * ) {
 *     Text("Cancel")
 * }
 *
 * // Loading state
 * KButton(onClick = {}, isLoading = true) {
 *     Text("Logging in...")
 * }
 * ```
 *
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param variant The visual style variant of the button. Defaults to [KButtonVariant.Default].
 * @param size The size variant of the button. Defaults to [KButtonSize.Default].
 * @param enabled Whether the button is enabled for interaction.
 * @param isLoading Whether to show a loading indicator instead of the content.
 * @param interactionSource The [MutableInteractionSource] to represent the stream of interactions.
 * @param content The composable content to display inside the button (typically text or an icon).
 */
@Composable
fun KButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KButtonVariant = KButtonVariant.Default,
    size: KButtonSize = KButtonSize.Default,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val colors = resolveColors(variant)
    val dims   = resolveDimensions(size)
    val shape = resolveShape(size)

    val border: BorderStroke? = when {
        colors.border != null && enabled  -> BorderStroke(1.dp, colors.border)
        colors.border != null && !enabled -> BorderStroke(1.dp, colors.border.copy(alpha = .38f))
        else -> null
    }

    val containerColor = if (enabled) colors.container else colors.disabledContainer
    val contentColor   = if (enabled) colors.content   else colors.disabledContent

    val sizeMod = if (dims.fixedWidth != null)
        modifier.size(dims.fixedWidth, dims.height)
    else
        modifier.height(dims.height)

    Surface(
        onClick           = { if (enabled && !isLoading) onClick() },
        modifier          = sizeMod,
        enabled           = enabled && !isLoading,
        shape             = shape,
        color             = containerColor,
        contentColor      = contentColor,
        border            = border,
        interactionSource = interactionSource
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = dims.horizontalPadding, vertical = dims.verticalPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(dims.iconSize),
                    color       = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                ProvideTextStyle(
                    MaterialTheme.typography.labelLarge.copy(
                        fontSize   = dims.fontSize.sp,
                        fontWeight = FontWeight.Medium
                    )
                ) { content() }
            }
        }
    }
}

/**
 * A convenience overload of [KButton] that accepts a plain string as a label.
 *
 * ### Example usage:
 * ```kotlin
 * KButton(text = "Submit", onClick = { /* Submit form */ })
 * ```
 *
 * @param text The label text to display inside the button.
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param variant The visual style variant of the button.
 * @param size The size variant of the button.
 * @param enabled Whether the button is enabled for interaction.
 * @param isLoading Whether to show a loading indicator instead of the content.
 */
@Composable
fun KButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KButtonVariant = KButtonVariant.Default,
    size: KButtonSize = KButtonSize.Default,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    KButton(onClick, modifier, variant, size, enabled, isLoading) { Text(text) }
}