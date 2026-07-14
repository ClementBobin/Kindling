package dev.kindling.android.helper.natif

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
 * Décrit un canal de notification à enregistrer.
 *
 * ```kotlin
 * val channel = NotificationChannelConfig(
 *     id          = "orders",
 *     name        = "Commandes",
 *     description = "Statut de vos commandes Cyna",
 *     importance  = NotificationManager.IMPORTANCE_DEFAULT
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
 * Décrit une notification locale à poster.
 *
 * Presets sémantiques alignés avec les états UI :
 * - [NotificationConfig.info]    → information neutre
 * - [NotificationConfig.success] → opération réussie
 * - [NotificationConfig.warning] → avertissement
 * - [NotificationConfig.error]   → erreur
 *
 * Config personnalisée :
 * ```kotlin
 * val config = NotificationConfig(
 *     id        = 42,
 *     channelId = "orders",
 *     title     = "Commande expédiée",
 *     body      = "Votre commande #1234 est en route.",
 *     smallIcon = R.drawable.ic_notification,
 *     priority  = NotificationCompat.PRIORITY_HIGH,
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
 * Helper de notifications locales centralisé.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { NotificationHelper(androidContext()) }
 * ```
 *
 * Initialisation (Application.onCreate ou module Koin) :
 * ```kotlin
 * notificationHelper.registerChannel(
 *     NotificationChannelConfig(
 *         id          = "orders",
 *         name        = "Commandes",
 *         description = "Statut de vos commandes"
 *     )
 * )
 * ```
 *
 * Post :
 * ```kotlin
 * notificationHelper.post(
 *     NotificationConfig.success(
 *         id        = 1,
 *         channelId = "orders",
 *         title     = "Paiement accepté",
 *         body      = "Votre abonnement Cyna est actif.",
 *         icon      = R.drawable.ic_notification
 *     )
 * )
 * ```
 *
 * L'app doit déclarer `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>`
 * (requise à l'exécution sur API 33+).
 */
class NotificationHelper(context: Context) {

    internal val appContext = context.applicationContext
    internal val manager    = NotificationManagerCompat.from(appContext)

    // ── Channel registration ──────────────────────────────────────────────────

    /**
     * Enregistre un canal de notification (idempotent — safe à appeler plusieurs fois).
     * No-op sur API < 26.
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