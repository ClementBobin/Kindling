package dev.kindling.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.kindling.core.theme.LocalKindlingShapes
import dev.kindling.core.theme.kindlingShadowMd
import kotlin.math.roundToInt

// ─────────────────────────────────────────────
//  Enums
// ─────────────────────────────────────────────

/** Which side of the trigger the inline panel appears on. */
enum class KPopoverSide { Top, Bottom, Left, Right }

/** Alignment of the panel along the cross-axis of the trigger. */
enum class KPopoverAlign { Start, Center, End }

/**
 * When [KPopover] is in overlay mode (`overlayZone` is not null), this
 * controls where on screen the panel is placed.
 */
enum class KPopoverOverlayPosition {
    /** Horizontally centred, vertically centred. */
    Center,
    /** Full width, pinned to the bottom of the screen (sheet style). */
    BottomSheet,
    /** Full width, pinned to the top of the screen. */
    TopSheet,
    /** Full height, pinned to the left edge (drawer style). */
    StartDrawer,
    /** Full height, pinned to the right edge. */
    EndDrawer
}

// ─────────────────────────────────────────────
//  KPopover
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  Inline implementation
// ─────────────────────────────────────────────

@Composable
private fun KPopoverInline(
    open: Boolean,
    onDismiss: () -> Unit,
    trigger: @Composable () -> Unit,
    side: KPopoverSide,
    align: KPopoverAlign,
    sideOffset: Dp,
    dismissOnClickOutside: Boolean,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs      = MaterialTheme.colorScheme
    val shape = LocalKindlingShapes.current.radiusLg
    val density = LocalDensity.current

    var triggerW  by remember { mutableStateOf(0) }
    var triggerH  by remember { mutableStateOf(0) }
    var panelW    by remember { mutableStateOf(0) }
    var panelH    by remember { mutableStateOf(0) }
    // Root bounds — measured once from the root-level Box
    var rootW     by remember { mutableStateOf(Int.MAX_VALUE) }
    // Trigger's X in root — needed for horizontal clamping
    var triggerRootX by remember { mutableStateOf(0f) }

    val sideOffsetPx = with(density) { sideOffset.toPx().roundToInt() }

    fun rawOffset(): IntOffset = when (side) {
        KPopoverSide.Bottom -> IntOffset(
            x = when (align) {
                KPopoverAlign.Start  -> 0
                KPopoverAlign.End    -> triggerW - panelW
                KPopoverAlign.Center -> (triggerW - panelW) / 2
            },
            y = triggerH + sideOffsetPx
        )
        KPopoverSide.Top -> IntOffset(
            x = when (align) {
                KPopoverAlign.Start  -> 0
                KPopoverAlign.End    -> triggerW - panelW
                KPopoverAlign.Center -> (triggerW - panelW) / 2
            },
            y = -(panelH + sideOffsetPx)
        )
        KPopoverSide.Right -> IntOffset(
            x = triggerW + sideOffsetPx,
            y = when (align) {
                KPopoverAlign.Start  -> 0
                KPopoverAlign.End    -> triggerH - panelH
                KPopoverAlign.Center -> (triggerH - panelH) / 2
            }
        )
        KPopoverSide.Left -> IntOffset(
            x = -(panelW + sideOffsetPx),
            y = when (align) {
                KPopoverAlign.Start  -> 0
                KPopoverAlign.End    -> triggerH - panelH
                KPopoverAlign.Center -> (triggerH - panelH) / 2
            }
        )
    }

    fun clampedOffset(): IntOffset {
        val raw = rawOffset()
        // Clamp horizontally so panel stays within root width
        val absLeft  = triggerRootX + raw.x
        val absRight = absLeft + panelW
        val clampedX = when {
            rootW < Int.MAX_VALUE && absRight > rootW -> raw.x - (absRight - rootW).roundToInt()
            absLeft < 0f                              -> raw.x - absLeft.roundToInt()
            else                                      -> raw.x
        }
        return IntOffset(clampedX, raw.y)
    }

    // Outermost Box measured to know screen/root width for clamping
    Box(
        modifier = modifier.onGloballyPositioned { coords ->
            // Walk up to find root width: use the root position approach
            // coords here is the Box itself; positionInRoot gives offset from root
            rootW = coords.size.width
        }
    ) {
        // Trigger
        Box(
            modifier = Modifier.onGloballyPositioned { coords ->
                triggerW     = coords.size.width
                triggerH     = coords.size.height
                triggerRootX = coords.positionInRoot().x
            }
        ) {
            trigger()
        }

        // Scrim for click-outside dismissal
        if (open && dismissOnClickOutside) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(8f)
                    // Expand to cover the whole screen by offsetting to root origin
                    .requiredSize(with(density) { rootW.toDp() }, 4000.dp)
                    .offset { IntOffset(-triggerRootX.roundToInt(), -triggerH) }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onDismiss
                    )
            )
        }

        // Panel
        AnimatedVisibility(
            visible  = open,
            enter    = expandVertically(tween(150)) + fadeIn(tween(150)),
            exit     = shrinkVertically(tween(150)) + fadeOut(tween(150)),
            modifier = Modifier
                .offset { clampedOffset() }
                .zIndex(9f)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 200.dp, max = 320.dp)
                    .onGloballyPositioned { coords ->
                        panelW = coords.size.width
                        panelH = coords.size.height
                    }
                    .kindlingShadowMd(shape)
                    .clip(shape)
                    .background(cs.surface)
                    .border(1.dp, cs.outline.copy(alpha = 0.4f), shape)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content             = content
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Overlay implementation
// ─────────────────────────────────────────────

