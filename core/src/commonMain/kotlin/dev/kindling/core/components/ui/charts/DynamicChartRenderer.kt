package dev.kindling.core.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// ─────────────────────────────────────────────────────────────────────────────
//  DynamicChartRenderer
//
//  Single entry-point that maps any [ChartRegistryItem] to its concrete
//  Kotlin/Compose renderer.  Mirrors the shadcn registry concept where each
//  chart file is registered under a category + variant name.
//
//  Usage
//  ─────
//  val item = ChartRegistryItem(
//      name        = "chart-area-gradient",
//      category    = ChartTypeCategory.AREA,
//      variant     = ChartVariant.GRADIENT,
//      title       = "Area Chart – Gradient",
//      description = "Showing total visitors for the last 6 months",
//      data        = listOf(desktopSeries, mobileSeries),
//      xLabels     = monthLabels,
//      footerBadge = "Trending up 5.2% this month",
//      footerTrend = "January – June 2024",
//  )
//  DynamicChartRenderer(item = item)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DynamicChartRenderer(
    item: ChartRegistryItem,
    modifier: Modifier = Modifier,
) {
    when (item.category) {

        // ── Area ──────────────────────────────────────────────────────────────
        ChartTypeCategory.AREA -> when (item.variant) {
            ChartVariant.GRADIENT -> KAreaChart(
                item      = item,
                gradient  = true,
                modifier  = modifier,
            )
            ChartVariant.STACKED -> KAreaChart(
                item     = item,
                stacked  = true,
                modifier = modifier,
            )
            ChartVariant.STACKED_EXPAND -> KAreaChart(
                item          = item,
                stacked       = true,
                expandToFull  = true,
                modifier      = modifier,
            )
            ChartVariant.LINEAR -> KAreaChart(
                item      = item,
                curveType = CurveType.LINEAR,
                modifier  = modifier,
            )
            ChartVariant.STEP -> KAreaChart(
                item      = item,
                curveType = CurveType.STEP,
                modifier  = modifier,
            )
            ChartVariant.LEGEND -> KAreaChart(
                item     = item,
                modifier = modifier,
                // UniversalChartCard's showLegend is toggled through the wrapper
                // by passing a specialised item; legend is always shown for LEGEND variant
            )
            else -> KAreaChart(item = item, modifier = modifier) // DEFAULT, AXES, INTERACTIVE, …
        }

        // ── Line ──────────────────────────────────────────────────────────────
        ChartTypeCategory.LINE -> when (item.variant) {
            ChartVariant.DOTS -> KLineChart(
                item     = item,
                showDots = true,
                modifier = modifier,
            )
            ChartVariant.DOTS_CUSTOM, ChartVariant.DOTS_COLORS -> KLineChart(
                item           = item,
                showDots       = true,
                dotsColorized  = true,
                modifier       = modifier,
            )
            ChartVariant.LINEAR -> KLineChart(
                item      = item,
                curveType = CurveType.LINEAR,
                modifier  = modifier,
            )
            ChartVariant.STEP -> KLineChart(
                item      = item,
                curveType = CurveType.STEP,
                modifier  = modifier,
            )
            else -> KLineChart(item = item, modifier = modifier) // DEFAULT, MULTIPLE, INTERACTIVE, …
        }

        // ── Bar ───────────────────────────────────────────────────────────────
        ChartTypeCategory.BAR -> when (item.variant) {
            ChartVariant.STACKED -> KBarChart(
                item         = item,
                stacked      = true,
                cornerRadius = BarCornerRadius.TOP_ONLY,
                modifier     = modifier,
            )
            ChartVariant.HORIZONTAL -> KHorizontalBarChart(
                item     = item,
                modifier = modifier,
            )
            ChartVariant.NEGATIVE -> KBarChart(
                item         = item,
                showNegative = true,
                cornerRadius = BarCornerRadius.ALL,
                modifier     = modifier,
            )
            ChartVariant.ACTIVE -> KBarChart(
                item        = item,
                activeIndex = 2,          // callers override this via a stateful wrapper
                modifier    = modifier,
            )
            else -> KBarChart(item = item, modifier = modifier) // DEFAULT, MULTIPLE, MIXED, LABEL, …
        }

        // ── Pie ───────────────────────────────────────────────────────────────
        ChartTypeCategory.PIE -> when (item.variant) {
            ChartVariant.DONUT -> KPieChart(
                item     = item,
                donut    = true,
                modifier = modifier,
            )
            ChartVariant.DONUT_TEXT -> KPieChart(
                item        = item,
                donut       = true,
                centerLabel = item.data.firstOrNull()?.values?.sum()
                    ?.toInt()?.toString() ?: "",
                modifier    = modifier,
            )
            ChartVariant.DONUT_ACTIVE -> KPieChart(
                item          = item,
                donut         = true,
                activeSegment = 0,         // callers supply the live index
                modifier      = modifier,
            )
            ChartVariant.PIE_LABEL, ChartVariant.PIE_LABEL_CUSTOM, ChartVariant.PIE_LABEL_LIST -> KPieChart(
                item       = item,
                showLabels = true,
                modifier   = modifier,
            )
            ChartVariant.PIE_SEPARATOR_NONE -> KPieChart(
                item             = item,
                separatorStroke  = 0f,
                modifier         = modifier,
            )
            else -> KPieChart(item = item, modifier = modifier) // SIMPLE, PIE_LEGEND, STACKED, …
        }

        // ── Radar ─────────────────────────────────────────────────────────────
        ChartTypeCategory.RADAR -> when (item.variant) {
            ChartVariant.DOTS -> KRadarChart(
                item     = item,
                showDots = true,
                modifier = modifier,
            )
            ChartVariant.GRID_CIRCLE -> KRadarChart(
                item     = item,
                gridType = RadarGridType.CIRCLE,
                modifier = modifier,
            )
            ChartVariant.GRID_CIRCLE_FILL -> KRadarChart(
                item     = item,
                gridType = RadarGridType.CIRCLE_FILL,
                modifier = modifier,
            )
            ChartVariant.GRID_CIRCLE_NO_LINES -> KRadarChart(
                item          = item,
                gridType      = RadarGridType.CIRCLE,
                showAxisLines = false,
                modifier      = modifier,
            )
            ChartVariant.GRID_NONE -> KRadarChart(
                item     = item,
                gridType = RadarGridType.NONE,
                modifier = modifier,
            )
            ChartVariant.LINES_ONLY -> KRadarChart(
                item          = item,
                gridType      = RadarGridType.NONE,
                showAxisLines = true,
                modifier      = modifier,
            )
            else -> KRadarChart(item = item, modifier = modifier) // DEFAULT, MULTIPLE, LEGEND, …
        }

        // ── Radial ────────────────────────────────────────────────────────────
        ChartTypeCategory.RADIAL -> when (item.variant) {
            ChartVariant.TEXT -> KRadialChart(
                item           = item,
                startAngle     = 0f,
                endAngle       = 250f,
                centerLabel    = item.data.firstOrNull()?.values?.firstOrNull()
                    ?.toInt()?.toString() ?: "",
                centerSubLabel = item.data.firstOrNull()?.label ?: "",
                cornerRadius   = true,
                modifier       = modifier,
            )
            ChartVariant.GRID -> KRadialChart(
                item           = item,
                showGrid       = true,
                showBackground = false,
                modifier       = modifier,
            )
            ChartVariant.SHAPE -> KRadialChart(
                item         = item,
                cornerRadius = true,
                modifier     = modifier,
            )
            ChartVariant.RADIAL_STACKED -> KRadialChart(
                item           = item,
                showBackground = false,
                modifier       = modifier,
            )
            else -> KRadialChart(item = item, modifier = modifier) // SIMPLE, LABEL, …
        }

        // ── Tooltip (no standalone canvas — just uses the card) ───────────────
        ChartTypeCategory.TOOLTIP -> KLineChart(item = item, modifier = modifier)
    }
}