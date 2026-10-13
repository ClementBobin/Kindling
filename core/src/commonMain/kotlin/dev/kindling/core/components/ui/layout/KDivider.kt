package dev.kindling.core.components.ui.layout

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Horizontal rule whose [thickness] comes from [dev.kindling.core.theme.KindlingShapes].
 *
 * A zero [thickness] renders nothing but the [modifier]'s own layout (so padding or
 * alignment still apply), instead of the one-pixel hairline that
 * [HorizontalDivider] draws for `0.dp`.
 */
@Composable
internal fun KDivider(
    color: Color,
    thickness: Dp,
    modifier: Modifier = Modifier,
) {
    if (thickness > 0.dp) {
        HorizontalDivider(modifier = modifier, thickness = thickness, color = color)
    } else {
        Spacer(modifier)
    }
}
