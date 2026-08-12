package dev.kindling.core.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.kindling.core.theme.LocalKindlingShapes

// ─────────────────────────────────────────────
//  Internal state holder
// ─────────────────────────────────────────────

/** Slot structure passed as receiver to [Dialog] content. */
class DialogScope internal constructor(val onDismiss: () -> Unit)

// ─────────────────────────────────────────────
//  Dialog (root)
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  DialogTrigger
// ─────────────────────────────────────────────

/**
 * Wraps any composable that should open the dialog.
 * Pass the setter directly if managing state externally.
 */
@Composable
fun DialogTrigger(content: @Composable () -> Unit) { content() }

// ─────────────────────────────────────────────
//  DialogPortal / DialogOverlay
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  DialogClose
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  DialogContent
// ─────────────────────────────────────────────

/**
 * The dialog panel — mirrors `DialogContent` from `dialog.tsx`.
 *
 * Renders a full-screen scrim + centred card.
 * Includes a close × button when [showCloseButton] = true (default).
 * Uses [KButton] for the close action.
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
                            size    = KButtonSize.IconSm
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    }
                }
                content()
            }
        }
    }
}

// ─────────────────────────────────────────────
//  DialogHeader
// ─────────────────────────────────────────────

@Composable
fun DialogHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content             = content
    )
}

// ─────────────────────────────────────────────
//  DialogTitle
// ─────────────────────────────────────────────

@Composable
fun DialogTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
//  DialogDescription
// ─────────────────────────────────────────────

@Composable
fun DialogDescription(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
//  DialogFooter
// ─────────────────────────────────────────────

/**
 * Footer row with optional built-in close button.
 * Uses [KButton] for [showCloseButton].
 */
@Composable
fun DialogFooter(
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val rounded = LocalKindlingShapes.current.roundedXl
    val cs = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = rounded, bottomEnd = rounded))) {
        HorizontalDivider(color = cs.outlineVariant, thickness = 0.5.dp)
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(cs.surfaceVariant.copy(.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            content()
            if (showCloseButton && onDismiss != null) {
                KButton(
                    onClick = onDismiss,
                    variant = KButtonVariant.Outline
                ) { Text("Close") }
            }
        }
    }
}