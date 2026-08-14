package dev.kindling.core.components.ui.maps.blocks.uptimemonitor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun KUptimeSummaryBar(
    nodes: List<KEdgeNode>,
    modifier: Modifier = Modifier,
) {
    val healthy  = nodes.count { it.status == KEdgeStatus.Healthy }
    val degraded = nodes.count { it.status == KEdgeStatus.Degraded }
    val down     = nodes.count { it.status == KEdgeStatus.Down }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        KUptimeSummaryStat(
            count = healthy,
            label = "Healthy",
            status = KEdgeStatus.Healthy,
        )
        KUptimeSummaryStat(
            count = degraded,
            label = "Degraded",
            status = KEdgeStatus.Degraded,
        )
        KUptimeSummaryStat(
            count = down,
            label = "Down",
            status = KEdgeStatus.Down,
        )
        KUptimeSummaryStat(
            count = nodes.size,
            label = "Total nodes",
            status = null,
        )
    }
}

@Composable
private fun KUptimeSummaryStat(
    count: Int,
    label: String,
    status: KEdgeStatus?,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = status?.color() ?: MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}