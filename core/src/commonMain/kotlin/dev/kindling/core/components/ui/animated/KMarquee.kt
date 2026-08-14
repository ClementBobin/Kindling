package dev.kindling.core.components.ui.animated

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

// ─── Enums & Configuration ───────────────────────────────────────────────────

enum class KMarqueeDirection {
    FORWARD,  // Right-to-Left (Default continuous ticker)
    BACKWARD  // Left-to-Right
}

object KMarqueeDefaults {
    val Spacing: Dp = 16.dp
    val Velocity: Dp = 40.dp // Speed in Dp per second
    val FadeWidth: Dp = 24.dp
}

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * A seamless infinite scrolling marquee container.
 *
 * This component scrolls its content continuously in a specified direction.
 * It is ideal for tickers, news feeds, or showcasing a list of logos/items
 * that should loop forever.
 *
 * ### Example usage:
 * ```kotlin
 * KMarquee(
 *     direction = KMarqueeDirection.FORWARD,
 *     velocity = 40.dp,
 *     spacing = 20.dp
 * ) {
 *     Row(verticalAlignment = Alignment.CenterVertically) {
 *         repeat(10) { index ->
 *             Text("Item $index", modifier = Modifier.padding(horizontal = 8.dp))
 *             Icon(Icons.Default.Star, null)
 *         }
 *     }
 * }
 * ```
 *
 * @param modifier Applied to the outer layout wrapper.
 * @param direction Controls the scroll direction ([KMarqueeDirection.FORWARD] or [KMarqueeDirection.BACKWARD]).
 * @param velocity Base speed in Dp per second.
 * @param spacing Minimum gap between duplicate items during loop.
 * @param repeatCount Number of item duplicates to render to guarantee continuous coverage.
 * @param pauseOnTouch Pauses animation when user holds down/taps the component.
 * @param enableFadeEdges Renders soft gradient fades at start and end edges.
 * @param fadeColor Color for the edge fade gradients (typically matches screen background).
 * @param fadeWidth Width of the edge fade gradient overlays.
 * @param content Slot layout to render inside the ticker.
 */
@Composable
fun KMarquee(
    modifier: Modifier = Modifier,
    direction: KMarqueeDirection = KMarqueeDirection.FORWARD,
    velocity: Dp = KMarqueeDefaults.Velocity,
    spacing: Dp = KMarqueeDefaults.Spacing,
    repeatCount: Int = 4,
    pauseOnTouch: Boolean = true,
    enableFadeEdges: Boolean = true,
    fadeColor: Color = Color.Unspecified,
    fadeWidth: Dp = KMarqueeDefaults.FadeWidth,
    content: @Composable () -> Unit
) {
    var isPaused by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val spacingPx = with(density) { spacing.toPx() }
    val velocityPx = with(density) { velocity.toPx() }

    Box(
        modifier = modifier
            .clipToBounds()
            .then(
                if (pauseOnTouch) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
            // 1. Measure single item content width
            val contentMeasurables = subcompose("content_sample") { content() }
            val contentPlaceables = contentMeasurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            
            val contentWidth = contentPlaceables.maxOfOrNull { it.width } ?: 0
            val contentHeight = contentPlaceables.maxOfOrNull { it.height } ?: 0

            val totalStepWidth = contentWidth + spacingPx

            // Measure children with exact heights
            val actualPlaceables = subcompose("marquee_items") {
                repeat(repeatCount) {
                    Row {
                        content()
                        Spacer(modifier = Modifier.width(spacing))
                    }
                }
            }.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0, maxHeight = constraints.maxHeight)) }

            layout(
                width = constraints.maxWidth,
                height = if (constraints.hasBoundedHeight) constraints.maxHeight else contentHeight
            ) {
                actualPlaceables.forEach { placeable ->
                    placeable.place(0, 0)
                }
            }
        }

        // Animated Offset Implementation
        KMarqueeContentEngine(
            direction = direction,
            velocityPx = velocityPx,
            spacing = spacing,
            repeatCount = repeatCount,
            isPaused = isPaused,
            content = content
        )

        // Gradient Fades
        if (enableFadeEdges) {
            val resolvedFadeColor = if (fadeColor != Color.Unspecified) fadeColor else Color.Black

            // Left Fade
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(fadeWidth)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(resolvedFadeColor, Color.Transparent)
                        )
                    )
            )

            // Right Fade
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(fadeWidth)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, resolvedFadeColor)
                        )
                    )
            )
        }
    }
}

@Composable
private fun KMarqueeContentEngine(
    direction: KMarqueeDirection,
    velocityPx: Float,
    spacing: Dp,
    repeatCount: Int,
    isPaused: Boolean,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "KMarqueeTransition")
    var contentWidthPx by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val totalShiftWidth = contentWidthPx + spacingPx

    val durationMillis = if (totalShiftWidth > 0f && velocityPx > 0f) {
        ((totalShiftWidth / velocityPx) * 1000).toInt().coerceAtLeast(1)
    } else 1000

    val offsetAnimation by transition.animateFloat(
        initialValue = if (direction == KMarqueeDirection.FORWARD) 0f else -totalShiftWidth,
        targetValue = if (direction == KMarqueeDirection.FORWARD) -totalShiftWidth else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "KMarqueeOffset"
    )

    val currentOffset = if (isPaused) 0f else offsetAnimation

    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val sampleMeasurables = subcompose("sample") { content() }
        val samplePlaceables = sampleMeasurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val singleItemWidth = samplePlaceables.maxOfOrNull { it.width }?.toFloat() ?: 0f

        if (contentWidthPx != singleItemWidth) {
            contentWidthPx = singleItemWidth
        }

        val items = subcompose("real_content") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(repeatCount) {
                    content()
                    Spacer(modifier = Modifier.width(spacing))
                }
            }
        }

        val placeables = items.map { it.measure(constraints.copy(minWidth = 0)) }
        val maxHeight = placeables.maxOfOrNull { it.height } ?: 0

        layout(constraints.maxWidth, maxHeight) {
            placeables.forEach { placeable ->
                placeable.placeRelative(x = currentOffset.toInt(), y = 0)
            }
        }
    }
}