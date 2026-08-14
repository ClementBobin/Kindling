package dev.kindling.core.components.charts

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
//  Radial Bar Chart
//  Covers: chart-radial-simple, chart-radial-grid, chart-radial-label,
//          chart-radial-shape, chart-radial-stacked, chart-radial-text
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A fully-themed radial bar chart that mirrors the shadcn `<RadialBarChart>` component.
 *
 * Each series value is drawn as a concentric arc at increasing radii, reading
 * from the innermost ring outward — the same layout recharts uses for
 * `<RadialBarChart innerRadius={30} outerRadius={110}>`.
 *
 * @param startAngle      Angle (degrees) where arcs begin.  0 = 3-o'clock;
 *                        -90 = 12-o'clock (default for chart-radial-simple).
 * @param endAngle        Angle (degrees) where a full arc ends.
 *                        360 = full circle; 250 = partial arc
 *                        (chart-radial-text uses 0→250).
 * @param showBackground  When true draws a dimmed full-circle track behind each
 *                        arc (chart-radial-simple uses `<RadialBar background />`).
 * @param showGrid        When true draws concentric circle guides at ring
 *                        boundaries (chart-radial-grid).
 * @param cornerRadius    Cap rounding on arc ends (chart-radial-shape uses
 *                        rounded caps).
 * @param centerLabel     Large number rendered in the centre hole
 *                        (chart-radial-text).  Empty string = no label.
 * @param centerSubLabel  Smaller sub-label below [centerLabel].
 */
@Composable
fun KRadialChart(
    item: ChartRegistryItem,
    startAngle: Float = -90f,
    endAngle: Float = 270f,
    showBackground: Boolean = true,
    showGrid: Boolean = false,
    cornerRadius: Boolean = false,
    centerLabel: String = "",
    centerSubLabel: String = "",
    modifier: Modifier = Modifier,
) {
    val colors      = KindlingChartColors.fromMaterial3()
    val trackColor  = MaterialTheme.colorScheme.surfaceVariant
    val gridColor   = MaterialTheme.colorScheme.outlineVariant

    UniversalChartCard(item = item, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                if (item.data.isEmpty()) return@Canvas

                // Flatten to single series for simple/text radial;
                // multiple series for stacked radial.
                val series  = item.data
                val count   = series.size
                val outerR  = size.minDimension / 2f * 0.92f
                val innerR  = outerR * 0.25f
                val ringGap = if (count > 1) 4.dp.toPx() else 0f
                val ringW   = if (count > 1)
                    (outerR - innerR - ringGap * (count - 1)) / count
                else
                    outerR - innerR

                val cx      = size.width  / 2f
                val cy      = size.height / 2f
                val sweepRange = endAngle - startAngle

                // Grid circles
                if (showGrid) {
                    repeat(count) { i ->
                        val r = innerR + i * (ringW + ringGap) + ringW / 2f
                        drawCircle(
                            color = gridColor,
                            radius = r,
                            center = Offset(cx, cy),
                            style = Stroke(0.5.dp.toPx())
                        )
                    }
                }

                series.forEachIndexed { si, s ->
                    val value    = s.values.firstOrNull() ?: 0f
                    val maxVal   = item.radialMaxValue
                    val fraction = (value / maxVal).coerceIn(0f, 1f)
                    val sweep    = fraction * sweepRange

                    // Ring geometry: innermost = index 0
                    val ringInner = innerR + si * (ringW + ringGap)
                    val ringOuter = ringInner + ringW
                    val ringMid   = (ringInner + ringOuter) / 2f
                    val halfStroke = ringW / 2f

                    val arcRect = Size(ringMid * 2, ringMid * 2)
                    val arcTL   = Offset(cx - ringMid, cy - ringMid)

                    val cap = if (cornerRadius) StrokeCap.Round else StrokeCap.Butt

                    // Background track
                    if (showBackground) {
                        drawArc(
                            color    = trackColor,
                            startAngle = startAngle,
                            sweepAngle = sweepRange,
                            useCenter  = false,
                            topLeft    = arcTL,
                            size       = arcRect,
                            style      = Stroke(width = ringW, cap = StrokeCap.Butt)
                        )
                    }

                    // Filled arc
                    val color = s.colors.firstOrNull() ?: colors.atIndex(si)
                    drawArc(
                        color      = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter  = false,
                        topLeft    = arcTL,
                        size       = arcRect,
                        style      = Stroke(width = ringW, cap = cap)
                    )
                }
            }

            // Centre text (chart-radial-text)
            if (centerLabel.isNotEmpty()) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text  = centerLabel,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    )
                    if (centerSubLabel.isNotEmpty()) {
                        Text(
                            text  = centerSubLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    }
                }
            }
        }
    }
}