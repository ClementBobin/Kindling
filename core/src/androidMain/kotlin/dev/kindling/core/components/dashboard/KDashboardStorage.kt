package dev.kindling.core.components.dashboard

import android.content.Context
import android.util.AtomicFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persistence manager handling local JSON disk read and write operations for dashboard layout schemas.
 *
 * @property context Application context reference utilized for accessing sandbox file paths.
 */
class KDashboardStorage(private val context: Context) {
    private val atomicFile = AtomicFile(File(context.filesDir, "dashboard_layout.json"))
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Encodes and persists a list of [KWidgetModel] configurations safely to local device storage using atomic writes.
     *
     * @param widgets List of widget configuration nodes to serialize and store.
     * @return A [Result] indicating success or containing any serialization/write exceptions.
     */
    fun saveLayout(widgets: List<KWidgetModel>): Result<Unit> {
        return runCatching {
            val serialized = json.encodeToString(widgets)
            val stream = atomicFile.startWrite()
            try {
                stream.write(serialized.toByteArray(Charsets.UTF_8))
                atomicFile.finishWrite(stream)
            } catch (e: Exception) {
                atomicFile.failWrite(stream)
                throw e
            }
        }
    }

    /**
     * Loads and decodes saved widget models from local storage, or returns `null` if no configuration file exists.
     *
     * @return List of decoded [KWidgetModel] instances, or null on failure/absence.
     */
    fun loadLayout(): List<KWidgetModel>? {
        if (!atomicFile.baseFile.exists()) return null
        return runCatching {
            val text = atomicFile.openRead().bufferedReader().use { it.readText() }
            json.decodeFromString<List<KWidgetModel>>(text)
        }.getOrNull()
    }
}