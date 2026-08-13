package dev.kindling.core.components.ui.dashboard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

object KWidgetRegistry {
    private val providers = mutableMapOf<String, @Composable (KWidgetModel) -> Unit>()
    private val metadataList = mutableListOf<KWidgetMetadata>()
    private var isInitialized = false

    fun register(
        metadata: KWidgetMetadata,
        provider: @Composable (KWidgetModel) -> Unit
    ) {
        providers[metadata.type] = provider
        if (metadataList.none { it.type == metadata.type }) {
            metadataList.add(metadata)
        }
    }

    fun get(type: String): (@Composable (KWidgetModel) -> Unit)? {
        ensureInitialized()
        return providers[type]
    }

    fun getAllMetadata(): List<KWidgetMetadata> {
        ensureInitialized()
        return metadataList
    }

    @Synchronized
    private fun ensureInitialized() {
        if (isInitialized) return
        isInitialized = true

        // Finds and executes all generated KSP initializers in all modules automatically
        try {
            val initializerClass = Class.forName("dev.kindling.generated.KWidgetModuleInitializer")
            initializerClass.getDeclaredField("INSTANCE").get(null)
        } catch (_: Exception) {
            // Module initializer not present in classpath or no widgets in module
        }
    }
}

@Composable
fun RenderWidgetContent(widget: KWidgetModel) {
    val provider = KWidgetRegistry.get(widget.type)
    
    if (provider != null) {
        provider(widget)
    } else {
        Text(text = widget.title)
    }
}