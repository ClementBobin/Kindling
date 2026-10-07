package dev.kindling.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.core.theme.KindlingTheme

@Composable
fun App(component: String?, dark: Boolean) {
    KindlingTheme(colorScheme = if (dark) ShadcnDark else ShadcnLight) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val demo = demos.firstOrNull { it.id == component }
            if (demo != null) {
                // Embedded in a docs page: just the component, centred.
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) { demo.content() }
                }
            } else {
                Gallery()
            }
        }
    }
}

/** Full gallery: component list on the left, the selected demo on the right. */
@Composable
private fun Gallery() {
    var selected by remember { mutableStateOf(demos.first().id) }
    Row(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            demos.forEach { d ->
                KButton(
                    text = d.title,
                    onClick = { selected = d.id },
                    variant = if (d.id == selected) KButtonVariant.Secondary else KButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Box(Modifier.weight(1f).fillMaxHeight().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = demos.first { it.id == selected }.title,
                    style = MaterialTheme.typography.titleLarge,
                )
                demos.first { it.id == selected }.content()
            }
        }
    }
}
