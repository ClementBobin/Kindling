package dev.kindling.android.natif

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
 * - [WakeLockConfig.partialCpu]  → CPU actif, écran peut s'éteindre (background work)
 * - [WakeLockConfig.screenDim]    → Écran allumé en mode dim (déprécié API 17, préférer FLAG_KEEP_SCREEN_ON dans l'UI)
 * - [WakeLockConfig.screenBright] → Écran pleine luminosité (déprécié API 17)
 *
 * Remarque : Pour maintenir l'écran allumé (dim/bright), utiliser FLAG_KEEP_SCREEN_ON 
 * au niveau de l'Activity / View au lieu de PowerManager. WakeLock.
 */
data class WakeLockConfig(
    val tag: String,
    val levelAndFlags: Int = PowerManager.PARTIAL_WAKE_LOCK,
    val timeoutMs: Long = 0L // 0L = pas de timeout automatique
) {
    companion object {
        fun partialCpu(tag: String, timeoutMs: Long = 0L) = WakeLockConfig(
            tag = tag,
            levelAndFlags = PowerManager.PARTIAL_WAKE_LOCK,
            timeoutMs = timeoutMs
        )

        @Suppress("DEPRECATION")
        fun screenDim(tag: String) = WakeLockConfig(
            tag = tag,
            levelAndFlags = PowerManager.SCREEN_DIM_WAKE_LOCK
        )

        @Suppress("DEPRECATION")
        fun screenBright(tag: String) = WakeLockConfig(
            tag = tag,
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
 */
class PowerHelper(context: Context) {

    internal val appContext = context.applicationContext
    internal val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    // ── WakeLock ──────────────────────────────────────────────────────────────

    /**
     * Acquiert un [PowerManager.WakeLock] selon [config].
     * Penser à appeler [release] pour éviter les fuites.
     */
    fun acquire(config: WakeLockConfig): PowerManager.WakeLock {
        val lock = powerManager.newWakeLock(config.levelAndFlags, config.tag)
        if (config.timeoutMs > 0L) {
            lock.acquire(config.timeoutMs)
        } else {
            lock.acquire()
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
     */
    fun openBatterySettings(context: Context): Boolean {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        } else {
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
