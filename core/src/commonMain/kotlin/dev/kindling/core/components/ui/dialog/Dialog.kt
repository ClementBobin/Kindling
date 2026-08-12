package dev.kindling.core.components.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Shadcn/ui-style Dialog root — mirrors `Dialog` + controlled open state.
 *
 * ```kotlin
 * var open by remember { mutableStateOf(false) }
 * Dialog(open = open, onOpenChange = { open = it }) {
 *     DialogTrigger { KButton("Open", onClick = { open = true }) }
 *     DialogContent {
 *         DialogHeader {
 *             DialogTitle("Edit profile")
 *             DialogDescription("Make changes here.")
 *         }
 *     }
 * }
 * ```
 *
 * In Compose, the trigger is simply any composable that calls `onOpenChange(true)`.
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