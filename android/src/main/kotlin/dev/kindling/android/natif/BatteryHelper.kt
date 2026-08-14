package dev.kindling.android.natif

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
 * Current battery status information.
 *
 * @param level The battery level percentage [0..100].
 * @param isCharging Whether the device is currently plugged into a power source.
 * @param chargeType The specific source of power (AC, USB, Wireless) or [ChargeType.None].
 * @param health The physical health state of the battery (e.g., Good, Overheat).
 * @param temperature The battery temperature in tenths of a degree Celsius (e.g., 250 = 25.0°C).
 * @param voltage The current battery voltage in millivolts.
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
 * Centralized battery helper for Android.
 *
 * This utility provides synchronous access and reactive flows for monitoring battery
 * status, level, and power-saving modes.
 *
 * **No permissions required.**
 *
 * ### Example usage:
 * ```kotlin
 * val batteryHelper = BatteryHelper(context)
 *
 * // Synchronous check
 * val status = batteryHelper.getStatus()
 * println("${status.level}% - ${if (status.isCharging) "Charging" else "On Battery"}")
 *
 * // Reactive flow
 * viewModelScope.launch {
 *     batteryHelper.statusFlow.collect { status ->
 *         updateBatteryUI(status)
 *     }
 * }
 *
 * // Power save mode
 * if (batteryHelper.isPowerSaveMode()) {
 *     disableExpensiveAnimations()
 * }
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
     * A [Flow] that emits a [BatteryStatus] whenever the battery state changes.
     * It emits the current status immediately upon subscription.
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