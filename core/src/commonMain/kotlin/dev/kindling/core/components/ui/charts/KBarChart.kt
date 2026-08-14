package dev.kindling.core.components.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import dev.kindling.core.theme.kindlingColors

// ─────────────────────────────────────────────────────────────────────────────
//  Bar Chart
//  Covers: chart-bar-default, chart-bar-multiple, chart-bar-stacked,
//          chart-bar-horizontal, chart-bar-negative, chart-bar-mixed,
//          chart-bar-label, chart-bar-label-custom, chart-bar-active
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A fully-themed vertical bar chart that mirrors the shadcn `<BarChart>` component.
 *
 * @param stacked        When true, series are stacked on top of each other.
 * @param showNegative   When true, bars below zero are drawn in chart2 color.
 * @param cornerRadius   Rounding applied to bar tops.
 * @param showGridLines  Horizontal grid lines toggle.
 * @param activeIndex    When >= 0, highlights that x-index and mutes all others.
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
    val colors = MaterialTheme.kindlingColors
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

            val allValues = item.data.flatMap { it.values }
            val maxAbsVal: Float = if (stacked)
                item.data.safeStackedMaxValue()
            else
                allValues.maxOf { kotlin.math.abs(it) }.let { if (it == 0f) 1f else it }

            val hasNegative = showNegative && allValues.any { it < 0f }
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
            val barWidth: Float = if (stacked)
                groupWidth - 2 * barPad
            else
                (groupWidth - 2 * barPad) / seriesCount - 2.dp.toPx()

            if (stacked) {
                repeat(dataCount) { xi ->
                    var stackedHeight = 0f
                    val groupX = xi * groupWidth + barPad
                    val isMuted = activeIndex >= 0 && xi != activeIndex

                    item.data.forEachIndexed { si, series ->
                        val value = series.values.getOrElse(xi) { 0f }
                        val barH = (value / maxAbsVal) * zeroY
                        val baseColor: Color = colors.atIndex(si)
                        val color: Color = if (isMuted) baseColor.copy(alpha = 0.3f) else baseColor
                        val isBottom = si == 0
                        val isTop = si == item.data.size - 1

                        drawRoundedBar(
                            x = groupX,
                            y = zeroY - stackedHeight - barH,
                            width = barWidth,
                            height = barH,
                            color = color,
                            cornerRadiusPx = cr.toPx(),
                            topRounded = isTop,
                            bottomRounded = isBottom,
                        )
                        stackedHeight += barH
                    }
                }
            } else {
                item.data.forEachIndexed { si, series ->
                    series.values.forEachIndexed { xi, value ->
                        val groupX = xi * groupWidth + barPad
                        val x = groupX + si * (barWidth + 2.dp.toPx())
                        val isMuted = activeIndex >= 0 && xi != activeIndex
                        val isPositive = value >= 0f

                        val baseColor: Color = if (showNegative && !isPositive)
                            colors.chart2
                        else
                            colors.atIndex(si)

                        val color: Color = if (isMuted) baseColor.copy(alpha = 0.3f) else baseColor
                        val barH = (kotlin.math.abs(value) / maxAbsVal) * zeroY
                        val y = if (isPositive) zeroY - barH else zeroY

                        drawRoundedBar(
                            x = x,
                            y = y,
                            width = barWidth,
                            height = barH,
                            color = color,
                            cornerRadiusPx = cr.toPx(),
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
//  Horizontal Bar Chart  —  chart-bar-horizontal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun KHorizontalBarChart(
    item: ChartRegistryItem,
    showGridLines: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.kindlingColors
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
            val maxVal = item.data.safeMaxValue()

            if (showGridLines) {
                repeat(5) { i ->
                    val x = size.width * i / 4
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 0.5.dp.toPx())
                }
            }

            val rowHeight = size.height / dataCount
            val barPad = rowHeight * 0.2f
            val barH = rowHeight - 2 * barPad
            val crPx = cr.toPx()

            item.data.forEachIndexed { si, s ->
                val color: Color = colors.atIndex(si)
                s.values.forEachIndexed { xi, v ->
                    val barW = (v / maxVal) * size.width
                    val y = xi * rowHeight + barPad
                    drawRoundedBar(
                        x = 0f,
                        y = y,
                        width = barW,
                        height = barH,
                        color = color,
                        cornerRadiusPx = crPx,
                        topRounded = false,
                        bottomRounded = false,
                        allRounded = true,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Corner radius enum
// ─────────────────────────────────────────────────────────────────────────────

enum class BarCornerRadius { NONE, TOP_ONLY, ALL }

// ─────────────────────────────────────────────────────────────────────────────
//  Rounded bar helper — uses Path + RoundRect instead of drawRoundedRect
//  (DrawScope.drawRoundedRect is not available on all KMP targets)
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawRoundedBar(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: Color,
    cornerRadiusPx: Float,
    topRounded: Boolean,
    bottomRounded: Boolean,
    allRounded: Boolean = false,
) {
    if (width <= 0f || height <= 0f) return
    val r = minOf(cornerRadiusPx, width / 2f, height / 2f)

    if (!topRounded && !bottomRounded && !allRounded) {
        drawRect(color, Offset(x, y), Size(width, height))
        return
    }

    val topR    = if (topRounded    || allRounded) CornerRadius(r, r) else CornerRadius.Zero
    val bottomR = if (bottomRounded || allRounded) CornerRadius(r, r) else CornerRadius.Zero

    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left   = x,
                top    = y,
                right  = x + width,
                bottom = y + height,
                topLeftCornerRadius     = topR,
                topRightCornerRadius    = topR,
                bottomLeftCornerRadius  = bottomR,
                bottomRightCornerRadius = bottomR,
            )
        )
    }
    drawPath(path, color)
}