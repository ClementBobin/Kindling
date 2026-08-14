package dev.kindling.core.components.ui.maps.blocks.logisticsnetwork

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun KLogisticsFilterSidebar(
    nodes: List<KLogisticsNode>,
    routes: List<KLogisticsRoute>,
    activeNodeTypes: Set<KLogisticsNodeType>,
    activeRouteStatuses: Set<KLogisticsRouteStatus>,
    onNodeTypeToggle: (KLogisticsNodeType) -> Unit,
    onRouteStatusToggle: (KLogisticsRouteStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Summary stats ─────────────────────────────────────────────────
        Text(
            text  = "Network overview",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))

        val visibleNodes  = nodes.filter { it.type in activeNodeTypes }
        val visibleRoutes = routes.filter { it.status in activeRouteStatuses }
        val totalShipments = visibleNodes.sumOf { it.shipments }
        val avgCapacity    = if (visibleNodes.isEmpty()) 0
                             else visibleNodes.sumOf { it.capacity } / visibleNodes.size
        val activeRouteCount = visibleRoutes.count { it.status == KLogisticsRouteStatus.Active }

        KLogisticsStat("Nodes visible",    "${visibleNodes.size}")
        KLogisticsStat("Total shipments",  "%,d".format(totalShipments))
        KLogisticsStat("Avg capacity",     "$avgCapacity%")
        KLogisticsStat("Active routes",    "$activeRouteCount / ${visibleRoutes.size}")

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))

        // ── Node type filters ─────────────────────────────────────────────
        Text(
            text  = "Node type",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        KLogisticsNodeType.entries.forEach { type ->
            KLogisticsFilterRow(
                label     = type.label(),
                dotColor  = type.color(),
                checked   = type in activeNodeTypes,
                onToggle  = { onNodeTypeToggle(type) },
                count     = nodes.count { it.type == type },
            )
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))

        // ── Route status filters ──────────────────────────────────────────
        Text(
            text  = "Route status",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        KLogisticsRouteStatus.entries.forEach { status ->
            KLogisticsFilterRow(
                label    = status.label(),
                dotColor = status.color(),
                checked  = status in activeRouteStatuses,
                onToggle = { onRouteStatusToggle(status) },
                count    = routes.count { it.status == status },
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun KLogisticsStat(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun KLogisticsFilterRow(
    label: String,
    dotColor: androidx.compose.ui.graphics.Color,
    checked: Boolean,
    onToggle: () -> Unit,
    count: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked  = checked,
            onCheckedChange = { onToggle() },
            colors   = CheckboxDefaults.colors(
                checkedColor   = dotColor,
                checkmarkColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}