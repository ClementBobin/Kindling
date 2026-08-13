package dev.kindling.core.components.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.kindling.core.theme.LocalKindlingShapes
import dev.kindling.core.components.ui.KButton
import dev.kindling.core.components.ui.KButtonSize
import dev.kindling.core.components.ui.KButtonVariant

/**
 * The dialog panel — mirrors `DialogContent` from `dialog.tsx`.
 *
 * Renders a full-screen scrim + centred card.
 * Includes a close × button when [showCloseButton] = true (default).
 * Uses [dev.kindling.core.components.ui.KButton] for the close action.
 *
 * ```kotlin
 * DialogContent(open = open, onDismiss = { open = false }) {
 *     DialogHeader { DialogTitle("Hello") }
 * }
 * ```
 */
@Composable
fun DialogContent(
    open: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = LocalKindlingShapes.current.radiusXl
    if (!open) return

    Dialog(
        onDismissRequest = onDismiss,
        properties       = properties
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Scrim
            DialogOverlay(onDismiss = onDismiss, modifier = Modifier.matchParentSize())

            // Panel
            Column(
                modifier = modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                // Close button (top-right) — uses KButton
                if (showCloseButton) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                        KButton(
                            onClick = onDismiss,
                            variant = KButtonVariant.Ghost,
                            size = KButtonSize.IconSm
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                content()
            }
        }
    }
}