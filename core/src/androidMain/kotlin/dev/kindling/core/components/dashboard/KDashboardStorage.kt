package dev.kindling.core.components.dashboard

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persistence manager handling local JSON disk read and write operations for dashboard layout schemas.
 *
 * @property context Application context reference utilized for accessing sandbox file paths.
 */
class KDashboardStorage(private val context: Context) {
    private val file = File(context.filesDir, "dashboard_layout.json")
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Encodes and persists a list of [KWidgetModel] configurations safely to local device storage.
     *
     * @param widgets List of widget configuration nodes to serialize and store.
     */
    fun saveLayout(widgets: List<KWidgetModel>) {
        runCatching {
            val serialized = json.encodeToString(widgets)
            file.writeText(serialized)
        }
    }

    /**
     * Loads and decodes saved widget models from local storage, or returns `null` if no configuration file exists.
     *
     * @return List of decoded [KWidgetModel] instances, or null on failure/absence.
     */
    fun loadLayout(): List<KWidgetModel>? {
        return runCatching {
            if (!file.exists()) return null
            val text = file.readText()
            json.decodeFromString<List<KWidgetModel>>(text)
        }.getOrNull()
    }
}