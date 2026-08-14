package dev.kindling.core.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.card.KCard
import dev.kindling.core.components.ui.card.KCardContent
import dev.kindling.core.components.ui.card.KCardDescription
import dev.kindling.core.components.ui.card.KCardHeader
import dev.kindling.core.components.ui.card.KCardTitle

// ─────────────────────────────────────────────────────────────────────────────
//  UniversalChartCard
//
//  The shared card shell consumed by every chart variant.
//  Mirrors the shadcn pattern of:
//    <Card>
//      <CardHeader> title + description </CardHeader>
//      <CardContent> {chart} </CardContent>
//      <CardFooter> trend badge + footnote </CardFooter>        ← optional
//    </Card>
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Card wrapper used by all Kindling chart composables.
 *
 * Renders the [KCardHeader] (title + description), the [chartContent] slot,
 * an optional colour-coded legend, and an optional footer with a trend badge
 * and footnote — all driven by the [ChartRegistryItem].
 *
 * @param item          Metadata and series data for the chart.
 * @param showLegend    When true, renders a row of coloured dots + series labels
 *                      below the chart (mirrors `<ChartLegend>`).
 * @param modifier      Applied to the outer [KCard].
 * @param chartContent  The actual drawing composable (Canvas or Box).
 */
@Composable
fun UniversalChartCard(
    item: ChartRegistryItem,
    showLegend: Boolean = false,
    modifier: Modifier = Modifier,
    chartContent: @Composable () -> Unit,
) {
    KCard(modifier = modifier.fillMaxWidth()) {

        // ── Header ────────────────────────────────────────────────────────────
        KCardHeader {
            KCardTitle(item.title)
            KCardDescription(item.description)
        }

        // ── Chart area ────────────────────────────────────────────────────────
        KCardContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                chartContent()

                // ── Legend (opt-in) ───────────────────────────────────────────
                if (showLegend && item.data.size > 1) {
                    ChartLegendRow(item = item)
                }

                // ── Footer (trend badge + footnote) ───────────────────────────
                if (item.footerBadge.isNotEmpty() || item.footerTrend.isNotEmpty()) {
                    ChartFooter(item = item)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ChartLegendRow
//  Mirrors <ChartLegend content={<ChartLegendContent />} />
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChartLegendRow(item: ChartRegistryItem) {
    val colors = KindlingChartColors.fromMaterial3()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item.data.forEachIndexed { i, series ->
            val dotColor = series.colors.firstOrNull() ?: colors.atIndex(i)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text  = series.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ChartFooter
//  Mirrors <CardFooter> with trend badge and date range footnote
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChartFooter(item: ChartRegistryItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (item.footerBadge.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Trending arrow indicator (▲ or ▼ derived from badge text)
                val isUp = item.footerBadge.contains("↑") ||
                           item.footerBadge.contains("up", ignoreCase = true) ||
                           item.footerBadge.contains("+")
                Text(
                    text  = if (isUp) "▲" else "▼",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isUp)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text  = item.footerBadge,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                )
            }
        }

        if (item.footerTrend.isNotEmpty()) {
            Text(
                text  = item.footerTrend,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}