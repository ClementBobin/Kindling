package dev.kindling.core.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import dev.kindling.core.theme.LocalKindlingShapes
import dev.kindling.core.components.ui.animated.kindlingShimmerBrush

/**
 * Shadcn/ui-style Skeleton — mirrors `skeleton.tsx`.
 *
 * ```kotlin
 * KSkeleton(modifier = Modifier.fillMaxWidth().height(20.dp))
 * KSkeleton(modifier = Modifier.size(40.dp), shape = CircleShape)
 * ```
 */
@Composable
fun KSkeleton(
    modifier: Modifier = Modifier,
) {
    val shape = LocalKindlingShapes.current.radiusMd
    Box(modifier = modifier.clip(shape).background(kindlingShimmerBrush()))
}