package dev.kindling.core.components.ui.maps.blocks.analyticsmapcard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val trendUp   = Color(0xFF22C55E) // green-500
private val trendDown = Color(0xFFEF4444) // red-500

@Composable
fun KAnalyticsMetricChip(
    metric: KAnalyticsMetric,
    modifier: Modifier = Modifier,
) {
    val trendColor = when (metric.trend) {
        KMetricTrend.Up      -> trendUp
        KMetricTrend.Down    -> trendDown
        KMetricTrend.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val trendArrow = when (metric.trend) {
        KMetricTrend.Up      -> "↑"
        KMetricTrend.Down    -> "↓"
        KMetricTrend.Neutral -> "→"
    }

    Column(modifier = modifier) {
        // Headline value
        Text(
            text  = metric.value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        // Label + trend
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text  = metric.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (metric.changeText != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = "$trendArrow ${metric.changeText}",
                    style = MaterialTheme.typography.labelSmall,
                    color = trendColor,
                )
            }
        }
    }
}