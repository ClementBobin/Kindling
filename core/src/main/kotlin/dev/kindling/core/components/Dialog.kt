package dev.kindling.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Shadcn/ui-style AlertDialog — confirmation / destructive actions.
 *
 * ```kotlin
 * KAlertDialog(
 *     open          = open,
 *     onDismiss     = { open = false },
 *     title         = "Are you absolutely sure?",
 *     description   = "This action cannot be undone.",
 *     confirmLabel  = "Continue",
 *     onConfirm     = { open = false },
 *     isDestructive = true
 * )
 * ```
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
    // Keep usePlatformDefaultWidth = true (the default) to avoid measurement
    // crashes on devices that don't support custom-width dialogs in all
    // configurations. Callers can override if they need it.
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = true)
) {
    if (!open) return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(12.dp),
        containerColor   = MaterialTheme.colorScheme.surface,
        properties       = properties,
        title            = {
            Text(
                text  = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text             = if (description != null) {
            {
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        confirmButton    = {
            KButton(
                onClick = onConfirm,
                variant = if (isDestructive) KButtonVariant.Destructive else KButtonVariant.Default,
                size    = KButtonSize.Sm
            ) { Text(confirmLabel) }
        },
        dismissButton    = {
            KButton(
                onClick = onDismiss,
                variant = KButtonVariant.Outline,
                size    = KButtonSize.Sm
            ) { Text(cancelLabel) }
        }
    )
}

/**
 * Shadcn/ui-style full-screen Dialog with a free-form content slot.
 *
 * `usePlatformDefaultWidth = false` is intentional here — we draw our own
 * 92 %-wide card.  To avoid the blank-dialog crash that can happen when the
 * first composition has zero size, the inner Column carries an explicit
 * `fillMaxWidth(0.92f)` and `wrapContentHeight()`.
 *
 * ```kotlin
 * KDialog(open = open, onDismiss = { open = false }) {
 *     KDialogHeader(title = "Edit Profile", description = "Make changes here.")
 *     Spacer(Modifier.height(16.dp))
 *     KFormField(…)
 *     KDialogFooter {
 *         KButton(text = "Cancel", onClick = { open = false }, variant = KButtonVariant.Outline)
 *         KButton(text = "Save",   onClick = { open = false })
 *     }
 * }
 * ```
 */
@Composable
fun KDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit
) {
    if (!open) return

    Dialog(
        onDismissRequest = onDismiss,
        properties       = properties
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()          // ← prevents zero-height crash
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            content = content
        )
    }
}

// ─────────────────────────────────────────────
//  Header / Footer helpers
// ─────────────────────────────────────────────

/** Standard dialog header: title + optional description. */
@Composable
fun KDialogHeader(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (description != null) {
            Text(
                text  = description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Standard dialog footer — right-aligns action buttons. */
@Composable
fun KDialogFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        content               = content
    )
}