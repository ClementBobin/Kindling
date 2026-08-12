package dev.kindling.core.components.ui.animated

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Enums & Defaults ────────────────────────────────────────────────────────

enum class KMarqueeOrientation {
    VERTICAL,
    HORIZONTAL
}

object KTextMarqueeDefaults {
    val Height: Dp = 120.dp
    val ItemHeight: Dp = 40.dp
    const val SpeedSecondsPerCycle: Float = 3f
    val Orientation = KMarqueeOrientation.VERTICAL
}

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * Vertical or horizontal infinite text/content marquee component with edge fade gradient masking
 * (Spell.sh / Magic UI inspired).
 *
 * @param items List of composable content items to scroll continuously.
 * @param modifier Applied to the marquee container layout.
 * @param prefix Optional composable displayed statically before the marquee slot.
 * @param orientation Direction of continuous scroll ([KMarqueeOrientation.VERTICAL] or [KMarqueeOrientation.HORIZONTAL]).
 * @param containerHeight Viewport height for vertical marquee masking.
 * @param itemHeight Height allotted per item step.
 * @param speedSecondsPerCycle Duration in seconds for one full loop cycle.
 * @param enableFadeMask Applies top/bottom or left/right gradient alpha mask for smooth entry/exit.
 */
@Composable
fun KTextMarquee(
    items: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    prefix: (@Composable () -> Unit)? = null,
    orientation: KMarqueeOrientation = KTextMarqueeDefaults.Orientation,
    containerHeight: Dp = KTextMarqueeDefaults.Height,
    itemHeight: Dp = KTextMarqueeDefaults.ItemHeight,
    speedSecondsPerCycle: Float = KTextMarqueeDefaults.SpeedSecondsPerCycle,
    enableFadeMask: Boolean = true
) {
    if (items.isEmpty()) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Optional prefix view
        if (prefix != null) {
            prefix()
            Spacer(modifier = Modifier.width(6.dp))
        }

        val itemCount = items.size
        val infiniteTransition = rememberInfiniteTransition(label = "KTextMarqueeTransition")

        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (speedSecondsPerCycle * 1000).toInt(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "MarqueeScrollProgress"
        )

        val maskModifier = if (enableFadeMask) {
            Modifier
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val maskBrush = if (orientation == KMarqueeOrientation.VERTICAL) {
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.25f to Color.Black,
                            0.75f to Color.Black,
                            1f to Color.Transparent
                        )
                    } else {
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.25f to Color.Black,
                            0.75f to Color.Black,
                            1f to Color.Transparent
                        )
                    }
                    drawRect(brush = maskBrush, blendMode = BlendMode.DstIn)
                }
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .height(containerHeight)
                .then(maskModifier),
            contentAlignment = Alignment.CenterStart
        ) {
            if (orientation == KMarqueeOrientation.VERTICAL) {
                val totalScrollHeightPx = itemHeight.value * itemCount

                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                    items.forEachIndexed { index, item ->
                        val itemOffsetY = remember(progress, index, itemCount, totalScrollHeightPx) {
                            val baseOffset = (index.toFloat() / itemCount)
                            val currentPos = (baseOffset - progress + 1f) % 1f
                            val centeredPos = currentPos - 0.5f
                            centeredPos * totalScrollHeightPx
                        }

                        Box(
                            modifier = Modifier
                                .height(itemHeight)
                                .graphicsLayer {
                                    translationY = itemOffsetY
                                },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            item()
                        }
                    }
                }
            } else {
                val totalScrollWidthPx = 300f * itemCount

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    items.forEachIndexed { index, item ->
                        val itemOffsetX = remember(progress, index, itemCount, totalScrollWidthPx) {
                            val baseOffset = (index.toFloat() / itemCount)
                            val currentPos = (baseOffset - progress + 1f) % 1f
                            val centeredPos = currentPos - 0.5f
                            centeredPos * totalScrollWidthPx
                        }

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    translationX = itemOffsetX
                                },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            item()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convenience overload for rendering simple string lists in `KTextMarquee`.
 */
@Composable
fun KTextMarqueeString(
    strings: List<String>,
    modifier: Modifier = Modifier,
    prefixText: String? = null,
    style: TextStyle = LocalTextStyle.current,
    containerHeight: Dp = KTextMarqueeDefaults.Height,
    itemHeight: Dp = KTextMarqueeDefaults.ItemHeight,
    speedSecondsPerCycle: Float = KTextMarqueeDefaults.SpeedSecondsPerCycle
) {
    val items: List<@Composable () -> Unit> = strings.map { str ->
        {
            Text(text = str, style = style)
        }
    }

    val prefixComposable: (@Composable () -> Unit)? = prefixText?.let {
        { Text(text = it, style = style) }
    }

    KTextMarquee(
        items = items,
        modifier = modifier,
        prefix = prefixComposable,
        containerHeight = containerHeight,
        itemHeight = itemHeight,
        speedSecondsPerCycle = speedSecondsPerCycle
    )
}