package dev.kindling.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render the header area of an empty state.
 *
 * @param modifier Applied to the outermost layout element.
 * @param content Header content slot.
 */
@Composable
fun KEmptyHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Preview(name = "KEmptyHeader — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KEmptyHeader — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKEmptyHeader() {
    KindlingPreviewSurface {
        KEmptyHeader {
            KEmptyTitle("Nothing here yet")
            KEmptyDescription("Try adjusting your filters or create a new item.")
        }
    }
}
