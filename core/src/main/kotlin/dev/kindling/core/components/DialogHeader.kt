package dev.kindling.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render a dialog header with a title and optional description.
 *
 * @param title Title text shown at the top of the dialog.
 * @param description Optional supporting text shown below the title.
 * @param modifier Applied to the outermost layout element.
 */
@Composable
fun KDialogHeader(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Render a dialog footer that right-aligns its action buttons.
 *
 * @param modifier Applied to the outermost layout element.
 * @param content Action button content.
 */
@Composable
fun KDialogFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        content = content
    )
}

@Preview(name = "KDialogHeader — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KDialogHeader — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKDialogHeader() {
    KindlingPreviewSurface {
        KDialogHeader(
            title = "Dialog title",
            description = "Optional supporting description text."
        )
    }
}
