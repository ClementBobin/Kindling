package dev.kindling.core.components.ui.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws a dashed border around a composable using custom stroke parameters.
 */
fun Modifier.dashedBorder(
    color: Color,
    shape: Shape,
    strokeWidth: Dp = 1.dp,
    dashWidth: Dp = 6.dp,
    gapWidth: Dp = 4.dp
): Modifier = this.drawWithContent {
    val strokeWidthPx = strokeWidth.toPx()
    val dashWidthPx = dashWidth.toPx()
    val gapWidthPx = gapWidth.toPx()

    val outline = shape.createOutline(size, layoutDirection, this)
    val path = Path().apply {
        when (outline) {
            is Outline.Rectangle -> addRect(outline.rect)
            is Outline.Rounded   -> addRoundRect(outline.roundRect)
            is Outline.Generic   -> addPath(outline.path)
        }
    }

    drawContent()
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashWidthPx, gapWidthPx),
                0f
            )
        )
    )
}