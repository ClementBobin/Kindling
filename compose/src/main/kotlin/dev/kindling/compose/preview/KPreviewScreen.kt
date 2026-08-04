package dev.kindling.compose.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

/**
 * Wraps preview content with your app's theme (replace [MaterialTheme]
 * with your own theme composable).
 *
 * Usage:
 * ```kotlin
 * @KPreview
 * @Composable
 * fun ProfileScreenPreview() {
 *     KPreviewScreen {
 *         ProfileContent(
 *             state = ProfileState.preview(),
 *             onSave = {}
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun KPreviewScreen(content: @Composable () -> Unit) {
    MaterialTheme {
        Surface(content = content)
    }
}
