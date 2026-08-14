package dev.kindling.core.components.ui.maps.blocks.uptimemonitor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun KUptimePopup(
    node: KEdgeNode,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
            .width(180.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = node.city,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            KUptimeStatusBadge(status = node.status)
        }
        Text(
            text = node.region,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(8.dp))

        // Metrics grid
        KUptimePopupMetric(
            label = "Latency",
            value = if (node.status == KEdgeStatus.Down) "—" else "${node.latencyMs} ms",
        )
        Spacer(Modifier.height(4.dp))
        KUptimePopupMetric(
            label = "Uptime",
            value = if (node.status == KEdgeStatus.Down) "—" else "${"%.2f".format(node.uptimePercent)}%",
        )
        Spacer(Modifier.height(4.dp))
        KUptimePopupMetric(
            label = "Req/s",
            value = if (node.status == KEdgeStatus.Down) "—" else "${node.requestsPerSec}",
        )
    }
}

@Composable
private fun KUptimePopupMetric(label: String, value: String) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}