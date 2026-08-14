package dev.kindling.core.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
//  Radar Chart
//  Covers: chart-radar-default, chart-radar-dots, chart-radar-multiple,
//          chart-radar-grid-circle, chart-radar-grid-circle-fill,
//          chart-radar-grid-circle-no-lines, chart-radar-grid-custom,
//          chart-radar-grid-fill, chart-radar-grid-none,
//          chart-radar-lines-only, chart-radar-legend, chart-radar-radius
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Controls the background grid drawn behind the radar polygon.
 *
 * | Value        | shadcn equivalent                          |
 * |--------------|---------------------------------------------|
 * | POLYGON      | default `<PolarGrid />`  (spiderweb lines)  |
 * | CIRCLE       | `<PolarGrid gridType="circle" />`           |
 * | CIRCLE_FILL  | circle grid + filled innermost ring         |
 * | NONE         | chart-radar-grid-none (no grid drawn)       |
 */
enum class RadarGridType { POLYGON, CIRCLE, CIRCLE_FILL, NONE }

/**
 * A fully-themed radar (spider) chart that mirrors the shadcn `<RadarChart>` component.
 *
 * @param gridType    Background grid style — see [RadarGridType].
 * @param showDots    Draw a dot at each vertex of the radar polygon.
 * @param showAxisLines  Draw spokes from centre to each axis label.
 *                    Pass false for chart-radar-lines-only and
 *                    chart-radar-grid-circle-no-lines variants.
 * @param gridLevels  Number of concentric rings / polygons to draw.
 */
@Composable
fun KRadarChart(
    item: ChartRegistryItem,
    gridType: RadarGridType = RadarGridType.POLYGON,
    showDots: Boolean = false,
    showAxisLines: Boolean = true,
    gridLevels: Int = 4,
    modifier: Modifier = Modifier,
) {
    val colors       = KindlingChartColors.fromMaterial3()
    val gridColor    = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    UniversalChartCard(item = item, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            if (item.data.isEmpty()) return@Canvas

            val axes      = item.xLabels.size.takeIf { it > 0 }
                ?: item.data.first().values.size
            if (axes < 3) return@Canvas

            val cx     = size.width  / 2f
            val cy     = size.height / 2f
            val radius = minOf(cx, cy) * 0.75f
            val maxVal = item.data.safeMaxValue()

            // ── Grid ──────────────────────────────────────────────────────────
            when (gridType) {
                RadarGridType.POLYGON, RadarGridType.CIRCLE, RadarGridType.CIRCLE_FILL -> {
                    repeat(gridLevels) { level ->
                        val r = radius * (level + 1) / gridLevels
                        if (gridType == RadarGridType.POLYGON) {
                            drawPolygon(axes, cx, cy, r, gridColor, filled = false)
                        } else {
                            val filled = gridType == RadarGridType.CIRCLE_FILL && level == 0
                            if (filled) {
                                drawCircle(surfaceColor.copy(alpha = 0.6f), r, Offset(cx, cy))
                            }
                            drawCircle(gridColor, r, Offset(cx, cy), style = Stroke(0.5.dp.toPx()))
                        }
                    }
                    // Axis spoke lines
                    if (showAxisLines) {
                        repeat(axes) { i ->
                            val angle = axisAngle(i, axes)
                            val end   = Offset(cx + radius * cos(angle), cy + radius * sin(angle))
                            drawLine(gridColor, Offset(cx, cy), end, 0.5.dp.toPx())
                        }
                    }
                }
                RadarGridType.NONE -> { /* no grid */ }
            }

            // ── Series polygons ───────────────────────────────────────────────
            item.data.forEachIndexed { si, series ->
                val color = colors.atIndex(si)
                val points = List(axes) { i ->
                    val v     = series.values.getOrElse(i) { 0f }
                    val angle = axisAngle(i, axes)
                    val r     = (v / maxVal) * radius
                    Offset(cx + r * cos(angle), cy + r * sin(angle))
                }

                // Filled area
                val fillPath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }
                drawPath(fillPath, color.copy(alpha = ChartDefaults.radarFillAlpha))

                // Stroke outline
                drawPath(
                    path = fillPath,
                    color = color,
                    style = Stroke(width = ChartDefaults.strokeWidth.toPx())
                )

                // Vertex dots
                if (showDots) {
                    points.forEach { pt ->
                        drawCircle(Color.White, ChartDefaults.dotRadius.toPx() + 1.5.dp.toPx(), pt)
                        drawCircle(color, ChartDefaults.dotRadius.toPx(), pt)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Internal helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Angle (radians) of axis [i] for an [axes]-sided polygon starting at 12-o'clock. */
private fun axisAngle(i: Int, axes: Int): Float =
    (2.0 * PI * i / axes - PI / 2.0).toFloat()

/** Draws a regular polygon outline (or filled) at [cx],[cy] with given [radius]. */
private fun DrawScope.drawPolygon(
    sides: Int,
    cx: Float,
    cy: Float,
    radius: Float,
    color: Color,
    filled: Boolean,
) {
    val path = Path().apply {
        repeat(sides) { i ->
            val angle = axisAngle(i, sides)
            val x = cx + radius * cos(angle)
            val y = cy + radius * sin(angle)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    if (filled) {
        drawPath(path, color)
    } else {
        drawPath(path, color, style = Stroke(width = 0.5.dp.toPx()))
    }
}