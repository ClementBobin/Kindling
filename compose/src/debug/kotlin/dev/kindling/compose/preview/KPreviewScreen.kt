package dev.kindling.compose.preview

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import dev.kindling.core.theme.KindlingShapes
import dev.kindling.core.theme.KindlingTheme

/**
 * Preview wrapper that mirrors [KindlingTheme] exactly.
 *
 * All parameters are optional and fall back to the same defaults as
 * [KindlingTheme], so a bare `KPreviewScreen { }` works out of the box
 * whether the caller is using a plain [MaterialTheme] or a full [KindlingTheme].
 *
 * ```kotlin
 * // Bare — works with plain MaterialTheme or KindlingTheme
 * @KPreview
 * @Composable
 * fun ProfilePreview() {
 *     KPreviewScreen {
 *         ProfileContent(state = ProfileState.preview(), onSave = {})
 *     }
 * }
 *
 * // Full override — mirrors a custom KindlingTheme call exactly
 * @KPreview
 * @Composable
 * fun ProfileThemedPreview() {
 *     KPreviewScreen(
 *         colorScheme = myDarkColorScheme,
 *         shapes      = KindlingShapes(base = 4.dp),
 *     ) {
 *         ProfileContent(state = ProfileState.preview(), onSave = {})
 *     }
 * }
 * ```
 */
@Composable
fun KPreviewScreen(
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    typography: Typography   = MaterialTheme.typography,
    shapes: KindlingShapes   = KindlingShapes(),
    content: @Composable () -> Unit
) {
    KindlingTheme(
        colorScheme = colorScheme,
        typography  = typography,
        shapes      = shapes,
        content     = { Surface(content = content) }
    )
}
