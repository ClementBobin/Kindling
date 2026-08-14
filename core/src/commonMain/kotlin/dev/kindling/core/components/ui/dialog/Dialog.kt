package dev.kindling.core.components.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Shadcn/ui-style Dialog root — mirrors `Dialog` with a controlled open state.
 *
 * A dialog is a temporary window that sits on top of the main UI, requiring user
 * interaction before they can return to the previous screen. It is used for
 * confirmations, small forms, or focused tasks.
 *
 * In Kindling, the trigger is typically any external composable that calls `onOpenChange(true)`.
 *
 * ### Example usage:
 * ```kotlin
 * var isDialogOpen by remember { mutableStateOf(false) }
 * 
 * // Trigger
 * KButton("Edit Profile", onClick = { isDialogOpen = true })
 * 
 * Dialog(
 *     open = isDialogOpen,
 *     onOpenChange = { isDialogOpen = it }
 * ) {
 *     DialogContent {
 *         DialogHeader {
 *             DialogTitle("Edit profile")
 *             DialogDescription("Update your account information below.")
 *         }
 *         // Form content...
 *         DialogFooter(showCloseButton = true, onDismiss = { isDialogOpen = false }) {
 *             KButton("Save Changes", onClick = { /* Save */ })
 *         }
 *     }
 * }
 * ```
 *
 * @param open Whether the dialog is currently visible.
 * @param onOpenChange Callback invoked when the open state should change (e.g., when the user clicks outside).
 * @param content The structured content of the dialog, typically including [DialogContent].
 */
@Composable
fun Dialog(
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    content: @Composable DialogScope.() -> Unit
) {
    val scope = remember(onOpenChange) { DialogScope { onOpenChange(false) } }
    scope.content()
    // The actual overlay is rendered by DialogContent below
    if (!open) return
}