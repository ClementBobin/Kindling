package dev.kindling.android.helper.natif

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.Executor

// ─────────────────────────────────────────────
//  ThermalStatus
// ─────────────────────────────────────────────

/**
 * État thermique de l'appareil (API 29+).
 *
 * - [ThermalStatus.None]       → pas de throttling
 * - [ThermalStatus.Light]      → throttling léger
 * - [ThermalStatus.Moderate]   → throttling modéré
 * - [ThermalStatus.Severe]     → throttling sévère
 * - [ThermalStatus.Critical]   → état critique
 * - [ThermalStatus.Emergency]  → urgence thermique
 * - [ThermalStatus.Shutdown]   → arrêt imminent
 * - [ThermalStatus.Unknown]    → état inconnu / API < 29
 */
enum class ThermalStatus {
    None, Light, Moderate, Severe, Critical, Emergency, Shutdown, Unknown
}

// ─────────────────────────────────────────────
//  ThermalHelper
// ─────────────────────────────────────────────

/**
 * Helper d'état thermique centralisé (API 29+).
 *
 * Sur API < 29, toutes les méthodes retournent [ThermalStatus.Unknown].
 * Aucune permission requise.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { ThermalHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Lecture synchrone
 * val status = thermalHelper.getStatus()
 * if (status >= ThermalStatus.Severe) reduceWorkload()
 *
 * // Stream réactif (dans viewModelScope)
 * thermalHelper.statusFlow(executor = Executors.newSingleThreadExecutor())
 *     .onEach { status -> adaptPerformance(status) }
 *     .launchIn(viewModelScope)
 * ```
 */
class ThermalHelper(context: Context) {

    internal val appContext   = context.applicationContext
    internal val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    // ── Synchronous ───────────────────────────────────────────────────────────

    fun getStatus(): ThermalStatus =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            powerManager.currentThermalStatus.toThermalStatus()
        else
            ThermalStatus.Unknown

    fun isThrottling(): Boolean = when (getStatus()) {
        ThermalStatus.Unknown, ThermalStatus.None -> false
        else -> true
    }

    fun isCritical(): Boolean = when (getStatus()) {
        ThermalStatus.Critical, ThermalStatus.Emergency, ThermalStatus.Shutdown -> true
        else -> false
    }

    // ── Reactive flow ─────────────────────────────────────────────────────────

    @RequiresApi(Build.VERSION_CODES.Q)
    fun statusFlow(executor: Executor): Flow<ThermalStatus> = callbackFlow {
        trySend(getStatus())

        val listener = PowerManager.OnThermalStatusChangedListener { status ->
            trySend(status.toThermalStatus())
        }

        powerManager.addThermalStatusListener(executor, listener)
        awaitClose { powerManager.removeThermalStatusListener(listener) }
    }.distinctUntilChanged()

    // ── Internal ──────────────────────────────────────────────────────────────

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun Int.toThermalStatus(): ThermalStatus = when (this) {
        PowerManager.THERMAL_STATUS_NONE      -> ThermalStatus.None
        PowerManager.THERMAL_STATUS_LIGHT     -> ThermalStatus.Light
        PowerManager.THERMAL_STATUS_MODERATE  -> ThermalStatus.Moderate
        PowerManager.THERMAL_STATUS_SEVERE    -> ThermalStatus.Severe
        PowerManager.THERMAL_STATUS_CRITICAL  -> ThermalStatus.Critical
        PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalStatus.Emergency
        PowerManager.THERMAL_STATUS_SHUTDOWN  -> ThermalStatus.Shutdown
        else                                  -> ThermalStatus.Unknown
    }
}