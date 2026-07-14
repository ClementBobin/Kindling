package dev.kindling.android.helper.natif

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri

// ─────────────────────────────────────────────
//  WakeLockConfig
// ─────────────────────────────────────────────

/**
 * Décrit un WakeLock à acquérir.
 *
 * Presets :
 * - [WakeLockConfig.PartialCpu]  → CPU actif, écran peut s'éteindre (background work)
 * - [WakeLockConfig.ScreenDim]   → écran allumé en mode dim (lecture)
 * - [WakeLockConfig.ScreenBright]→ écran pleine luminosité
 */
data class WakeLockConfig(
    val tag: String,
    val levelAndFlags: Int    = PowerManager.PARTIAL_WAKE_LOCK,
    val timeoutMs: Long       = 10 * 60 * 1000L    // 0 = pas de timeout automatique
) {
    companion object {
        fun partialCpu(tag: String, timeoutMs: Long = 0L) = WakeLockConfig(
            tag           = tag,
            levelAndFlags = PowerManager.PARTIAL_WAKE_LOCK,
            timeoutMs     = timeoutMs
        )
        fun screenDim(tag: String) = WakeLockConfig(
            tag           = tag,
            // SCREEN_DIM_WAKE_LOCK déprécié API 17 — utiliser WindowManager.FLAG_KEEP_SCREEN_ON
            // depuis une Activity à la place. Conservé pour compatibilité service/background.
            levelAndFlags = PowerManager.SCREEN_DIM_WAKE_LOCK
        )
        fun screenBright(tag: String) = WakeLockConfig(
            tag           = tag,
            levelAndFlags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK
        )
    }
}

// ─────────────────────────────────────────────
//  PowerHelper
// ─────────────────────────────────────────────

/**
 * Helper de gestion de l'énergie centralisé.
 *
 * Permission requise : `android.permission.WAKE_LOCK`
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { PowerHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // WakeLock pendant un traitement
 * val lock = powerHelper.acquire(WakeLockConfig.partialCpu("MyApp:Processing", timeoutMs = 60_000))
 * try {
 *     doHeavyWork()
 * } finally {
 *     powerHelper.release(lock)
 * }
 *
 * // Vérifier le mode économie d'énergie
 * if (powerHelper.isPowerSaveMode()) reduceSyncFrequency()
 *
 * // Demander l'exemption d'optimisation batterie
 * if (!powerHelper.isIgnoringBatteryOptimizations()) {
 *     powerHelper.requestIgnoreBatteryOptimizations(context)
 * }
 * ```
 */
class PowerHelper(context: Context) {

    internal val appContext   = context.applicationContext
    internal val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    // ── WakeLock ──────────────────────────────────────────────────────────────

    /**
     * Acquiert un [PowerManager.WakeLock] selon [config].
     * Penser à appeler [release] pour éviter les fuites.
     *
     * Quand [WakeLockConfig.timeoutMs] vaut `0`, le lock est acquis sans timeout
     * (contrat de [WakeLockConfig.partialCpu] avec `timeoutMs = 0`).
     * Pour tout autre valeur positive, le timeout est transmis au système.
     */
    fun acquire(config: WakeLockConfig): PowerManager.WakeLock {
        val lock = powerManager.newWakeLock(config.levelAndFlags, config.tag)
        if (config.timeoutMs > 0L) {
            lock.acquire(config.timeoutMs)
        } else {
            @Suppress("DEPRECATION")
            lock.acquire(10*60*1000L /*10 minutes*/)
        }
        return lock
    }

    /** Relâche le [lock] s'il est encore tenu. */
    fun release(lock: PowerManager.WakeLock) {
        if (lock.isHeld) lock.release()
    }

    /**
     * Exécute [block] sous WakeLock et relâche automatiquement.
     */
    inline fun withWakeLock(config: WakeLockConfig, block: () -> Unit) {
        val lock = acquire(config)
        try { block() } finally { release(lock) }
    }

    // ── Power save ────────────────────────────────────────────────────────────

    fun isPowerSaveMode(): Boolean = powerManager.isPowerSaveMode

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isDeviceIdleMode(): Boolean = powerManager.isDeviceIdleMode

    fun isInteractive(): Boolean = powerManager.isInteractive

    // ── Battery optimization ──────────────────────────────────────────────────

    fun isIgnoringBatteryOptimizations(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(appContext.packageName)
        } else {
            true // API 21-22 : battery optimizations inexistantes
        }

    /**
     * Ouvre le dialogue système pour demander l'exemption d'optimisation batterie.
     * Requiert `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` dans le manifest.
     *
     * Retourne `false` si l'intent ne peut pas être résolu (ROM custom, profil géré).
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun requestIgnoreBatteryOptimizations(context: Context): Boolean {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:${context.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            true
        } else {
            Log.w(TAG, "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS non résolu sur cet appareil")
            false
        }
    }

    /**
     * Ouvre les réglages d'optimisation batterie.
     *
     * Retourne `false` si l'intent ne peut pas être résolu (ROM custom, profil géré).
     */
    fun openBatterySettings(context: Context): Boolean {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        } else {
            // Fallback : réglages généraux de la batterie
            Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            true
        } else {
            Log.w(TAG, "Intent réglages batterie non résolu sur cet appareil")
            false
        }
    }

    companion object {
        private const val TAG = "PowerHelper"
    }
}