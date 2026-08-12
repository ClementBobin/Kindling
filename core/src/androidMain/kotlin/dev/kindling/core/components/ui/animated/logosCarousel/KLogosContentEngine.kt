package dev.kindling.core.components.animated

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import dev.kindling.utils.method.KCounter

@Composable
internal fun KLogosContentEngine(
    direction: KLogosCarouselDirection,
    velocityPx: Float,
    spacing: Dp,
    repeatCount: Int,
    isPaused: Boolean,
    loopCounter: KCounter?,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "KLogosTransition")
    var singleSetWidthPx by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val totalShiftPx = singleSetWidthPx + spacingPx

    val durationMillis = if (totalShiftPx > 0f && velocityPx > 0f) {
        ((totalShiftPx / velocityPx) * 1000).toInt().coerceAtLeast(1)
    } else 1000

    val offsetAnimation by transition.animateFloat(
        initialValue = if (direction == KLogosCarouselDirection.FORWARD) 0f else -totalShiftPx,
        targetValue = if (direction == KLogosCarouselDirection.FORWARD) -totalShiftPx else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "KLogosOffset"
    )

    var lastOffset by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(offsetAnimation) {
        if (direction == KLogosCarouselDirection.FORWARD && offsetAnimation > lastOffset && lastOffset < -totalShiftPx * 0.9f) {
            loopCounter?.increment()
        } else if (direction == KLogosCarouselDirection.BACKWARD && offsetAnimation < lastOffset && lastOffset > -totalShiftPx * 0.1f) {
            loopCounter?.increment()
        }
        lastOffset = offsetAnimation
    }

    val currentOffset = if (isPaused) 0f else offsetAnimation

    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val sampleMeasurables = subcompose("sample_measure") { content() }
        val samplePlaceables = sampleMeasurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val measuredSingleWidth = samplePlaceables.maxOfOrNull { it.width }?.toFloat() ?: 0f

        if (singleSetWidthPx != measuredSingleWidth) {
            singleSetWidthPx = measuredSingleWidth
        }

        val repeatedMeasurables = subcompose("repeated_logos") {
            Row {
                repeat(repeatCount) {
                    content()
                    Spacer(modifier = Modifier.width(spacing))
                }
            }
        }

        val placeables = repeatedMeasurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val maxContentHeight = placeables.maxOfOrNull { it.height } ?: 0

        layout(
            width = constraints.maxWidth,
            height = if (constraints.hasBoundedHeight) constraints.maxHeight else maxContentHeight
        ) {
            placeables.forEach { placeable ->
                placeable.placeRelative(x = currentOffset.toInt(), y = 0)
            }
        }
    }
}