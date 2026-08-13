package dev.kindling.core.components.ui.charts

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
 */
enum class ChartVariant {
    // Area
    DEFAULT, GRADIENT, STACKED, STACKED_EXPAND, LINEAR, STEP, AXES, LEGEND, INTERACTIVE,
    // Bar
    MULTIPLE, STACKED, HORIZONTAL, NEGATIVE, LABEL, LABEL_CUSTOM, MIXED, ACTIVE,
    // Line
    DOTS, DOTS_CUSTOM, DOTS_COLORS, LABEL, LABEL_CUSTOM, MULTIPLE, LINEAR, STEP, INTERACTIVE,
    // Pie
    SIMPLE, DONUT, DONUT_TEXT, DONUT_ACTIVE, PIE_LABEL, PIE_LABEL_CUSTOM, PIE_LABEL_LIST,
    PIE_LEGEND, PIE_SEPARATOR_NONE, STACKED,
    // Radar
    DOTS, GRID_CIRCLE, GRID_CIRCLE_FILL, GRID_CIRCLE_NO_LINES, GRID_CUSTOM, GRID_FILL,
    GRID_NONE, ICONS, LEGEND, LINES_ONLY, MULTIPLE, RADIUS, LABEL_CUSTOM,
    // Radial
    GRID, LABEL, SHAPE, TEXT, RADIAL_STACKED
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