package dev.kindling.showcase.docs

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.KInput
import dev.kindling.showcase.demos
import dev.kindling.showcase.docs.playground.Chip

/**
 * `#/dev`: every registered demo on one page, at a width you pick. A fast visual smoke test after changing a
 * component or a demo: scan for clipped, empty or overlapping previews without opening each docs page.
 * Not linked from the navigation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DevPage(onOpenPage: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    var width by remember { mutableStateOf(360) }
    var query by remember { mutableStateOf("") }
    val shown = demos.filter { query.isBlank() || it.title.contains(query, ignoreCase = true) || it.id.contains(query, ignoreCase = true) }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Demo smoke test", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
        Text(
            "All ${demos.size} live demos at once. Pick a width to check how they behave when narrow; click a title to open its docs page.",
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Width", fontSize = 13.sp, color = colors.onSurfaceVariant)
            listOf(280, 360, 480, 720).forEach { Chip("${it}dp", selected = width == it) { width = it } }
            KInput(value = query, onValueChange = { query = it }, placeholder = "Filter...", modifier = Modifier.width(200.dp))
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            shown.forEach { d ->
                Column(
                    Modifier.width(width.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, colors.outline, RoundedCornerShape(10.dp)),
                ) {
                    Text(
                        d.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPage("components/${d.id}") }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                    Box(Modifier.fillMaxWidth().padding(14.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { d.content() }
                    }
                }
            }
        }
    }
}
