package dev.kindling.android.natif

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import androidx.core.net.toUri

// ─────────────────────────────────────────────
//  DownloadConfig
// ─────────────────────────────────────────────

/**
 * Describes a download task to be performed via [DownloadManager].
 *
 * Use the provided presets:
 * - [DownloadConfig.publicFile] -> Public Downloads folder, notification visible upon completion.
 *
 * ### Example usage:
 * ```kotlin
 * val config = DownloadConfig(
 *     url = "https://example.com/file.pdf",
 *     fileName = "document.pdf",
 *     title = "Financial Report",
 *     requiresWifi = true
 * )
 * val downloadId = downloadHelper.enqueue(config)
 * ```
 */
data class DownloadConfig(
    val url: String,
    val fileName: String,
    val title: String               = fileName,
    val description: String         = "",
    val mimeType: String            = "*/*",
    val destinationDir: String      = Environment.DIRECTORY_DOWNLOADS,
    val requiresWifi: Boolean       = false,
    val allowMetered: Boolean       = true,
    val visibleInNotification: Boolean = true
) {
    companion object {
        fun publicFile(
            url: String,
            fileName: String,
            mimeType: String = "*/*",
            title: String = fileName
        ) = DownloadConfig(
            url       = url,
            fileName  = fileName,
            mimeType  = mimeType,
            title     = title,
            visibleInNotification = true
        )
    }
}

// ─────────────────────────────────────────────
//  DownloadStatus
// ─────────────────────────────────────────────

sealed class DownloadStatus {
    data class  Running(val downloadedBytes: Long, val totalBytes: Long) : DownloadStatus()
    data class  Success(val localUri: Uri)                               : DownloadStatus()
    data class  Failed(val reason: Int)                                  : DownloadStatus()
    data object Paused                                                   : DownloadStatus()
    data object Pending                                                  : DownloadStatus()
}

// ─────────────────────────────────────────────
//  DownloadHelper
// ─────────────────────────────────────────────

/**
 * Centralized download helper based on Android's [DownloadManager].
 *
 * Handles file downloads to public or private directories with automatic
 * progress tracking and system notifications.
 *
 * **Permissions:**
 * - No permissions required for public folder downloads on API 29+.
 * - `WRITE_EXTERNAL_STORAGE` required on API < 29.
 *
 * ### Example usage:
 * ```kotlin
 * val downloadHelper = DownloadHelper(context)
 * 
 * val id = downloadHelper.enqueue(
 *     DownloadConfig.publicFile(
 *         url = "https://example.com/report.pdf",
 *         fileName = "rapport.pdf"
 *     )
 * )
 *
 * // Observe completion
 * viewModelScope.launch {
 *     downloadHelper.completionFlow(id).collect { status -> 
 *         if (status is DownloadStatus.Success) {
 *             openFile(status.localUri)
 *         }
 *     }
 * }
 * ```
 */
class DownloadHelper(context: Context) {

    internal val appContext       = context.applicationContext
    internal val downloadManager: DownloadManager =
        appContext.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: throw IllegalStateException("DownloadManager unavailable on this device")

    // ── Enqueue ───────────────────────────────────────────────────────────────

    /** Starts the download described by [config]. Returns the download ID. */
    fun enqueue(config: DownloadConfig): Long {
        val request = DownloadManager.Request(config.url.toUri()).apply {
            setTitle(config.title)
            setDescription(config.description)
            setMimeType(config.mimeType)
            setDestinationInExternalPublicDir(config.destinationDir, config.fileName)
            setNotificationVisibility(
                if (config.visibleInNotification)
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                else
                    DownloadManager.Request.VISIBILITY_HIDDEN
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setRequiresCharging(false)
            }
            // Restreindre au Wi-Fi si demandé ; setAllowedOverMetered contrôle
            // l'accès réseau mesuré indépendamment du type de réseau autorisé.
            if (config.requiresWifi) {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            }
            setAllowedOverMetered(config.allowMetered)
        }
        return downloadManager.enqueue(request)
    }

    // ── Status ────────────────────────────────────────────────────────────────

    /** Retourne le statut courant du téléchargement [id]. */
    fun getStatus(id: Long): DownloadStatus? {
        val query  = DownloadManager.Query().setFilterById(id)
        val cursor = downloadManager.query(query)
        return cursor.use { c ->
            if (!c.moveToFirst()) return null
            val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_RUNNING -> {
                    val downloaded = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total      = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    DownloadStatus.Running(downloaded, total)
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    val uriStr = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    DownloadStatus.Success(uriStr.toUri())
                }
                DownloadManager.STATUS_FAILED  -> {
                    val reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                    DownloadStatus.Failed(reason)
                }
                DownloadManager.STATUS_PAUSED  -> DownloadStatus.Paused
                else                           -> DownloadStatus.Pending
            }
        }
    }

    // ── Completion flow ───────────────────────────────────────────────────────

    /**
     * Flow émettant le [DownloadStatus] final (Success ou Failed) pour l'[id] donné.
     * Se ferme automatiquement après émission.
     *
     * Si le téléchargement est déjà terminé au moment de la souscription, le
     * statut est émis immédiatement sans enregistrer de BroadcastReceiver.
     */
    fun completionFlow(id: Long): Flow<DownloadStatus> = callbackFlow {
        // Vérification eagée : le téléchargement peut avoir fini avant la souscription.
        val existing = getStatus(id)
        if (existing is DownloadStatus.Success || existing is DownloadStatus.Failed) {
            trySend(existing)
            close()
            return@callbackFlow
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (completedId == id) {
                    getStatus(id)?.let { trySend(it) }
                    close()
                }
            }
        }

        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED  // broadcast système → exported
        )

        awaitClose { appContext.unregisterReceiver(receiver) }
    }

    // ── Control ───────────────────────────────────────────────────────────────

    fun cancel(vararg ids: Long) = downloadManager.remove(*ids)

    fun getDownloadUri(id: Long): Uri? = downloadManager.getUriForDownloadedFile(id)
}