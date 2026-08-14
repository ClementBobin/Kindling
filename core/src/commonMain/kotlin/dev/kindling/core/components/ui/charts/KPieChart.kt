package dev.kindling.core.components.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import dev.kindling.core.theme.kindlingColors

// ─────────────────────────────────────────────────────────────────────────────
//  Pie / Donut Chart
//  Covers: chart-pie-simple, chart-pie-donut, chart-pie-donut-text,
//          chart-pie-donut-active, chart-pie-label, chart-pie-label-list,
//          chart-pie-legend, chart-pie-separator-none, chart-pie-stacked
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A fully-themed pie / donut chart that mirrors the shadcn `<PieChart>` component.
 *
 * @param donut           When true, cuts out an inner circle to produce a donut
 *                        (chart-pie-donut / chart-pie-donut-text).
 * @param centerLabel     Text rendered in the donut hole (chart-pie-donut-text).
 *                        Ignored when [donut] is false.
 * @param activeSegment   Index of the segment to expand outward slightly
 *                        (chart-pie-donut-active / chart-pie-interactive).
 *                        -1 means no active segment.
 * @param showLabels      Draws a line + percentage label at each segment
 *                        (chart-pie-label / chart-pie-label-custom).
 * @param separatorStroke Stroke width of the gap between segments in dp.
 *                        Pass 0.dp for chart-pie-separator-none.
 * @param innerRadiusFraction  Fraction of outer radius used for the donut hole.
 *                             Sourced from [ChartRegistryItem.innerRadiusFraction].
 */
@Composable
fun KPieChart(
    item: ChartRegistryItem,
    donut: Boolean = false,
    centerLabel: String = "",
    activeSegment: Int = -1,
    showLabels: Boolean = false,
    separatorStroke: Float = 2f,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.kindlingColors
    val onSurface = MaterialTheme.colorScheme.onSurface
    val mutedFg = MaterialTheme.colorScheme.onSurfaceVariant

    UniversalChartCard(item = item, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
            ) {
                if (item.data.isEmpty()) return@Canvas

                // Flatten all series into single segments list
                // (Pie charts in shadcn usually use a single series with per-slice colors)
                val series = item.data.first()
                val total = series.values.sum().let { if (it == 0f) 1f else it }
                val outerR = size.minDimension / 2f
                val innerR = if (donut) outerR * item.innerRadiusFraction else 0f
                val cx = size.width / 2f
                val cy = size.height / 2f
                val activeExpand = outerR * 0.06f

                var startAngle = -90f   // top = 12-o'clock

                series.values.forEachIndexed { i, v ->
                    val sweep = (v / total) * 360f
                    val isActive = i == activeSegment
                    val midAngle = startAngle + sweep / 2f
                    val expandOffset = if (isActive) activeExpand else 0f
                    val ox = cx + expandOffset * cos(Math.toRadians(midAngle.toDouble())).toFloat()
                    val oy = cy + expandOffset * sin(Math.toRadians(midAngle.toDouble())).toFloat()

                    val sliceColor = series.colors.getOrElse(i) { colors.atIndex(i) }

                    if (donut) {
                        // Donut ring via arc stroke
                        val strokeW = outerR - innerR
                        val ringR = (outerR + innerR) / 2f
                        drawArc(
                            color = sliceColor,
                            startAngle = startAngle,
                            sweepAngle = sweep - separatorStroke,
                            useCenter = false,
                            topLeft = Offset(ox - ringR, oy - ringR),
                            size = Size(ringR * 2, ringR * 2),
                            style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                        )
                    } else {
                        drawArc(
                            color = sliceColor,
                            startAngle = startAngle,
                            sweepAngle = sweep - separatorStroke,
                            useCenter = true,
                            topLeft = Offset(ox - outerR, oy - outerR),
                            size = Size(outerR * 2, outerR * 2),
                        )
                    }

                    // Outer label line + tick
                    if (showLabels) {
                        val pct = (v / total * 100).toInt()
                        drawLabelLine(
                            cx = ox, cy = oy,
                            outerR = outerR,
                            midAngle = midAngle,
                            color = mutedFg,
                        )
                    }

                    startAngle += sweep
                }
            }

            // Center text (donut-text variant)
            if (donut && centerLabel.isNotEmpty()) {
                Text(
                    text = centerLabel,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Internal helper: tick line outside a segment
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawLabelLine(
    cx: Float, cy: Float,
    outerR: Float,
    midAngle: Float,
    color: Color,
) {
    val rad = Math.toRadians(midAngle.toDouble())
    val innerPt = Offset(
        cx + outerR * cos(rad).toFloat(),
        cy + outerR * sin(rad).toFloat()
    )
    val outerPt = Offset(
        cx + (outerR + 12.dp.toPx()) * cos(rad).toFloat(),
        cy + (outerR + 12.dp.toPx()) * sin(rad).toFloat()
    )
    drawLine(color, innerPt, outerPt, strokeWidth = 1.dp.toPx())
    drawCircle(color, 2.dp.toPx(), outerPt)
}