package dev.kindling.core.components.ui.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import dev.kindling.core.theme.LocalKindlingShapes
import dev.kindling.core.theme.kindlingBorder

/**
 * Focus-aware outline for text fields.
 *
 * Material's `OutlinedTextField` hardcodes its border thickness (1 dp resting, 2 dp
 * focused), so Kindling fields make Material's own border transparent and draw this one
 * instead.  Widths follow [dev.kindling.core.theme.KindlingShapes.borderWidth]
 * (resting) and `borderWidthThick` (focused).  Colour precedence matches Material:
 * disabled, then error, then focused, then resting.
 */
@Composable
internal fun Modifier.kindlingFieldBorder(
    interactionSource: InteractionSource,
    shape: Shape,
    enabled: Boolean = true,
    isError: Boolean = false,
    focusedColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedColor: Color = MaterialTheme.colorScheme.outline,
    disabledColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
    errorColor: Color = MaterialTheme.colorScheme.error,
): Modifier {
    val shapes = LocalKindlingShapes.current
    val focused by interactionSource.collectIsFocusedAsState()

    val color by animateColorAsState(
        targetValue = when {
            !enabled -> disabledColor
            isError  -> errorColor
            focused  -> focusedColor
            else     -> unfocusedColor
        },
        label = "kindlingFieldBorderColor",
    )
    val width by animateDpAsState(
        targetValue = if (focused) shapes.borderWidthThick else shapes.borderWidth,
        label = "kindlingFieldBorderWidth",
    )
    return this.kindlingBorder(width, color, shape)
}
