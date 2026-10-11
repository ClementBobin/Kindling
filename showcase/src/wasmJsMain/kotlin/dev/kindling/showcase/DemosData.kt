package dev.kindling.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.charts.ChartRegistryItem
import dev.kindling.core.components.ui.charts.ChartSeries
import dev.kindling.core.components.ui.charts.KAreaChart
import dev.kindling.core.components.ui.charts.KBarChart
import dev.kindling.core.components.ui.charts.KPieChart
import dev.kindling.core.components.ui.charts.KRadarChart
import dev.kindling.core.components.ui.charts.KRadialChart
import dev.kindling.core.components.ui.combobox.Combobox
import dev.kindling.core.components.ui.combobox.ComboboxCollection
import dev.kindling.core.components.ui.combobox.ComboboxContent
import dev.kindling.core.components.ui.combobox.ComboboxEmpty
import dev.kindling.core.components.ui.combobox.ComboboxTrigger
import dev.kindling.core.components.ui.combobox.ComboboxList
import dev.kindling.core.components.ui.combobox.KComboboxItem
import dev.kindling.core.components.ui.combobox.rememberComboboxState
import dev.kindling.core.components.ui.datatable.KDataTable
import dev.kindling.core.components.ui.datatable.KTableColumn

private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")

@Composable
fun AreaChartDemo() {
    val item = remember {
        ChartRegistryItem(
            title = "Visitors",
            description = "Last 6 months",
            data = listOf(ChartSeries("Desktop", listOf(186f, 305f, 237f, 73f, 209f, 214f))),
            xLabels = months,
        )
    }
    KAreaChart(item = item, modifier = Modifier.widthIn(max = 380.dp))
}

@Composable
fun BarChartDemo() {
    val item = remember {
        ChartRegistryItem(
            title = "Revenue",
            description = "Last 6 months",
            data = listOf(ChartSeries("Revenue", listOf(186f, 305f, 237f, 73f, 209f, 214f))),
            xLabels = months,
        )
    }
    KBarChart(item = item, modifier = Modifier.widthIn(max = 380.dp))
}

@Composable
fun PieChartDemo() {
    val item = remember {
        ChartRegistryItem(
            title = "Browsers",
            description = "Share of visitors",
            data = listOf(ChartSeries("Visitors", listOf(275f, 200f, 187f, 173f, 90f))),
            xLabels = listOf("Chrome", "Safari", "Firefox", "Edge", "Other"),
        )
    }
    KPieChart(item = item, donut = true, centerLabel = "925", modifier = Modifier.widthIn(max = 380.dp))
}

@Composable
fun RadarChartDemo() {
    val item = remember {
        ChartRegistryItem(
            title = "Skills",
            description = "Team assessment",
            data = listOf(ChartSeries("Score", listOf(86f, 70f, 90f, 64f, 78f, 55f))),
            xLabels = listOf("Design", "Code", "Test", "Ops", "Docs", "Sales"),
        )
    }
    KRadarChart(item = item, showDots = true, modifier = Modifier.widthIn(max = 380.dp))
}

@Composable
fun RadialChartDemo() {
    val item = remember {
        ChartRegistryItem(
            title = "Storage",
            description = "Used capacity",
            data = listOf(ChartSeries("Used", listOf(72f))),
            radialMaxValue = 100f,
        )
    }
    KRadialChart(
        item = item,
        centerLabel = "72%",
        centerSubLabel = "used",
        cornerRadius = true,
        modifier = Modifier.widthIn(max = 380.dp),
    )
}

@Composable
fun ChartsDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BarChartDemo()
        PieChartDemo()
    }
}

private data class Payment(val email: String, val status: String, val amount: Int)

@Composable
fun DataTableDemo() {
    val payments = remember {
        listOf(
            Payment("ken99@example.com", "Success", 316),
            Payment("abe45@example.com", "Success", 242),
            Payment("monserrat44@example.com", "Processing", 837),
            Payment("silas22@example.com", "Failed", 874),
            Payment("carmella@example.com", "Success", 721),
        )
    }
    val columns = remember {
        listOf(
            KTableColumn<Payment>("email", "Email", weight = 2f) { Text(it.email) },
            KTableColumn<Payment>("status", "Status") { Text(it.status) },
            KTableColumn<Payment>("amount", "Amount", align = TextAlign.End) { Text("$" + it.amount) },
        )
    }
    KDataTable(columns = columns, data = payments, striped = true, modifier = Modifier.widthIn(max = 380.dp))
}

@Composable
fun ComboboxDemo() {
    val items = remember {
        listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro", "Compose").map { KComboboxItem(it.lowercase(), it) }
    }
    var current by remember { mutableStateOf<KComboboxItem?>(null) }
    val state = rememberComboboxState(selected = current, onSelect = { current = it })
    Combobox(modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth()) {
        ComboboxTrigger(state = state)
        ComboboxContent(state = state) {
            ComboboxList {
                ComboboxCollection(state = state, items = items)
                if (items.none { it.label.contains(state.query, ignoreCase = true) }) {
                    ComboboxEmpty { Text("No framework found.") }
                }
            }
        }
    }
}
