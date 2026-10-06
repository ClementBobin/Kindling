package dev.kindling.core.components.ui.gesture

/**
 * Screen edge the user pulls away from to trigger a refresh in [KEdgeRefresh].
 *
 * [Start] and [End] follow the layout direction (they swap in RTL), exactly like
 * `Alignment.CenterStart` / `Alignment.CenterEnd`.
 */
enum class KRefreshEdge {
    /** Pull down from the top edge (YouTube-style). */
    Top,

    /** Pull up from the bottom edge. */
    Bottom,

    /** Pull toward the end from the start edge (left in LTR). */
    Start,

    /** Pull toward the start from the end edge (right in LTR). */
    End
}
