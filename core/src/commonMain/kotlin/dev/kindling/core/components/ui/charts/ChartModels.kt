package dev.kindling.core.components.charts

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
//  Chart type taxonomy — mirrors shadcn/ui's registry categories
// ─────────────────────────────────────────────────────────────────────────────

enum class ChartTypeCategory {
    AREA, BAR, LINE, PIE, RADAR, RADIAL, TOOLTIP
}

/**
 * Variant refinements within each category.
 * Maps 1-to-1 to shadcn chart filenames (e.g. chart-area-gradient → GRADIENT).
 *
 * Naming rules
 * ────────────
 * Entries shared across categories (same semantic, same renderer flag) appear
 * exactly once — e.g. DOTS is used by both LINE and RADAR, STACKED by AREA/BAR/PIE.
 * Entries that are category-specific and would otherwise collide are prefixed with
 * their category abbreviation (BAR_, LINE_, PIE_, RADAR_, RADIAL_).
 */
enum class ChartVariant {
    // ── Shared across multiple categories ─────────────────────────────────────
    DEFAULT,        // area-default, bar-default, line-default
    STACKED,        // area-stacked, bar-stacked, pie-stacked
    STACKED_EXPAND, // area-stacked-expand
    LINEAR,         // area-linear, line-linear
    STEP,           // area-step, line-step
    INTERACTIVE,    // area-interactive, line-interactive
    MULTIPLE,       // bar-multiple, line-multiple, radar-multiple
    DOTS,           // line-dots, radar-dots
    DOTS_CUSTOM,    // line-dots-custom
    DOTS_COLORS,    // line-dots-colors
    LABEL,          // bar-label, line-label, radial-label
    LABEL_CUSTOM,   // bar-label-custom, line-label-custom, radar-label-custom
    LEGEND,         // area-legend, radar-legend
    ACTIVE,         // bar-active, pie-donut-active

    // ── Area-specific ─────────────────────────────────────────────────────────
    GRADIENT,       // area-gradient
    AXES,           // area-axes

    // ── Bar-specific ──────────────────────────────────────────────────────────
    HORIZONTAL,     // bar-horizontal
    NEGATIVE,       // bar-negative
    MIXED,          // bar-mixed

    // ── Pie-specific ──────────────────────────────────────────────────────────
    SIMPLE,         // pie-simple
    DONUT,          // pie-donut
    DONUT_TEXT,     // pie-donut-text
    DONUT_ACTIVE,   // pie-donut-active
    PIE_LABEL,      // pie-label
    PIE_LABEL_CUSTOM, // pie-label-custom
    PIE_LABEL_LIST, // pie-label-list
    PIE_LEGEND,     // pie-legend
    PIE_SEPARATOR_NONE, // pie-separator-none

    // ── Radar-specific ────────────────────────────────────────────────────────
    GRID_CIRCLE,          // radar-grid-circle
    GRID_CIRCLE_FILL,     // radar-grid-circle-fill
    GRID_CIRCLE_NO_LINES, // radar-grid-circle-no-lines
    GRID_CUSTOM,          // radar-grid-custom
    GRID_FILL,            // radar-grid-fill
    GRID_NONE,            // radar-grid-none
    ICONS,                // radar-icons
    LINES_ONLY,           // radar-lines-only
    RADIUS,               // radar-radius

    // ── Radial-specific ───────────────────────────────────────────────────────
    GRID,           // radial-grid  (no radar collision: radar uses GRID_* prefixed names)
    SHAPE,          // radial-shape
    TEXT,           // radial-text
    RADIAL_STACKED, // radial-stacked (prefixed: STACKED is already used by area/bar/pie)
}

// ─────────────────────────────────────────────────────────────────────────────
//  Data model
// ─────────────────────────────────────────────────────────────────────────────

/**
 * One series of data.  [values] are raw floats; sign is preserved so negative
 * bars work correctly.  [colors] is optional — if empty the renderer falls back
 * to the theme palette (primary, secondary, tertiary…).
 */
data class ChartSeriesData(
    val label: String,
    val values: List<Float>,
    val colors: List<Color> = emptyList()
)

/**
 * Everything a chart card needs:  metadata for the header/footer, the drawing
 * category, and the actual series data.
 *
 * [xLabels] drives axis tick labels (months, days, …).
 * [footerTrend] is the optional footer body line (e.g. "January – June 2024").
 * [footerBadge] is the optional trending badge string (e.g. "↑ 5.2% this month").
 */
data class ChartRegistryItem(
    val name: String,
    val category: ChartTypeCategory,
    val variant: ChartVariant = ChartVariant.DEFAULT,
    val title: String,
    val description: String,
    val data: List<ChartSeriesData>,
    val xLabels: List<String> = emptyList(),
    val footerTrend: String = "",
    val footerBadge: String = "",
    /** For radial/radial-text: maximum value the arc represents (full circle). */
    val radialMaxValue: Float = 400f,
    /** Inner radius fraction (0..1) for donut / radial charts. */
    val innerRadiusFraction: Float = 0.55f,
    /** Whether bars / areas should stack. */
    val stacked: Boolean = false,
)