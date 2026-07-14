package dev.kindling.android.helper.natif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// ─────────────────────────────────────────────
//  BatteryStatus
// ─────────────────────────────────────────────

/**
 * État courant de la batterie.
 *
 * @param level          Niveau [0..100].
 * @param isCharging     `true` si en charge (USB ou AC).
 * @param chargeType     Type de charge ou [ChargeType.None].
 * @param health         Santé de la batterie.
 * @param temperature    Température en dixièmes de degré Celsius (ex. 250 = 25.0°C).
 * @param voltage        Tension en millivolts.
 */
data class BatteryStatus(
    val level: Int,
    val isCharging: Boolean,
    val chargeType: ChargeType,
    val health: BatteryHealth,
    val temperature: Int,
    val voltage: Int
) {
    val temperatureCelsius: Float get() = temperature / 10f
}

enum class ChargeType { Ac, Usb, Wireless, None }

enum class BatteryHealth {
    Good, Overheat, Dead, OverVoltage, UnspecifiedFailure, Cold, Unknown
}

// ─────────────────────────────────────────────
//  BatteryHelper
// ─────────────────────────────────────────────

/**
 * Helper batterie centralisé.
 *
 * Aucune permission requise.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { BatteryHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Lecture synchrone
 * val status = batteryHelper.getStatus()
 * println("${status.level}% — ${if (status.isCharging) "en charge" else "sur batterie"}")
 *
 * // Stream réactif
 * batteryHelper.statusFlow
 *     .onEach { status -> updateBatteryUI(status) }
 *     .launchIn(viewModelScope)
 *
 * // Mode économie d'énergie
 * val saving = batteryHelper.isPowerSaveMode()
 * ```
 */
class BatteryHelper(context: Context) {

    internal val appContext   = context.applicationContext
    internal val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    // ── Synchronous ───────────────────────────────────────────────────────────

    fun getStatus(): BatteryStatus? {
        val intent = appContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return null
        return intent.toBatteryStatus()
    }

    fun getLevel(): Int? = getStatus()?.level

    fun isCharging(): Boolean = getStatus()?.isCharging ?: false

    fun isPowerSaveMode(): Boolean = powerManager.isPowerSaveMode

    fun isIgnoringBatteryOptimizations(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(appContext.packageName)
        } else {
            true
        }

    // ── Reactive flow ─────────────────────────────────────────────────────────

    /**
     * Flow émettant un [BatteryStatus] à chaque changement d'état batterie.
     * Émet immédiatement l'état courant à la souscription.
     */
    val statusFlow: Flow<BatteryStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                intent.toBatteryStatus()?.let { trySend(it) }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        getStatus()?.let { trySend(it) }

        awaitClose { appContext.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun Intent.toBatteryStatus(): BatteryStatus? {
        val rawLevel  = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale     = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (rawLevel == -1 || scale <= 0) return null

        val level     = (rawLevel * 100 / scale.toFloat()).toInt()
        val status    = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        val plugged   = getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargeType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC      -> ChargeType.Ac
            BatteryManager.BATTERY_PLUGGED_USB     -> ChargeType.Usb
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargeType.Wireless
            else                                   -> ChargeType.None
        }

        val health = when (getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD                -> BatteryHealth.Good
            BatteryManager.BATTERY_HEALTH_OVERHEAT            -> BatteryHealth.Overheat
            BatteryManager.BATTERY_HEALTH_DEAD                -> BatteryHealth.Dead
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE        -> BatteryHealth.OverVoltage
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UnspecifiedFailure
            BatteryManager.BATTERY_HEALTH_COLD                -> BatteryHealth.Cold
            else                                              -> BatteryHealth.Unknown
        }

        return BatteryStatus(
            level        = level,
            isCharging   = isCharging,
            chargeType   = chargeType,
            health       = health,
            temperature  = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0),
            voltage      = getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        )
    }
}