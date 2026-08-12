package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Constrains its content to a given aspect ratio — mirrors shadcn/ui `AspectRatio`.
 *
 * ```kotlin
 * KAspectRatio(ratio = 16f / 9f) {
 *     Image(painter = …, contentDescription = null, modifier = Modifier.fillMaxSize())
 * }
 * ```
 *
 * @param ratio   Width-to-height ratio, e.g. `16f / 9f`, `4f / 3f`, `1f`.
 * @param modifier Applied to the outer [Box].
 * @param content  Content to render inside the constrained area.
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