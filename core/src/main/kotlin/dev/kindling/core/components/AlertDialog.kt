package dev.kindling.core.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.DialogProperties
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render a shadcn/ui-style alert dialog for confirmation actions.
 *
 * ```kotlin
 * var open by remember { mutableStateOf(false) }
 * KAlertDialog(
 *     open = open,
 *     onDismiss = { open = false },
 *     title = "Are you absolutely sure?",
 *     description = "This action cannot be undone.",
 *     confirmLabel = "Continue",
 *     onConfirm = { open = false },
 *     isDestructive = true
 * )
 * ```
 *
 * @param open When `true`, the dialog is shown.
 * @param onDismiss Callback invoked when the dialog should be dismissed.
 * @param title Title shown at the top of the dialog.
 * @param description Optional supporting text shown below the title.
 * @param confirmLabel Label shown on the confirm button.
 * @param cancelLabel Label shown on the cancel button.
 * @param isDestructive When `true`, styles the confirm action as destructive.
 * @param onConfirm Callback invoked when the confirm button is pressed.
 * @param properties Dialog properties for platform configuration.
 */
@Composable
fun KAlertDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    title: String,
    description: String? = null,
    confirmLabel: String = "Continue",
    cancelLabel: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties()
) {
    if (!open) return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = if (description != null) {
            {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        confirmButton = {
            KButton(
                onClick = onConfirm,
                variant = if (isDestructive) KButtonVariant.Destructive else KButtonVariant.Default,
                size = KButtonSize.Sm
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            KButton(
                onClick = onDismiss,
                variant = KButtonVariant.Outline,
                size = KButtonSize.Sm
            ) { Text(cancelLabel) }
        },
        properties = properties
    )
}

@Preview(name = "KAlertDialog — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KAlertDialog — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKAlertDialog() {
    KindlingPreviewSurface {
        KAlertDialog(
            open = true,
            onDismiss = { },
            title = "Are you absolutely sure?",
            description = "This action cannot be undone.",
            confirmLabel = "Continue",
            cancelLabel = "Cancel",
            isDestructive = true,
            onConfirm = { }
        )
    }
}
