package dev.kindling.core.components.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Wraps any composable that should open the dialog.
 * Pass the setter directly if managing state externally.
 */
@Composable
fun DialogTrigger(content: @Composable () -> Unit) { content() }

/** Semi-transparent scrim behind the dialog panel. */
@Composable
fun DialogOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = .1f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onDismiss
            )
    )
}

/** No-op wrapper to mirror the web `DialogPortal` slot. */
@Composable
fun DialogPortal(content: @Composable () -> Unit) { content() }

/**
 * Any composable that dismisses the dialog when tapped.
 *
 * ```kotlin
 * DialogClose(onDismiss = { open = false }) {
 *     Text("Cancel")
 * }
 * ```
 */
@Composable
fun DialogClose(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.clickable(onClick = onDismiss)) { content() }
}