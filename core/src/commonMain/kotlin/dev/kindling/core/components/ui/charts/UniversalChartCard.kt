package dev.kindling.core.components.ui.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.card.KCard
import dev.kindling.core.components.ui.card.KCardContent
import dev.kindling.core.components.ui.card.KCardDescription
import dev.kindling.core.components.ui.card.KCardHeader
import dev.kindling.core.components.ui.card.KCardTitle
import dev.kindling.core.theme.kindlingShapes

@Composable
fun UniversalChartCard(
    item: ChartRegistryItem,
    modifier: Modifier = Modifier,
    chartContent: @Composable () -> Unit
) {
    val shapes = MaterialTheme.kindlingShapes

    KCard(modifier = modifier.fillMaxWidth()) {
        KCardHeader {
            KCardTitle(item.title)
            KCardDescription(item.description)
        }
        KCardContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                chartContent()
            }
        }
    }
}