package dev.kindling.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Constrains its content to a given aspect ratio.
 *
 * Mirrors the shadcn/ui `AspectRatio` component backed by Radix UI.
 * Fills available width and sets height via [ratio].
 *
 * ```kotlin
 * KAspectRatio(ratio = 16f / 9f) {
 *     Image(
 *         painter = painterResource(R.drawable.hero),
 *         contentDescription = null,
 *         contentScale = ContentScale.Crop,
 *         modifier = Modifier.fillMaxSize()
 *     )
 * }
 * ```
 *
 * @param ratio  Width-to-height ratio (e.g. `16f / 9f`, `1f`, `4f / 3f`).
 * @param modifier Optional modifier applied to the outer [Box].
 * @param content  The composable content to render inside the constrained area.
 */
@Composable
fun KAspectRatio(
    ratio: Float = 1f,
    modifier: Modifier = Modifier,
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