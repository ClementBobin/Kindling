package dev.kindling.core.components.ui.charts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ChartTypeCategory {
    AREA, LINE, BAR, PIE, RADAR, RADIAL
}

enum class ChartVariant {
    DEFAULT,
    STACKED,
    PERCENT,
    GRADIENT,
    DOTS,
    MULTIPLE,
    GRID_CIRCLE,
    GRID_CIRCLE_FILL,
    GRID_CIRCLE_NO_LINE,
    GRID_NONE,
    LINES_ONLY,
    LEGEND,
    RADIUS,
    SHAPE,
    TEXT,
    HORIZONTAL,
    ACTIVE,
    NEGATIVE
}

data class ChartSeries(
    val label: String,
    val values: List<Float>,
    val colors: List<Color> = emptyList()
)

data class ChartRegistryItem(
    val id: String = "",
    val category: ChartTypeCategory = ChartTypeCategory.AREA,
    val variant: ChartVariant = ChartVariant.DEFAULT,
    val title: String,
    val description: String? = null,
    val data: List<ChartSeries>,
    val xLabels: List<String> = emptyList(),
    val footerBadge: String = "",
    val footerTrend: String = "",
    val innerRadiusFraction: Float = 0.6f,
    val radialMaxValue: Float = 100f // Added property for radial charts
)

object ChartDefaults {
    val strokeWidth = 2.dp
    val dotRadius = 3.dp
    val barCorner = 4.dp
    const val areaFillAlpha = 0.2f
    const val radarFillAlpha = 0.2f
}