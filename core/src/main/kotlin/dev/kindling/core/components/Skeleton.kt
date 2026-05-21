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
import androidx.compose.ui.tooling.preview.Preview
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Create a moving shimmer [Brush] from the current Material3 colour scheme.
 *
 * @param baseColor Base colour for the shimmer gradient.
 * @param highlightColor Highlight colour for the shimmer gradient.
 * @param durationMillis Duration of a single shimmer cycle in milliseconds.
 * @return A [Brush] that animates across the component surface.
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
 * Render a shadcn/ui-style skeleton placeholder.
 *
 * ```kotlin
 * KSkeleton(modifier = Modifier.fillMaxWidth().height(20.dp))
 * KSkeleton(modifier = Modifier.size(40.dp), shape = CircleShape)
 * ```
 *
 * @param modifier Applied to the outermost layout element.
 * @param shape Shape of the skeleton placeholder.
 */
@Composable
fun KSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(modifier = modifier.clip(shape).background(kindlingShimmerBrush()))
}

@Preview(name = "KSkeleton — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KSkeleton — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKSkeleton() {
    KindlingPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KSkeleton(modifier = Modifier.fillMaxWidth(0.9f).height(12.dp))
            KSkeleton(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp))
            KSkeleton(modifier = Modifier.fillMaxWidth(0.8f).height(12.dp))
        }
    }
}
