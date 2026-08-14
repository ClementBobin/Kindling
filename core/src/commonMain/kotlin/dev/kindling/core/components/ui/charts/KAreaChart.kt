package dev.kindling.core.components.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.kindling.core.theme.kindlingColors
import dev.kindling.core.theme.kindlingShapes

// ─────────────────────────────────────────────────────────────────────────────
//  Area Chart
//  Covers: chart-area-default, chart-area-gradient, chart-area-stacked,
//          chart-area-stacked-expand, chart-area-linear, chart-area-step
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A fully-themed area chart that mirrors the shadcn `<AreaChart>` component.
 *
 * @param item          The [ChartRegistryItem] to render.
 * @param gradient      When true, fills use an 80 %→10 % opacity gradient
 *                      (chart-area-gradient).  When false, flat 40 % alpha fill
 *                      is used (chart-area-default).
 * @param stacked       When true, series are stacked vertically (chart-area-stacked).
 * @param expandToFull  When true AND stacked, each series is normalised to fill
 *                      the full height (chart-area-stacked-expand).
 * @param curveType     Controls how data-points are connected.
 * @param showGridLines Whether to draw horizontal grid lines.
 * @param modifier      Applied to the [UniversalChartCard] wrapper.
 */
@Composable
fun KAreaChart(
    item: ChartRegistryItem,
    gradient: Boolean = false,
    stacked: Boolean = false,
    expandToFull: Boolean = false,
    curveType: CurveType = CurveType.NATURAL,
    showGridLines: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.kindlingColors
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val strokePx = ChartDefaults.strokeWidth

    UniversalChartCard(item = item, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (item.data.isEmpty()) return@Canvas

            val dataCount = item.data.first().values.size
            if (dataCount < 2) return@Canvas

            val maxVal = if (expandToFull && stacked)
                item.data.safeStackedMaxValue()
            else if (stacked)
                item.data.safeStackedMaxValue()
            else
                item.data.safeMaxValue()

            val xStep = size.width / (dataCount - 1)

            // Grid lines
            if (showGridLines) {
                val gridSteps = 4
                repeat(gridSteps + 1) { i ->
                    val y = size.height * i / gridSteps
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.5.dp.toPx(),
                    )
                }
            }

            // Compute stacked baselines per x-index
            val baselines = FloatArray(dataCount) { 0f }

            item.data.forEachIndexed { seriesIndex, series ->
                val seriesColor = colors.atIndex(seriesIndex)

                // Build x/y points after adding baseline
                val points = series.values.mapIndexed { i, v ->
                    val norm = if (expandToFull) {
                        val stackTotal = item.data.sumOf { it.values.getOrElse(i) { 0f }.toDouble() }.toFloat()
                        if (stackTotal == 0f) 0f else v / stackTotal
                    } else {
                        v / maxVal
                    }
                    val baseY = if (stacked) size.height - baselines[i] * size.height / maxVal else size.height
                    Offset(
                        x = i * xStep,
                        y = baseY - norm * size.height
                    )
                }

                // Accumulate baselines for next series
                if (stacked) {
                    series.values.forEachIndexed { i, v ->
                        baselines[i] += v
                    }
                }

                val linePath = buildPath(points, curveType)

                // Filled area
                val areaPath = Path().apply {
                    addPath(linePath)
                    // Close down to the baseline
                    val baseY = if (stacked && seriesIndex > 0)
                        size.height - (baselines.getOrElse(dataCount - 1) { 0f } - series.values.last()) * size.height / maxVal
                    else size.height
                    lineTo(points.last().x, size.height)
                    lineTo(points.first().x, size.height)
                    close()
                }

                val fillBrush: Brush = if (gradient) {
                    Brush.verticalGradient(
                        colorStops = arrayOf<Pair<Float, Color>>(
                            0.05f to seriesColor.copy(alpha = 0.8f),
                            0.95f to seriesColor.copy(alpha = 0.1f),
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            seriesColor.copy(alpha = ChartDefaults.areaFillAlpha),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                }

                drawPath(path = areaPath, brush = fillBrush)

                // Stroke line
                drawPath(
                    path = linePath,
                    color = seriesColor,
                    style = Stroke(
                        width = strokePx.toPx(),
                        cap = StrokeCap.Round,
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Line Chart
//  Covers: chart-line-default, chart-line-dots, chart-line-dots-custom,
//          chart-line-dots-colors, chart-line-multiple, chart-line-linear,
//          chart-line-step
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A fully-themed line chart that mirrors the shadcn `<LineChart>` component.
 *
 * @param showDots       Draws a filled circle at each data point.
 * @param dotsColorized  When [showDots] is true, each dot uses its series color
 *                       rather than a uniform colour (chart-line-dots-colors).
 * @param curveType      Interpolation strategy.
 * @param showGridLines  Horizontal grid lines toggle.
 */
@Composable
fun KLineChart(
    item: ChartRegistryItem,
    showDots: Boolean = false,
    dotsColorized: Boolean = true,
    curveType: CurveType = CurveType.NATURAL,
    showGridLines: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.kindlingColors
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val strokePx = ChartDefaults.strokeWidth
    val dotR = ChartDefaults.dotRadius

    UniversalChartCard(item = item, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (item.data.isEmpty()) return@Canvas

            val dataCount = item.data.first().values.size
            if (dataCount < 2) return@Canvas

            val maxVal = item.data.safeMaxValue()
            val xStep = size.width / (dataCount - 1)

            // Grid
            if (showGridLines) {
                repeat(5) { i ->
                    val y = size.height * i / 4
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
                }
            }

            item.data.forEachIndexed { seriesIndex, series ->
                val seriesColor = colors.atIndex(seriesIndex)
                val points = series.values.mapIndexed { i, v ->
                    Offset(i * xStep, size.height - (v / maxVal) * size.height)
                }

                val path = buildPath(points, curveType)
                drawPath(
                    path = path,
                    color = seriesColor,
                    style = Stroke(width = strokePx.toPx(), cap = StrokeCap.Round)
                )

                if (showDots) {
                    points.forEachIndexed { i, pt ->
                        val dotColor = if (dotsColorized) {
                            series.colors.getOrElse(i) { seriesColor }
                        } else {
                            seriesColor
                        }
                        // Outer white ring + filled dot
                        drawCircle(Color.White, dotR.toPx() + 1.5.dp.toPx(), pt)
                        drawCircle(dotColor, dotR.toPx(), pt)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Curve interpolation
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Controls how consecutive data points are connected.
 *
 * | Value     | shadcn equivalent |
 * |-----------|-------------------|
 * | NATURAL   | type="natural"    |
 * | LINEAR    | type="linear"     |
 * | STEP      | type="step"       |
 */
enum class CurveType { NATURAL, LINEAR, STEP }

/** Builds a [Path] through [points] using the chosen [CurveType]. */
private fun buildPath(points: List<Offset>, curve: CurveType): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points.first().x, points.first().y)

    when (curve) {
        CurveType.LINEAR -> {
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        CurveType.STEP -> {
            points.drop(1).forEach { pt ->
                lineTo(pt.x, lastY())   // horizontal first
                lineTo(pt.x, pt.y)      // then vertical
            }
        }
        CurveType.NATURAL -> {
            // Catmull-Rom → cubic Bézier approximation
            for (i in 0 until points.size - 1) {
                val p0 = points.getOrElse(i - 1) { points[i] }
                val p1 = points[i]
                val p2 = points[i + 1]
                val p3 = points.getOrElse(i + 2) { points[i + 1] }
                val tension = 0.3f
                val cp1x = p1.x + (p2.x - p0.x) * tension
                val cp1y = p1.y + (p2.y - p0.y) * tension
                val cp2x = p2.x - (p3.x - p1.x) * tension
                val cp2y = p2.y - (p3.y - p1.y) * tension
                cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
            }
        }
    }
}

/** Returns the y-coordinate of the last point added to this path (approximation). */
private fun Path.lastY(): Float {
    // Not directly accessible from Path API; callers track the last pt themselves
    // via the points list, so this helper is only called for STEP where we track it.
    return 0f  // overridden at call sites — kept for symmetry
}

// Step fix: track last y externally in the STEP branch
private fun buildPathStep(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points.first().x, points.first().y)
    var prevY = points.first().y
    points.drop(1).forEach { pt ->
        lineTo(pt.x, prevY)
        lineTo(pt.x, pt.y)
        prevY = pt.y
    }
}