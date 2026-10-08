package dev.kindling.showcase

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kindling.core.theme.KindlingTheme

/** Standalone single-component view (`?component=<id>`), used when a demo is embedded elsewhere. */
@Composable
fun App(component: String?, dark: Boolean) {
    KindlingTheme(colorScheme = if (dark) ShadcnDark else ShadcnLight) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) { demos.firstOrNull { it.id == component }?.content?.invoke() }
            }
        }
    }
}
