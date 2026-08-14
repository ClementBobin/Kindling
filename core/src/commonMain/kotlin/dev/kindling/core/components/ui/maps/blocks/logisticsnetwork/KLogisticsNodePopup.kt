package dev.kindling.core.components.ui.maps.blocks.logisticsnetwork

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun KLogisticsNodePopup(
    node: KLogisticsNode,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
            .width(200.dp),
    ) {
        // Type badge
        Text(
            text  = node.type.label().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = node.type.color(),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = node.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text  = "${node.city}, ${node.state}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(8.dp))

        KLogisticsPopupRow("Shipments",  "%,d".format(node.shipments))
        Spacer(Modifier.height(4.dp))
        KLogisticsPopupRow("Capacity",   "${node.capacity}%")
        Spacer(Modifier.height(4.dp))
        KLogisticsPopupRow("On-time",    "${"%.1f".format(node.onTimeRate)}%")
    }
}

@Composable
private fun KLogisticsPopupRow(label: String, value: String) {
    Row {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = androidx.compose.ui.Modifier.weight(1f),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}