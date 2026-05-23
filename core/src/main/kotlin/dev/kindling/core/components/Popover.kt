package dev.kindling.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────
//  Anchor side / alignment
// ─────────────────────────────────────────────

/** Preferred side on which the popover appears relative to its anchor. */
enum class KPopoverSide { Top, Bottom, Left, Right }

/** Alignment of the popover along the cross-axis of the anchor. */
enum class KPopoverAlign { Start, Center, End }

// ─────────────────────────────────────────────
//  KPopover
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Popover — a controlled, floating content panel anchored to
 * a trigger composable.
 *
 * Uses [DropdownMenu] which handles Android window/focus anchoring correctly.
 * [side] and [align] are mapped to [DropdownMenuProperties] offset hints.
 *
 * ```kotlin
 * var open by remember { mutableStateOf(false) }
 *
 * KPopover(
 *     open      = open,
 *     onDismiss = { open = false },
 *     trigger   = {
 *         KButton(text = "Open", onClick = { open = !open })
 *     }
 * ) {
 *     KPopoverHeader {
 *         KPopoverTitle("Dimensions")
 *         KPopoverDescription("Set your preferred dimensions.")
 *     }
 *     Spacer(Modifier.height(8.dp))
 *     KInput(value = "100%", onValueChange = {})
 * }
 * ```
 *
 * @param open        Whether the popover is visible.
 * @param onDismiss   Called when the user taps outside.
 * @param trigger     The anchor composable (always visible).
 * @param side        Preferred side — mapped to [DropdownMenu] placement.
 * @param align       Cross-axis alignment (Start / Center / End).
 * @param sideOffset  Gap between trigger and panel (passed as offset).
 * @param modifier    Applied to the wrapping [Box].
 * @param content     Popover panel composable body.
 */
@Composable
fun KPopover(
    open: Boolean,
    onDismiss: () -> Unit,
    trigger: @Composable () -> Unit,
    side: KPopoverSide = KPopoverSide.Bottom,
    align: KPopoverAlign = KPopoverAlign.Center,
    sideOffset: androidx.compose.ui.unit.Dp = 4.dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        trigger()

        DropdownMenu(
            expanded         = open,
            onDismissRequest = onDismiss,
            modifier         = Modifier.widthIn(min = 200.dp, max = 320.dp)
        ) {
            // Render callers' content as DropdownMenuItems would be too
            // restrictive — we wrap in a padded Column instead.
            Column(
                modifier            = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
                content             = content
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Header / Title / Description helpers
// ─────────────────────────────────────────────

/**
 * Optional header section inside a [KPopover].
 */
@Composable
fun KPopoverHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        content             = content
    )
}

/**
 * Popover title — slightly larger than body text.
 */
@Composable
fun KPopoverTitle(text: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text     = text,
        style    = MaterialTheme.typography.titleSmall,
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

/**
 * Popover description — muted secondary text.
 */
@Composable
fun KPopoverDescription(text: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text     = text,
        style    = MaterialTheme.typography.bodySmall,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}