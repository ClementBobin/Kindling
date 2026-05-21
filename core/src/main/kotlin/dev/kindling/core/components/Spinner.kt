package dev.kindling.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import dev.kindling.core.components.internal.KindlingPreviewSurface
import dev.kindling.core.components.internal.PreviewLabel

private fun KSpinnerSize.toDp(): Dp = when (this) {
    KSpinnerSize.Sm      -> 16.dp
    KSpinnerSize.Default -> 24.dp
    KSpinnerSize.Lg      -> 32.dp
    KSpinnerSize.Xl      -> 48.dp
}

private fun KSpinnerSize.strokeWidth(): Dp = when (this) {
    KSpinnerSize.Sm      -> 2.dp
    KSpinnerSize.Default -> 2.5.dp
    KSpinnerSize.Lg      -> 3.dp
    KSpinnerSize.Xl      -> 4.dp
}

/**
 * Render a shadcn/ui-style spinner with a smooth rotating arc.
 *
 * ```kotlin
 * KSpinner()
 * KSpinner(size = KSpinnerSize.Lg, label = "Loading…")
 * ```
 *
 * @param modifier Applied to the outermost layout element.
 * @param size Size preset for the spinner.
 * @param color Foreground arc colour.
 * @param trackColor Background arc colour.
 * @param label Optional label shown beneath the spinner.
 */
@Composable
fun KSpinner(
    modifier: Modifier = Modifier,
    size: KSpinnerSize = KSpinnerSize.Default,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.15f),
    label: String? = null,
) {
    val dp       = size.toDp()
    val strokeDp = size.strokeWidth()

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

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(dp)) {
            val stroke = Stroke(width = strokeDp.toPx(), cap = StrokeCap.Round)
            val inset  = strokeDp.toPx() / 2f
            
            // 'this.size' is DrawScope.size (pixels), renamed to avoid clash with KSpinnerSize param
            val canvasSize = this.size
            val arcSize = Size(
                width  = canvasSize.width  - strokeDp.toPx(),
                height = canvasSize.height - strokeDp.toPx()
            )
            val topLeft = Offset(inset, inset)

            drawArc(trackColor, 0f, 360f, false, style = stroke, topLeft = topLeft, size = arcSize)
            drawArc(color, angle, 80f, false, style = stroke, topLeft = topLeft, size = arcSize)
        }

        if (label != null) {
            Text(
                text  = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(name = "KSpinner — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KSpinner — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKSpinner() {
    KindlingPreviewSurface {
        PreviewLabel("Sizes")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KSpinner(size = KSpinnerSize.Sm)
            KSpinner(size = KSpinnerSize.Default)
            KSpinner(size = KSpinnerSize.Lg)
            KSpinner(size = KSpinnerSize.Xl)
        }

        PreviewLabel("With label")
        KSpinner(label = "Loading…")

        PreviewLabel("Custom error colour")
        KSpinner(color = MaterialTheme.colorScheme.error)
    }
}
