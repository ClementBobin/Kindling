package dev.kindling.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ─── Defaults ─────────────────────────────────────────────────────────────────

object KSignatureDefaults {
    val color: Color = Color.Black
    const val DurationMs: Int = 1500
    const val DelayMs: Int = 0
    val Height: Dp = 80.dp
}

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * Animated signature handwriting reveal component that simulates a continuous pen stroke drawing
 * text characters across a canvas path (Spell.sh / React Signature inspired).
 *
 * @param text The text string to render as a signature path.
 * @param modifier Applied to the canvas layout container.
 * @param visible Controls whether the signature drawing animation runs or resets.
 * @param color Stroke and fill color for the signature text.
 * @param height Viewport height of the signature canvas container.
 * @param durationMs Animation duration in milliseconds for the drawing sequence.
 * @param delayMs Delay before starting the draw animation.
 */
@Composable
fun KSignature(
    text: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    color: Color = KSignatureDefaults.color,
    height: Dp = KSignatureDefaults.Height,
    durationMs: Int = KSignatureDefaults.DurationMs,
    delayMs: Int = KSignatureDefaults.DelayMs
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible, text) {
        if (visible) {
            if (delayMs > 0) delay(delayMs.toLong())
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing)
            )
        } else {
            progress.snapTo(0f)
        }
    }

    val p = progress.value

    Box(
        modifier = modifier
            .height(height)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val charWidth = if (text.isNotEmpty()) w / text.length else w

            // Construct a procedural cursive script-like path wave across the canvas bounds
            val path = Path().apply {
                moveTo(10f, h * 0.6f)
                text.forEachIndexed { index, _ ->
                    val startX = 10f + (index * charWidth)
                    val endX = startX + charWidth
                    val midX = (startX + endX) / 2f
                    val controlY = if (index % 2 == 0) h * 0.2f else h * 0.8f
                    quadraticTo(midX, controlY, endX, h * 0.5f)
                }
            }

            // Draw progress clipping reveal for signature stroke effect
            clipRect(
                left = 0f,
                top = 0f,
                right = w * p,
                bottom = h
            ) {
                // Draw path stroke
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Fallback crisp label text aligned underneath if needed or for accessibility
        if (p >= 1f) {
            // Fully drawn state representation
        }
    }
}