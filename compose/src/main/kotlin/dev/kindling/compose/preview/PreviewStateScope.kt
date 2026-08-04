package dev.kindling.compose.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Renders multiple named state variants in a single preview panel.
 *
 * ```kotlin
 * @KPreview
 * @Composable
 * fun ProfileStatesPreview() {
 *     KPreviewScreen {
 *         PreviewStateGallery(
 *             "Default"  to ProfileState.preview(),
 *             "Loading"  to ProfileState.previewLoading(),
 *             "Empty"    to ProfileState.previewEmpty(),
 *         ) { state ->
 *             ProfileContent(state = state, onSave = {}, onDelete = {})
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun <S> PreviewStateGallery(
    vararg states: Pair<String, S>,
    content: @Composable (S) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        states.forEachIndexed { i, (label, state) ->
            if (i > 0) HorizontalDivider()
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            )
            content(state)
        }
    }
}
