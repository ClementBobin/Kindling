package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shadcn/ui-style Popover with two display modes:
 *
 * **Inline mode** (default — `overlayZone` = null):
 * The panel is positioned adjacent to [trigger], clamped so it never escapes
 * the measured root bounds. [side] and [align] control placement.
 *
 * **Overlay mode** (`overlayZone` is not null):
 * The panel fills a full-screen scrim and is placed at `overlayZone` inside
 * it (center, bottom-sheet, drawer, …). Useful for modal-style popovers.
 *
 * In both modes, [dismissOnClickOutside] = `true` (default) closes the panel
 * when the user taps outside the panel area.
 *
 * ```kotlin
 * // Inline popover
 * var open by remember { mutableStateOf(false) }
 * KPopover(
 *     open      = open,
 *     onDismiss = { open = false },
 *     trigger   = { KButton("Open", onClick = { open = !open }) }
 * ) {
 *     KPopoverTitle("Hello")
 * }
 *
 * // Bottom-sheet style overlay
 * KPopover(
 *     open        = open,
 *     onDismiss   = { open = false },
 *     overlayZone = KPopoverOverlayPosition.BottomSheet,
 *     trigger     = { KButton("Sheet", onClick = { open = !open }) }
 * ) {
 *     Text("Sheet content")
 * }
 * ```
 */
@Composable
fun KPopover(
    open: Boolean,
    onDismiss: () -> Unit,
    trigger: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    // ── Inline mode ───────────────────────────
    side: KPopoverSide = KPopoverSide.Bottom,
    align: KPopoverAlign = KPopoverAlign.Center,
    sideOffset: Dp = 4.dp,
    // ── Overlay mode ──────────────────────────
    overlayZone: KPopoverOverlayPosition? = null,
    // ── Shared ────────────────────────────────
    dismissOnClickOutside: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    if (overlayZone != null) {
        KPopoverOverlay(
            open                  = open,
            onDismiss             = onDismiss,
            position              = overlayZone,
            dismissOnClickOutside = dismissOnClickOutside,
            trigger               = trigger,
            modifier              = modifier,
            content               = content
        )
    } else {
        KPopoverInline(
            open                  = open,
            onDismiss             = onDismiss,
            trigger               = trigger,
            side                  = side,
            align                 = align,
            sideOffset            = sideOffset,
            dismissOnClickOutside = dismissOnClickOutside,
            modifier              = modifier,
            content               = content
        )
    }
}