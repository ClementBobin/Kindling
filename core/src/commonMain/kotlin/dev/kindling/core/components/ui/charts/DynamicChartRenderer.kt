package dev.kindling.core.components.ui.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DynamicChartRenderer(
    item: ChartRegistryItem,
    modifier: Modifier = Modifier
) {
    when (item.category) {
        ChartTypeCategory.AREA -> {
            when (item.variant) {
                ChartVariant.STACKED -> KAreaChart(item = item, stacked = true, modifier = modifier)
                ChartVariant.PERCENT -> KAreaChart(item = item, stacked = true, expandToFull = true, modifier = modifier)
                ChartVariant.GRADIENT -> KAreaChart(item = item, gradient = true, modifier = modifier)
                ChartVariant.DOTS -> KAreaChart(item = item, modifier = modifier)
                else -> KAreaChart(item = item, modifier = modifier)
            }
        }
        ChartTypeCategory.LINE -> {
            when (item.variant) {
                ChartVariant.DOTS -> KLineChart(item = item, showDots = true, modifier = modifier)
                ChartVariant.MULTIPLE -> KLineChart(item = item, showDots = true, modifier = modifier)
                ChartVariant.LINES_ONLY -> KLineChart(item = item, showDots = false, modifier = modifier)
                else -> KLineChart(item = item, modifier = modifier)
            }
        }
        ChartTypeCategory.BAR -> {
            when (item.variant) {
                ChartVariant.STACKED -> KBarChart(item = item, stacked = true, modifier = modifier)
                ChartVariant.HORIZONTAL -> KHorizontalBarChart(item = item, modifier = modifier)
                ChartVariant.ACTIVE -> KBarChart(item = item, activeIndex = 0, modifier = modifier)
                ChartVariant.NEGATIVE -> KBarChart(item = item, showNegative = true, modifier = modifier)
                else -> KBarChart(item = item, modifier = modifier)
            }
        }
        ChartTypeCategory.PIE -> {
            when (item.variant) {
                ChartVariant.STACKED -> KPieChart(item = item, donut = true, centerLabel = "Total", modifier = modifier)
                else -> KPieChart(item = item, donut = false, modifier = modifier)
            }
        }
        ChartTypeCategory.RADAR -> {
            when (item.variant) {
                ChartVariant.DOTS -> KRadarChart(item = item, showDots = true, modifier = modifier)
                ChartVariant.GRID_CIRCLE -> KRadarChart(item = item, gridType = RadarGridType.CIRCLE, modifier = modifier)
                ChartVariant.GRID_CIRCLE_FILL -> KRadarChart(item = item, gridType = RadarGridType.CIRCLE_FILL, modifier = modifier)
                ChartVariant.GRID_CIRCLE_NO_LINE -> KRadarChart(item = item, gridType = RadarGridType.CIRCLE, showAxisLines = false, modifier = modifier)
                ChartVariant.GRID_NONE -> KRadarChart(item = item, gridType = RadarGridType.NONE, modifier = modifier)
                else -> KRadarChart(item = item, modifier = modifier)
            }
        }
        ChartTypeCategory.RADIAL -> {
            when (item.variant) {
                ChartVariant.STACKED -> KRadialChart(item = item, showBackground = true, modifier = modifier)
                ChartVariant.TEXT -> KRadialChart(item = item, centerLabel = "1,250", centerSubLabel = "Visitors", modifier = modifier)
                else -> KRadialChart(item = item, modifier = modifier)
            }
        }
    }
}