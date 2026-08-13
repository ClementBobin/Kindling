package dev.kindling.core.components.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kindling.core.theme.LocalKindlingShapes

internal data class ButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val border: Color? = null
)

@Composable
internal fun resolveColors(variant: KButtonVariant): ButtonColors {
    val cs = MaterialTheme.colorScheme
    return when (variant) {
        KButtonVariant.Default -> ButtonColors(
            cs.primary, cs.onPrimary,
            cs.onSurface.copy(.12f), cs.onSurface.copy(.38f)
        )
        KButtonVariant.Destructive -> ButtonColors(
            cs.error.copy(.1f), cs.error,
            cs.onSurface.copy(.12f), cs.onSurface.copy(.38f)
        )
        KButtonVariant.Outline -> ButtonColors(
            Color.Transparent, cs.onBackground,
            Color.Transparent, cs.onSurface.copy(.38f), border = cs.outline
        )
        KButtonVariant.Secondary -> ButtonColors(
            cs.secondaryContainer, cs.onSecondaryContainer,
            cs.onSurface.copy(.12f), cs.onSurface.copy(.38f)
        )
        KButtonVariant.Ghost -> ButtonColors(
            Color.Transparent, cs.onBackground,
            Color.Transparent, cs.onSurface.copy(.38f)
        )
        KButtonVariant.Link -> ButtonColors(
            Color.Transparent, cs.primary,
            Color.Transparent, cs.onSurface.copy(.38f)
        )
    }
}

internal data class ButtonDimensions(
    val height: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val fontSize: Float,
    val iconSize: Dp,
    val fixedWidth: Dp? = null
)

internal fun resolveDimensions(size: KButtonSize) = when (size) {
    KButtonSize.Default -> ButtonDimensions(32.dp, 10.dp, 0.dp, 14f, 16.dp)
    KButtonSize.Xs      -> ButtonDimensions(24.dp, 8.dp, 0.dp, 12f, 12.dp)
    KButtonSize.Sm      -> ButtonDimensions(28.dp, 10.dp, 0.dp, 12.8f, 14.dp)
    KButtonSize.Lg      -> ButtonDimensions(36.dp, 10.dp, 0.dp, 14f, 16.dp)
    KButtonSize.Icon    -> ButtonDimensions(32.dp, 0.dp, 0.dp, 14f, 16.dp, fixedWidth = 32.dp)
    KButtonSize.IconXs  -> ButtonDimensions(24.dp, 0.dp, 0.dp, 12f, 12.dp, fixedWidth = 24.dp)
    KButtonSize.IconSm  -> ButtonDimensions(28.dp, 0.dp, 0.dp, 12f, 14.dp, fixedWidth = 28.dp)
    KButtonSize.IconLg  -> ButtonDimensions(36.dp, 0.dp, 0.dp, 14f, 16.dp, fixedWidth = 36.dp)
}

@Composable
internal fun resolveShape(size: KButtonSize): Shape {
    val shapes = LocalKindlingShapes.current
    return when (size) {
        KButtonSize.Xs, KButtonSize.IconXs ->
            RoundedCornerShape(minOf(shapes.roundedMd, 10.dp))
        KButtonSize.Sm, KButtonSize.IconSm ->
            RoundedCornerShape(minOf(shapes.roundedMd, 12.dp))
        else ->
            shapes.radiusLg
    }
}