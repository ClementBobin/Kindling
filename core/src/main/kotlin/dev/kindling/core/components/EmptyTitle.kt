package dev.kindling.core.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render the title text for an empty state.
 *
 * @param text Title text to display.
 * @param modifier Applied to the text layout.
 */
@Composable
fun KEmptyTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text,
        modifier = modifier,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )
}

@Preview(name = "KEmptyTitle — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KEmptyTitle — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKEmptyTitle() {
    KindlingPreviewSurface {
        KEmptyTitle("Nothing to show")
    }
}