@Composable
private fun KPopoverOverlay(
    open: Boolean,
    onDismiss: () -> Unit,
    position: KPopoverOverlayPosition,
    dismissOnClickOutside: Boolean,
    trigger: @Composable () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        // Trigger — always visible, never moves
        trigger()

        // Full-screen scrim + panel
        if (open) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            ) {
                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cs.scrim.copy(alpha = 0.4f))
                        .then(
                            if (dismissOnClickOutside) Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = onDismiss
                            ) else Modifier
                        )
                )

                // Panel positioned inside the scrim
                val panelAlignment: Alignment = when (position) {
                    KPopoverOverlayPosition.Center      -> Alignment.Center
                    KPopoverOverlayPosition.BottomSheet -> Alignment.BottomCenter
                    KPopoverOverlayPosition.TopSheet    -> Alignment.TopCenter
                    KPopoverOverlayPosition.StartDrawer -> Alignment.CenterStart
                    KPopoverOverlayPosition.EndDrawer   -> Alignment.CenterEnd
                }

                val panelModifier: Modifier = when (position) {
                    KPopoverOverlayPosition.BottomSheet,
                    KPopoverOverlayPosition.TopSheet    -> Modifier.fillMaxWidth()
                    KPopoverOverlayPosition.StartDrawer,
                    KPopoverOverlayPosition.EndDrawer   -> Modifier.fillMaxHeight().widthIn(max = 320.dp)
                    KPopoverOverlayPosition.Center      -> Modifier.widthIn(min = 200.dp, max = 320.dp)
                }

                AnimatedVisibility(
                    visible  = open,
                    enter    = fadeIn(tween(200)) + expandVertically(tween(200), expandFrom = when (position) {
                        KPopoverOverlayPosition.TopSheet    -> Alignment.Top
                        KPopoverOverlayPosition.StartDrawer,
                        KPopoverOverlayPosition.EndDrawer   -> Alignment.CenterVertically
                        else                                -> Alignment.Bottom
                    }),
                    exit     = fadeOut(tween(200)) + shrinkVertically(tween(200), shrinkTowards = when (position) {
                        KPopoverOverlayPosition.TopSheet    -> Alignment.Top
                        KPopoverOverlayPosition.StartDrawer,
                        KPopoverOverlayPosition.EndDrawer   -> Alignment.CenterVertically
                        else                                -> Alignment.Bottom
                    }),
                    modifier = Modifier.align(panelAlignment)
                ) {
                    Column(
                        modifier = panelModifier
                            // Stop taps on the panel reaching the scrim
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = {}
                            )
                            .shadow(8.dp, when (position) {
                                KPopoverOverlayPosition.BottomSheet -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                KPopoverOverlayPosition.TopSheet    -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                KPopoverOverlayPosition.StartDrawer -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                                KPopoverOverlayPosition.EndDrawer   -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                                KPopoverOverlayPosition.Center      -> RoundedCornerShape(16.dp)
                            })
                            .clip(when (position) {
                                KPopoverOverlayPosition.BottomSheet -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                KPopoverOverlayPosition.TopSheet    -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                KPopoverOverlayPosition.StartDrawer -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                                KPopoverOverlayPosition.EndDrawer   -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                                KPopoverOverlayPosition.Center      -> RoundedCornerShape(16.dp)
                            })
                            .background(cs.surface)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        content             = content
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Header / Title / Description helpers
// ─────────────────────────────────────────────

/** Optional header section inside a [KPopover]. */
@Composable
fun KPopoverHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content             = content
    )
}

/** Popover title. */
@Composable
fun KPopoverTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleSmall,
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

/** Popover description — muted secondary text. */
@Composable
fun KPopoverDescription(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.bodySmall,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
