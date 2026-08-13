package dev.kindling.core.components.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  Bar Chart
//  Covers: chart-bar-default, chart-bar-multiple, chart-bar-stacked,
//          chart-bar-horizontal, chart-bar-negative, chart-bar-mixed,
//          chart-bar-label, chart-bar-label-custom, chart-bar-active
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A fully-themed vertical bar chart that mirrors the shadcn `<BarChart>` component.
 *
 * @param stacked        When true, series are stacked on top of each other
 *                       (chart-bar-stacked).
 * @param showNegative   When true, bars below zero are drawn in [negativeColor]
 *                       (chart-bar-negative).
 * @param cornerRadius   Rounding applied to bar tops.  Use [BarCornerRadius.TOP_ONLY]
 *                       for stacked (outer corners only), [BarCornerRadius.ALL] for
 *                       single-series charts.
 * @param showGridLines  Horizontal grid lines toggle.
 * @param activeIndex    When >= 0, draws the bar at that x-index with full opacity
 *                       and all others muted (chart-bar-active).
 */
@Composable
fun KBarChart(
    item: ChartRegistryItem,
    stacked: Boolean = false,
    showNegative: Boolean = false,
    cornerRadius: BarCornerRadius = BarCornerRadius.ALL,
    showGridLines: Boolean = true,
    activeIndex: Int = -1,
    modifier: Modifier = Modifier,
) {
    val colors = KindlingChartColors.fromMaterial3()
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val cr = ChartDefaults.barCorner

    UniversalChartCard(item = item, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (item.data.isEmpty()) return@Canvas

            val dataCount = item.data.first().values.size
            val seriesCount = item.data.size

            // Determine value range (negative bars need a zero baseline)
            val allValues = item.data.flatMap { it.values }
            val maxAbsVal = if (stacked)
                item.data.safeStackedMaxValue()
            else
                allValues.maxOf { kotlin.math.abs(it) }.let { if (it == 0f) 1f else it }

            val hasNegative = showNegative && allValues.any { it < 0f }
            // Y coordinate that represents value = 0
            val zeroY = if (hasNegative) size.height / 2f else size.height

            // Grid
            if (showGridLines) {
                repeat(5) { i ->
                    val y = size.height * i / 4
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
                }
            }

            val groupWidth = size.width / dataCount
            val barPad = groupWidth * 0.15f
            val barWidth = if (stacked) groupWidth - 2 * barPad
            else (groupWidth - 2 * barPad) / seriesCount - 2.dp.toPx()

            if (stacked) {
                // ── Stacked bars ──────────────────────────────────────────────
                repeat(dataCount) { xi ->
                    var stackedHeight = 0f
                    val groupX = xi * groupWidth + barPad
                    val isMuted = activeIndex >= 0 && xi != activeIndex

                    item.data.forEachIndexed { si, series ->
                        val value = series.values.getOrElse(xi) { 0f }
                        val barH = (value / maxAbsVal) * zeroY
                        val color = colors.atIndex(si).let {
                            if (isMuted) it.copy(alpha = 0.3f) else it
                        }
                        val isBottom = si == 0
                        val isTop = si == item.data.size - 1

                        drawRoundedBar(
                            x = groupX,
                            y = zeroY - stackedHeight - barH,
                            width = barWidth,
                            height = barH,
                            color = color,
                            cornerRadiusDp = cr.toPx(),
                            topRounded = isTop,
                            bottomRounded = isBottom,
                        )
                        stackedHeight += barH
                    }
                }
            } else {
                // ── Grouped bars ─────────────────────────────────────────────
                item.data.forEachIndexed { si, series ->
                    series.values.forEachIndexed { xi, value ->
                        val groupX = xi * groupWidth + barPad
                        val x = groupX + si * (barWidth + 2.dp.toPx())
                        val isMuted = activeIndex >= 0 && xi != activeIndex
                        val isPositive = value >= 0f

                        val baseColor = if (showNegative && !isPositive)
                            colors.chart2  // negative colour (chart-2)
                        else
                            colors.atIndex(si)

                        val color = if (isMuted) baseColor.copy(alpha = 0.3f) else baseColor
                        val barH = (kotlin.math.abs(value) / maxAbsVal) * zeroY
                        val y = if (isPositive) zeroY - barH else zeroY

                        drawRoundedBar(
                            x = x,
                            y = y,
                            width = barWidth,
                            height = barH,
                            color = color,
                            cornerRadiusDp = cr.toPx(),
                            topRounded = cornerRadius != BarCornerRadius.NONE,
                            bottomRounded = cornerRadius == BarCornerRadius.ALL,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Horizontal Bar Chart
//  Covers: chart-bar-horizontal
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A horizontal bar chart.  Equivalent to shadcn's `<BarChart layout="vertical">`.
 */
@Composable
fun KHorizontalBarChart(
    item: ChartRegistryItem,
    showGridLines: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = KindlingChartColors.fromMaterial3()
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val cr = ChartDefaults.barCorner

    UniversalChartCard(item = item, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (item.data.isEmpty()) return@Canvas

            val series = item.data.first()
            val dataCount = series.values.size
            val maxVal = item.data.safeMaxValue()

            // Vertical grid lines
            if (showGridLines) {
                repeat(5) { i ->
                    val x = size.width * i / 4
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 0.5.dp.toPx())
                }
            }

            val rowHeight = size.height / dataCount
            val barPad = rowHeight * 0.2f
            val barH = rowHeight - 2 * barPad
            val cr4 = cr.toPx()

            item.data.forEachIndexed { si, s ->
                val color = colors.atIndex(si)
                s.values.forEachIndexed { xi, v ->
                    val barW = (v / maxVal) * size.width
                    val y = xi * rowHeight + barPad
                    drawRoundedRect(
                        color = color,
                        topLeft = Offset(0f, y),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(cr4, cr4)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Rounding helper
// ─────────────────────────────────────────────────────────────────────────────

enum class BarCornerRadius { NONE, TOP_ONLY, ALL }

private fun DrawScope.drawRoundedBar(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: Color,
    cornerRadiusDp: Float,
    topRounded: Boolean,
    bottomRounded: Boolean,
) {
    val r = minOf(cornerRadiusDp, width / 2f, height / 2f)
    if (!topRounded && !bottomRounded) {
        drawRect(color, Offset(x, y), Size(width, height))
        return
    }
    drawRoundedRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(r, r)
    )
    // Flatten undesired corners by overdrawing a square rect on top or bottom half
    if (!bottomRounded) {
        drawRect(color, Offset(x, y + height / 2f), Size(width, height / 2f))
    }
    if (!topRounded) {
        drawRect(color, Offset(x, y), Size(width, height / 2f))
    }
}