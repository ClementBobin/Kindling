package dev.kindling.core.components.ui.dashboard

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistence manager handling local JSON read and write operations for dashboard layout schemas.
 */
class KDashboardStorage(private val saveAction: (String) -> Unit, private val loadAction: () -> String?) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Encodes and persists a list of [KWidgetModel] configurations safely.
     */
    fun saveLayout(widgets: List<KWidgetModel>): Result<Unit> {
        return runCatching {
            val serialized = json.encodeToString(widgets)
            saveAction(serialized)
        }
    }

    /**
     * Loads and decodes saved widget models from storage, or returns `null` if no configuration exists.
     */
    fun loadLayout(): List<KWidgetModel>? {
        val text = loadAction() ?: return null
        return runCatching {
            json.decodeFromString<List<KWidgetModel>>(text)
        }.getOrNull()
    }
}
