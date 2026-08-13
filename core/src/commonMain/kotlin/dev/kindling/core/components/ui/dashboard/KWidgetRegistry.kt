package dev.kindling.core.components.ui.dashboard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.kindling.core.components.ui.dashboard.GeneratedWidgetMap
import dev.kindling.core.components.ui.dashboard.KWidgetModel

/**
 * Dynamic dispatcher component that evaluates incoming widget models and executes
 * the corresponding KSP-generated Composable layout mapping block.
 *
 * @param widget The data container holding widget properties and routing flags.
 */
@Composable
fun RenderWidgetContent(widget: KWidgetModel) {
    val provider = GeneratedWidgetMap[widget.type]
    
    if (provider != null) {
        provider(widget)
    } else {
        Text(text = widget.title)
    }
}