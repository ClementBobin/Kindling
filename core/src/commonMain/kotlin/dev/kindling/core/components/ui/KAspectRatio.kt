package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Constrains its content to a given aspect ratio — mirrors shadcn/ui `AspectRatio`.
 *
 * This component is useful for maintaining consistent proportions for images, videos, or
 * other media content regardless of their actual dimensions or the parent container's width.
 *
 * ### Example usage:
 * ```kotlin
 * KAspectRatio(ratio = 16f / 9f) {
 *     Image(
 *         painter = painterResource(Res.drawable.landscape),
 *         contentDescription = "Landscape image",
 *         modifier = Modifier.fillMaxSize(),
 *         contentScale = ContentScale.Crop
 *     )
 * }
 * ```
 *
 * @param modifier The modifier to be applied to the layout.
 * @param ratio The width-to-height ratio (e.g., 1.0f for a square, 1.77f for 16:9).
 * @param content The composable content to be constrained by the aspect ratio.
 */
@Composable
fun KAspectRatio(
    modifier: Modifier = Modifier,
    ratio: Float = 1f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
    ) {
        content()
    }
}