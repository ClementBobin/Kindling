package dev.kindling.android.natif

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// ─────────────────────────────────────────────
//  BluetoothState
// ─────────────────────────────────────────────

sealed class BluetoothState {
    data object On          : BluetoothState()
    data object Off         : BluetoothState()
    data object TurningOn   : BluetoothState()
    data object TurningOff  : BluetoothState()
    data object Unavailable : BluetoothState()
}

// ─────────────────────────────────────────────
//  BluetoothScanConfig
// ─────────────────────────────────────────────

/**
 * Décrit une configuration de scan Bluetooth.
 *
 * Presets :
 * - [BluetoothScanConfig.Default] → scan classique sans filtre
 */
data class BluetoothScanConfig(
    val durationMs: Long = 12_000L
) {
    companion object {
        val Default = BluetoothScanConfig()
        val Quick   = BluetoothScanConfig(durationMs = 5_000L)
    }
}

// ─────────────────────────────────────────────
//  BluetoothHelper
// ─────────────────────────────────────────────

/**
 * Helper Bluetooth centralisé.
 *
 * Permissions requises (API 31+) :
 * - `BLUETOOTH_SCAN`    → découverte d'appareils
 * - `BLUETOOTH_CONNECT` → connexion / lecture du nom
 *
 * API < 31 : `BLUETOOTH` + `BLUETOOTH_ADMIN`.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { BluetoothHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // État courant
 * val state = bluetoothHelper.getState()
 *
 * // Appareils couplés
 * val paired = bluetoothHelper.getPairedDevices()
 *
 * // Observer l'état
 * bluetoothHelper.stateFlow.onEach { state -> … }.launchIn(viewModelScope)
 *
 * // Scanner (Flow d'appareils découverts)
 * bluetoothHelper.scanFlow(BluetoothScanConfig.Quick)
 *     .onEach { device -> … }
 *     .launchIn(viewModelScope)
 * ```
 */
class BluetoothHelper(context: Context) {

    internal val appContext     = context.applicationContext
    internal val bluetoothManager =
        appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    internal val adapter: BluetoothAdapter? = bluetoothManager?.adapter

    // ── State ─────────────────────────────────────────────────────────────────

    fun isAvailable(): Boolean = adapter != null

    fun getState(): BluetoothState {
        val a = adapter ?: return BluetoothState.Unavailable
        return when (a.state) {
            BluetoothAdapter.STATE_ON          -> BluetoothState.On
            BluetoothAdapter.STATE_OFF         -> BluetoothState.Off
            BluetoothAdapter.STATE_TURNING_ON  -> BluetoothState.TurningOn
            BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.TurningOff
            else                               -> BluetoothState.Unavailable
        }
    }

    val stateFlow: Flow<BluetoothState> = callbackFlow {
        trySend(getState())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    trySend(getState())
                }
            }
        }
        appContext.registerReceiver(
            receiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        awaitClose { appContext.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    // ── Paired devices ────────────────────────────────────────────────────────

    @RequiresPermission(
        anyOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH]
    )
    fun getPairedDevices(): Set<BluetoothDevice> =
        adapter?.bondedDevices ?: emptySet()

    // ── Discovery flow ────────────────────────────────────────────────────────

    @RequiresPermission(
        anyOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN]
    )
    fun scanFlow(config: BluetoothScanConfig = BluetoothScanConfig.Default): Flow<BluetoothDevice> =
        callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == BluetoothDevice.ACTION_FOUND) {
                        val device: BluetoothDevice? =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                            else
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let { trySend(it) }
                    }
                }
            }

            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            appContext.registerReceiver(receiver, filter)
            adapter?.startDiscovery()

            // Auto-cancel after duration
            kotlinx.coroutines.delay(config.durationMs)
            adapter?.cancelDiscovery()
            close()

            awaitClose {
                adapter?.cancelDiscovery()
                appContext.unregisterReceiver(receiver)
            }
        }
}