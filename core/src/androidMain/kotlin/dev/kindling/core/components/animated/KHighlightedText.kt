package dev.kindling.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ─── Enums & Defaults ────────────────────────────────────────────────────────

enum class KHighlightStyle {
    /** Smooth rectangular background marker wipe. */
    BACKGROUND,

    /** Animated organic underline beneath the text. */
    UNDERLINE,

    /** Playful freehand sketchy box encircling the text. */
    CIRCLE
}

object KHighlightedTextDefaults {
    val HighlightColor: Color = Color(0xFFFEF08A) // Soft yellow marker accent
    val UnderlineColor: Color = Color(0xFFF59E0B) // Amber accent

    const val DurationMs: Int = 800
    const val DelayMs: Int = 200
    val Thickness: Dp = 3.dp
    val PaddingHorizontal: Dp = 4.dp
    val PaddingVertical: Dp = 2.dp
}

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * Text component featuring animated highlighter wipe effects (background marker,
 * underline sweep, or sketchy encircled loop) inspired by Spell.sh.
 *
 * @param text The string to render and highlight.
 * @param modifier Applied to the layout container.
 * @param visible Triggers or resets the highlight drawing animation.
 * @param style Visual highlight style ([KHighlightStyle.BACKGROUND], [KHighlightStyle.UNDERLINE], or [KHighlightStyle.CIRCLE]).
 * @param textStyle Typography style applied to the text.
 * @param highlightColor Color of the marker fill or vector line stroke.
 * @param textColor Custom text color override.
 * @param strokeWidth Thickness of underline or circle strokes.
 * @param paddingHorizontal Horizontal padding padding around background highlights.
 * @param paddingVertical Vertical padding around background highlights.
 * @param durationMs Animation duration in milliseconds.
 * @param delayMs Delay before starting the highlight drawing animation.
 */
@Composable
fun KHighlightedText(
    text: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    style: KHighlightStyle = KHighlightStyle.BACKGROUND,
    textStyle: TextStyle = LocalTextStyle.current,
    highlightColor: Color = KHighlightedTextDefaults.HighlightColor,
    textColor: Color = Color.Unspecified,
    strokeWidth: Dp = KHighlightedTextDefaults.Thickness,
    paddingHorizontal: Dp = KHighlightedTextDefaults.PaddingHorizontal,
    paddingVertical: Dp = KHighlightedTextDefaults.PaddingVertical,
    durationMs: Int = KHighlightedTextDefaults.DurationMs,
    delayMs: Int = KHighlightedTextDefaults.DelayMs
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs.toLong())
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing)
            )
        } else {
            animProgress.snapTo(0f)
        }
    }

    val progress = animProgress.value

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Highlight Canvas Layer (rendered behind text)
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            when (style) {
                KHighlightStyle.BACKGROUND -> {
                    val padX = paddingHorizontal.toPx()
                    val padY = paddingVertical.toPx()

                    val targetWidth = (w + (padX * 2)) * progress

                    drawRoundRect(
                        color = highlightColor,
                        topLeft = Offset(-padX, -padY),
                        size = Size(targetWidth, h + (padY * 2)),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }

                KHighlightStyle.UNDERLINE -> {
                    val strokePx = strokeWidth.toPx()
                    val startY = h + 2.dp.toPx()

                    val path = Path().apply {
                        moveTo(0f, startY)
                        quadraticTo(
                            w * 0.5f, startY + (3.dp.toPx() * progress),
                            w * progress, startY
                        )
                    }

                    drawPath(
                        path = path,
                        color = highlightColor,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }

                KHighlightStyle.CIRCLE -> {
                    val strokePx = strokeWidth.toPx()
                    val padX = paddingHorizontal.toPx()
                    val padY = paddingVertical.toPx()

                    val path = Path().apply {
                        val rectX = -padX
                        val rectY = -padY
                        val rectW = w + (padX * 2)
                        val rectH = h + (padY * 2)

                        moveTo(rectX + (rectW * 0.5f), rectY)

                        if (progress > 0.25f) {
                            quadraticTo(
                                rectX + rectW, rectY,
                                rectX + rectW, rectY + (rectH * 0.5f)
                            )
                        }
                        if (progress > 0.50f) {
                            quadraticTo(
                                rectX + rectW, rectY + rectH,
                                rectX + (rectW * 0.5f), rectY + rectH
                            )
                        }
                        if (progress > 0.75f) {
                            quadraticTo(
                                rectX, rectY + rectH,
                                rectX, rectY + (rectH * 0.5f)
                            )
                        }
                        if (progress >= 1.0f) {
                            quadraticTo(
                                rectX, rectY,
                                rectX + (rectW * 0.5f), rectY
                            )
                        }
                    }

                    drawPath(
                        path = path,
                        color = highlightColor,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Foreground Text Layer
        Text(
            text = text,
            style = textStyle.copy(
                color = if (textColor != Color.Unspecified) textColor else textStyle.color
            )
        )
    }
}