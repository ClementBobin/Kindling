package dev.kindling.core.components.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Spinner
// ─────────────────────────────────────────────

private val KSpinnerSize.sizeDp: Dp get() = when (this) {
    KSpinnerSize.Sm      -> 16.dp
    KSpinnerSize.Default -> 24.dp
    KSpinnerSize.Lg      -> 32.dp
    KSpinnerSize.Xl      -> 48.dp
}

private val KSpinnerSize.strokeDp: Dp get() = when (this) {
    KSpinnerSize.Sm      -> 2.dp
    KSpinnerSize.Default -> 2.5.dp
    KSpinnerSize.Lg      -> 3.dp
    KSpinnerSize.Xl      -> 4.dp
}

/**
 * Shadcn/ui-style Spinner — mirrors `spinner.tsx`.
 *
 * ```kotlin
 * Spinner()
 * Spinner(size = KSpinnerSize.Lg)
 * ```
 */
@Composable
fun Spinner(
    modifier: Modifier = Modifier,
    size: KSpinnerSize = KSpinnerSize.Default,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.15f)
) {
    val infinite = rememberInfiniteTransition(label = "spinner")
    val angle by infinite.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Canvas(modifier = modifier.size(size.sizeDp)) {
        val stroke = Stroke(width = size.strokeDp.toPx(), cap = StrokeCap.Round)
        val inset  = size.strokeDp.toPx() / 2f
        val arcSize = Size(this.size.width - size.strokeDp.toPx(), this.size.height - size.strokeDp.toPx())
        val topLeft = Offset(inset, inset)

        drawArc(trackColor, 0f, 360f, false, style = stroke, topLeft = topLeft, size = arcSize)
        drawArc(color, angle, 80f, false, style = stroke, topLeft = topLeft, size = arcSize)
    }
}