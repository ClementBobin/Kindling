package dev.kindling.core.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Variants & Sizes  (mirrors shadcn/ui)
// ─────────────────────────────────────────────

enum class KButtonVariant {
    Default, Destructive, Outline, Secondary, Ghost, Link
}

enum class KButtonSize {
    Default, Sm, Lg, Icon
}

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
 * Shadcn/ui-style Button for Jetpack Compose.
 *
 * Colours are resolved entirely from [MaterialTheme.colorScheme], so this
 * component works with any light or dark colour scheme out of the box.
 *
 * ```kotlin
 * KButton(onClick = { }) { Text("Click me") }
 * KButton(onClick = { }, variant = KButtonVariant.Outline) { Text("Outlined") }
 * KButton(onClick = { }, variant = KButtonVariant.Destructive) { Text("Delete") }
 * KButton(onClick = { }, size = KButtonSize.Icon) { Icon(Icons.Default.Add, null) }
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

/** Convenience overload that accepts a plain text label. */
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
