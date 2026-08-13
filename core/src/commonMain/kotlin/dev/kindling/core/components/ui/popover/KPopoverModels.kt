package dev.kindling.core.components.ui.popover

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