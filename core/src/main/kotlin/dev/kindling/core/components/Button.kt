package dev.kindling.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import dev.kindling.core.components.internal.KindlingPreviewSurface
import dev.kindling.core.components.internal.PreviewLabel

// ─────────────────────────────────────────────
//  Internal helpers
// ─────────────────────────────────────────────

private data class ButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val border: Color? = null
)

@Composable
private fun resolveColors(variant: KButtonVariant): ButtonColors {
    val cs = MaterialTheme.colorScheme
    return when (variant) {
        KButtonVariant.Default -> ButtonColors(
            container         = cs.primary,
            content           = cs.onPrimary,
            disabledContainer = cs.onSurface.copy(alpha = 0.12f),
            disabledContent   = cs.onSurface.copy(alpha = 0.38f)
        )
        KButtonVariant.Destructive -> ButtonColors(
            container         = cs.error,
            content           = cs.onError,
            disabledContainer = cs.onSurface.copy(alpha = 0.12f),
            disabledContent   = cs.onSurface.copy(alpha = 0.38f)
        )
        KButtonVariant.Outline -> ButtonColors(
            container         = Color.Transparent,
            content           = cs.onBackground,
            disabledContainer = Color.Transparent,
            disabledContent   = cs.onSurface.copy(alpha = 0.38f),
            border            = cs.outline
        )
        KButtonVariant.Secondary -> ButtonColors(
            container         = cs.secondaryContainer,
            content           = cs.onSecondaryContainer,
            disabledContainer = cs.onSurface.copy(alpha = 0.12f),
            disabledContent   = cs.onSurface.copy(alpha = 0.38f)
        )
        KButtonVariant.Ghost -> ButtonColors(
            container         = Color.Transparent,
            content           = cs.onBackground,
            disabledContainer = Color.Transparent,
            disabledContent   = cs.onSurface.copy(alpha = 0.38f)
        )
        KButtonVariant.Link -> ButtonColors(
            container         = Color.Transparent,
            content           = cs.primary,
            disabledContainer = Color.Transparent,
            disabledContent   = cs.onSurface.copy(alpha = 0.38f)
        )
    }
}

private data class ButtonDimensions(
    val height: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val fontSize: Float,
    val width: Dp? = null
)

private fun resolveDimensions(size: KButtonSize) = when (size) {
    KButtonSize.Default -> ButtonDimensions(36.dp, 16.dp, 8.dp, 14f)
    KButtonSize.Sm      -> ButtonDimensions(32.dp, 12.dp, 6.dp, 12f)
    KButtonSize.Lg      -> ButtonDimensions(40.dp, 32.dp, 8.dp, 14f)
    KButtonSize.Icon    -> ButtonDimensions(36.dp, 0.dp, 0.dp, 14f, width = 36.dp)
}

// ─────────────────────────────────────────────
//  Main composable
// ─────────────────────────────────────────────

/**
 * Render a shadcn/ui-style button.
 *
 * Colours are resolved from [MaterialTheme.colorScheme], so the component adapts to light
 * and dark palettes automatically.
 *
 * ```kotlin
 * KButton(onClick = { }) { Text("Click me") }
 * KButton(onClick = { }, variant = KButtonVariant.Outline) { Text("Outlined") }
 * KButton(onClick = { }, variant = KButtonVariant.Destructive) { Text("Delete") }
 * KButton(onClick = { }, size = KButtonSize.Icon) { Icon(Icons.Default.Add, null) }
 * ```
 *
 * @param onClick Called when the button is tapped and not disabled or loading.
 * @param modifier Applied to the outermost layout element.
 * @param variant Visual style — see [KButtonVariant].
 * @param size Controls height, padding, and font size — see [KButtonSize].
 * @param enabled When `false`, the button is non-interactive and visually dimmed.
 * @param isLoading When `true`, replaces content with a [CircularProgressIndicator].
 * @param interactionSource Interaction source used for ripple and pressed state.
 * @param content Slot for the button label or icon.
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
    val shape  = RoundedCornerShape(6.dp)

    val border: BorderStroke? = when {
        colors.border != null && enabled  -> BorderStroke(1.dp, colors.border)
        colors.border != null && !enabled -> BorderStroke(1.dp, colors.border.copy(alpha = 0.38f))
        else -> null
    }

    val containerColor = if (enabled) colors.container else colors.disabledContainer
    val contentColor   = if (enabled) colors.content   else colors.disabledContent

    val sizeModifier = if (dims.width != null) modifier.size(dims.width, dims.height)
                       else modifier.height(dims.height)

    Surface(
        onClick          = { if (enabled && !isLoading) onClick() },
        modifier         = sizeModifier,
        enabled          = enabled && !isLoading,
        shape            = shape,
        color            = containerColor,
        contentColor     = contentColor,
        border           = border,
        interactionSource = interactionSource
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = dims.horizontalPadding, vertical = dims.verticalPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(16.dp),
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
 * Render a button with a plain text label.
 *
 * ```kotlin
 * KButton(text = "Save", onClick = { viewModel.save() })
 * ```
 *
 * @param text Label shown inside the button.
 * @param onClick Called when the button is tapped and not disabled or loading.
 * @param modifier Applied to the outermost layout element.
 * @param variant Visual style — see [KButtonVariant].
 * @param size Controls height, padding, and font size — see [KButtonSize].
 * @param enabled When `false`, the button is non-interactive and visually dimmed.
 * @param isLoading When `true`, replaces content with a [CircularProgressIndicator].
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
    KButton(
        onClick   = onClick,
        modifier  = modifier,
        variant   = variant,
        size      = size,
        enabled   = enabled,
        isLoading = isLoading
    ) { Text(text) }
}

@Preview(name = "KButton — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KButton — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKButton() {
    KindlingPreviewSurface {
        PreviewLabel("Variants")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KButton("Default", onClick = { }, modifier = Modifier.weight(1f))
                KButton(
                    "Destructive",
                    onClick = { },
                    variant = KButtonVariant.Destructive,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KButton("Outline", onClick = { }, variant = KButtonVariant.Outline, modifier = Modifier.weight(1f))
                KButton("Secondary", onClick = { }, variant = KButtonVariant.Secondary, modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KButton("Ghost", onClick = { }, variant = KButtonVariant.Ghost, modifier = Modifier.weight(1f))
                KButton("Link", onClick = { }, variant = KButtonVariant.Link, modifier = Modifier.weight(1f))
            }
        }

        PreviewLabel("Sizes")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton("Default", onClick = { }, size = KButtonSize.Default)
            KButton("Small", onClick = { }, size = KButtonSize.Sm)
            KButton("Large", onClick = { }, size = KButtonSize.Lg)
        }

        PreviewLabel("States")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton("Loading", onClick = { }, isLoading = true)
            KButton("Disabled", onClick = { }, enabled = false)
        }

        PreviewLabel("Icon")
        KButton(onClick = { }, size = KButtonSize.Icon) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}
