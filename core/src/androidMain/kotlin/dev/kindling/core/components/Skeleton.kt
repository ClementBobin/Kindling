package dev.kindling.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Returns a moving shimmer [Brush] from the current Material3 colour scheme.
 * Drop it as a [Modifier.background] to get the skeleton animation on any shape.
 */
@Composable
fun kindlingShimmerBrush(
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    highlightColor: Color = MaterialTheme.colorScheme.surface,
    durationMillis: Int = 1_200
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1_000f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start  = Offset(translateAnim - 300f, translateAnim - 300f),
        end    = Offset(translateAnim, translateAnim)
    )
}

/**
 * Shadcn/ui-style Skeleton — shimmering loading placeholder.
 *
 * ```kotlin
 * KSkeleton(modifier = Modifier.fillMaxWidth().height(20.dp))
 * KSkeleton(modifier = Modifier.size(40.dp), shape = CircleShape)
 * ```
 */
@Composable
fun KSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(modifier = modifier.clip(shape).background(kindlingShimmerBrush()))
}