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
 * Used to display a placeholder preview while content is loading. It uses a shimmering
 * animation to provide visual feedback that the app is still active.
 *
 * ### Example usage:
 * ```kotlin
 * Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
 *     // Placeholder for a profile image
 *     KSkeleton(modifier = Modifier.size(40.dp).clip(CircleShape))
 *     
 *     // Placeholder for a title
 *     KSkeleton(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp))
 *     
 *     // Placeholder for a description
 *     KSkeleton(modifier = Modifier.fillMaxWidth().height(16.dp))
 * }
 * ```
 * 
 * @param modifier The modifier to be applied to the layout, determining the skeleton's size and position.
 */
@Composable
fun KSkeleton(
    modifier: Modifier = Modifier,
) {
    val shape = LocalKindlingShapes.current.radiusMd
    Box(modifier = modifier.clip(shape).background(kindlingShimmerBrush()))
}