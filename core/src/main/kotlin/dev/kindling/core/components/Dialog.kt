package dev.kindling.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render a shadcn/ui-style dialog with a free-form content slot.
 *
 * ```kotlin
 * KDialog(open = open, onDismiss = { open = false }) {
 *     KDialogHeader(title = "Edit Profile", description = "Make changes here.")
 *     Spacer(Modifier.height(16.dp))
 *     // … form fields …
 *     KDialogFooter {
 *         KButton(text = "Save changes", onClick = { open = false })
 *     }
 * }
 * ```
 *
 * @param open When `true`, the dialog is shown.
 * @param onDismiss Callback invoked when the dialog should be dismissed.
 * @param properties Dialog properties for platform configuration.
 * @param content Content shown inside the dialog body.
 */
@Composable
fun KDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit
) {
    if (!open) return

    Dialog(onDismissRequest = onDismiss, properties = properties) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            content = content
        )
    }
}

@Preview(name = "KDialog — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KDialog — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKDialog() {
    KindlingPreviewSurface {
        KDialog(open = true, onDismiss = { }) {
            KDialogHeader(title = "Dialog title", description = "This is a simple dialog.")
            KDialogFooter {
                KButton(text = "Close", onClick = { })
            }
        }
    }
}
