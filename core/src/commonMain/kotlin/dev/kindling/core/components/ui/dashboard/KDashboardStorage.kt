package dev.kindling.core.components.ui.dashboard

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persistence manager handling local JSON disk read and write operations for dashboard layout schemas.
 *
 * @property filesDir The application's files directory path provided by platform-specific expect/actual or parameter.
 */
class KDashboardStorage(private val filesDir: File) {
    private val jsonFile = File(filesDir, "dashboard_layout.json")
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Encodes and persists a list of [KWidgetModel] configurations safely to local device storage.
     */
    fun saveLayout(widgets: List<KWidgetModel>): Result<Unit> {
        return runCatching {
            val serialized = json.encodeToString(widgets)
            // Simple atomic-like write using a temporary file
            val tempFile = File(jsonFile.parentFile, "${jsonFile.name}.tmp")
            tempFile.writeText(serialized, Charsets.UTF_8)
            if (jsonFile.exists()) {
                jsonFile.delete()
            }
            if (!tempFile.renameTo(jsonFile)) {
                throw IllegalStateException("Failed to update dashboard layout file.")
            }
        }
    }

    /**
     * Loads and decodes saved widget models from local storage, or returns `null` if no configuration file exists.
     */
    fun loadLayout(): List<KWidgetModel>? {
        if (!jsonFile.exists()) return null
        return runCatching {
            val text = jsonFile.readText(Charsets.UTF_8)
            json.decodeFromString<List<KWidgetModel>>(text)
        }.getOrNull()
    }
}