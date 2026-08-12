package dev.kindling.core.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import dev.kindling.core.theme.LocalKindlingShapes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        KButtonVariant.Default     -> ButtonColors(cs.primary, cs.onPrimary,
            cs.onSurface.copy(.12f), cs.onSurface.copy(.38f))
        KButtonVariant.Destructive -> ButtonColors(cs.error.copy(.1f), cs.error,
            cs.onSurface.copy(.12f), cs.onSurface.copy(.38f))
        KButtonVariant.Outline     -> ButtonColors(Color.Transparent, cs.onBackground,
            Color.Transparent, cs.onSurface.copy(.38f), border = cs.outline)
        KButtonVariant.Secondary   -> ButtonColors(cs.secondaryContainer, cs.onSecondaryContainer,
            cs.onSurface.copy(.12f), cs.onSurface.copy(.38f))
        KButtonVariant.Ghost       -> ButtonColors(Color.Transparent, cs.onBackground,
            Color.Transparent, cs.onSurface.copy(.38f))
        KButtonVariant.Link        -> ButtonColors(Color.Transparent, cs.primary,
            Color.Transparent, cs.onSurface.copy(.38f))
    }
}

private data class ButtonDimensions(
    val height: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val fontSize: Float,
    val iconSize: Dp,
    val fixedWidth: Dp? = null
)

// Note: shape need to change based on size .
private fun resolveDimensions(size: KButtonSize) = when (size) {
    KButtonSize.Default -> ButtonDimensions(32.dp, 10.dp, 0.dp, 14f, 16.dp)
    KButtonSize.Xs      -> ButtonDimensions(24.dp, 8.dp,  0.dp, 12f, 12.dp)
    KButtonSize.Sm      -> ButtonDimensions(28.dp, 10.dp, 0.dp, 12.8f, 14.dp)
    KButtonSize.Lg      -> ButtonDimensions(36.dp, 10.dp, 0.dp, 14f, 16.dp)
    KButtonSize.Icon    -> ButtonDimensions(32.dp, 0.dp,  0.dp, 14f, 16.dp, fixedWidth = 32.dp)
    KButtonSize.IconXs  -> ButtonDimensions(24.dp, 0.dp,  0.dp, 12f, 12.dp, fixedWidth = 24.dp)
    KButtonSize.IconSm  -> ButtonDimensions(28.dp, 0.dp,  0.dp, 12f, 14.dp, fixedWidth = 28.dp)
    KButtonSize.IconLg  -> ButtonDimensions(36.dp, 0.dp,  0.dp, 14f, 16.dp, fixedWidth = 36.dp)
}

@Composable
private fun resolveShape(size: KButtonSize): androidx.compose.ui.graphics.Shape {
    val shapes = LocalKindlingShapes.current
    return when (size) {
        KButtonSize.Xs, KButtonSize.IconXs ->
            // min(radiusMd, 10dp)
            RoundedCornerShape(minOf(shapes.roundedMd, 10.dp))
        KButtonSize.Sm, KButtonSize.IconSm ->
            // min(radiusMd, 12dp)
            RoundedCornerShape(minOf(shapes.roundedMd, 12.dp))
        else ->
            shapes.radiusLg
    }
}

// ─────────────────────────────────────────────
//  KButton
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Button.
 *
 * Respects [LocalLayoutDirection] automatically — icon/content order is
 * never reversed programmatically; rely on Compose's RTL mirroring.
 *
 * ```kotlin
 * KButton(onClick = {}) { Text("Save") }
 * KButton(onClick = {}, variant = KButtonVariant.Outline, size = KButtonSize.Sm) { Text("Cancel") }
 * KButton(onClick = {}, size = KButtonSize.Icon) { Icon(Icons.Default.Add, null) }
 * ```
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

/** Convenience overload accepting a plain text label. */
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