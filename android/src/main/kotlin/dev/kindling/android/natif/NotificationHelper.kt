package dev.kindling.android.natif

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// ─────────────────────────────────────────────
//  NotificationChannel descriptor
// ─────────────────────────────────────────────

/**
 * Describes a notification channel to be registered with the system.
 *
 * Notification channels are required starting from Android O (API 26).
 *
 * ### Example usage:
 * ```kotlin
 * val channel = NotificationChannelConfig(
 *     id = "messages",
 *     name = "Messages",
 *     description = "Notifications for new chat messages",
 *     importance = NotificationManager.IMPORTANCE_HIGH
 * )
 * notificationHelper.registerChannel(channel)
 * ```
 */
data class NotificationChannelConfig(
    val id: String,
    val name: String,
    val description: String = "",
    val importance: Int     = 3
)

// ─────────────────────────────────────────────
//  NotificationConfig
// ─────────────────────────────────────────────

/**
 * Describes a local notification to be posted to the system tray.
 *
 * Provides semantic presets that align with standard UI states:
 * - [NotificationConfig.info] -> Neutral information
 * - [NotificationConfig.success] -> Operation successful
 * - [NotificationConfig.warning] -> High priority warning
 * - [NotificationConfig.error] -> Error or failure notification
 *
 * ### Example usage:
 * ```kotlin
 * val config = NotificationConfig(
 *     id = 101,
 *     channelId = "updates",
 *     title = "Update available",
 *     body = "A new version of Kindling is ready to install.",
 *     smallIcon = R.drawable.ic_notification,
 *     autoCancel = true
 * )
 * notificationHelper.post(config)
 * ```
 */
data class NotificationConfig(
    val id: Int,
    val channelId: String,
    val title: String,
    val body: String,
    val smallIcon: Int,
    val priority: Int           = NotificationCompat.PRIORITY_DEFAULT,
    val autoCancel: Boolean     = true,
    val contentIntent: PendingIntent? = null,
    val ongoing: Boolean        = false
) {
    companion object {
        fun info(
            id: Int,
            channelId: String,
            title: String,
            body: String,
            @DrawableRes icon: Int
        ) = NotificationConfig(id, channelId, title, body, icon,
            priority = NotificationCompat.PRIORITY_DEFAULT)

        fun success(
            id: Int,
            channelId: String,
            title: String,
            body: String,
            @DrawableRes icon: Int
        ) = NotificationConfig(id, channelId, title, body, icon,
            priority = NotificationCompat.PRIORITY_DEFAULT)

        fun warning(
            id: Int,
            channelId: String,
            title: String,
            body: String,
            @DrawableRes icon: Int
        ) = NotificationConfig(id, channelId, title, body, icon,
            priority = NotificationCompat.PRIORITY_HIGH)

        fun error(
            id: Int,
            channelId: String,
            title: String,
            body: String,
            @DrawableRes icon: Int
        ) = NotificationConfig(id, channelId, title, body, icon,
            priority = NotificationCompat.PRIORITY_HIGH)
    }
}

// ─────────────────────────────────────────────
//  NotificationHelper
// ─────────────────────────────────────────────

/**
 * Centralized helper for managing Android local notifications.
 *
 * This utility simplifies channel registration and notification posting while
 * ensuring compliance with Android's notification requirements (API 26+ channels).
 *
 * **Note:** Apps targeting API 33+ must declare and request the
 * `android.permission.POST_NOTIFICATIONS` permission at runtime.
 *
 * ### Initialization:
 * ```kotlin
 * val notificationHelper = NotificationHelper(context)
 * 
 * notificationHelper.registerChannel(
 *     NotificationChannelConfig(
 *         id = "alerts",
 *         name = "System Alerts"
 *     )
 * )
 * ```
 */
class NotificationHelper(context: Context) {

    internal val appContext = context.applicationContext
    internal val manager    = NotificationManagerCompat.from(appContext)

    // ── Channel registration ──────────────────────────────────────────────────

    /**
     * Registers a notification channel (idempotent — safe to call multiple times).
     * This is a no-op on Android versions below API 26 (O).
     */
    fun registerChannel(config: NotificationChannelConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            val channel = NotificationChannel(
                config.id,
                config.name,
                config.importance
            ).apply { description = config.description }
            manager.createNotificationChannel(channel)
        }
    }

    /** Enregistre plusieurs canaux en une seule fois. */
    fun registerChannels(vararg configs: NotificationChannelConfig) =
        configs.forEach { registerChannel(it) }

    // ── Post / Cancel ─────────────────────────────────────────────────────────

    /** Poste la notification décrite par [config]. */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun post(config: NotificationConfig) {
        val notification = NotificationCompat.Builder(appContext, config.channelId)
            .setSmallIcon(config.smallIcon)
            .setContentTitle(config.title)
            .setContentText(config.body)
            .setPriority(config.priority)
            .setAutoCancel(config.autoCancel)
            .setOngoing(config.ongoing)
            .apply { config.contentIntent?.let { setContentIntent(it) } }
            .build()

        manager.notify(config.id, notification)
    }

    /** Annule la notification identifiée par [id]. */
    fun cancel(id: Int) = manager.cancel(id)

    /** Annule toutes les notifications de l'app. */
    fun cancelAll() = manager.cancelAll()

    // ── State ─────────────────────────────────────────────────────────────────

    /** `true` si les notifications sont activées pour l'app. */
    fun areEnabled(): Boolean = manager.areNotificationsEnabled()
}