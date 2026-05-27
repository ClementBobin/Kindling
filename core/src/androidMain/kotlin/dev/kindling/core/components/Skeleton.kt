package dev.kindling.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────
//  Shimmer brush helper
// ─────────────────────────────────────────────

@Composable
fun kindlingShimmerBrush(
    baseColor: Color      = MaterialTheme.colorScheme.surfaceVariant,
    highlightColor: Color = MaterialTheme.colorScheme.surface,
    durationMillis: Int   = 1_200
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val t by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1_000f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerT"
    )
    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start  = Offset(t - 300f, t - 300f),
        end    = Offset(t, t)
    )
}

// ─────────────────────────────────────────────
//  Skeleton
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Skeleton — mirrors `skeleton.tsx`.
 *
 * ```kotlin
 * Skeleton(modifier = Modifier.fillMaxWidth().height(20.dp))
 * Skeleton(modifier = Modifier.size(40.dp), shape = CircleShape)
 * ```
 */
@Composable
fun Skeleton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    Box(modifier = modifier.clip(shape).background(kindlingShimmerBrush()))
}