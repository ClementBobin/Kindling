package dev.kindling.android.natif

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// ─────────────────────────────────────────────
//  WifiState
// ─────────────────────────────────────────────

sealed class WifiState {
    data object Enabled     : WifiState()
    data object Disabled    : WifiState()
    data object Enabling    : WifiState()
    data object Disabling   : WifiState()
    data object Unknown     : WifiState()
}

// ─────────────────────────────────────────────
//  WifiNetworkInfo
// ─────────────────────────────────────────────

/** Informations sur le réseau Wi-Fi actuellement connecté. */
data class WifiNetworkInfo(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val linkSpeedMbps: Int,
    val frequency: Int
)

// ─────────────────────────────────────────────
//  WifiHelper
// ─────────────────────────────────────────────

/**
 * Helper Wi-Fi centralisé.
 *
 * Permissions requises :
 * - `ACCESS_WIFI_STATE`          → état et infos réseau
 * - `CHANGE_WIFI_STATE`          → activer/désactiver (API < 29 seulement)
 * - `ACCESS_FINE_LOCATION`       → scan des réseaux (requis par Android)
 *
 * Nota : sur API 29+, activer/désactiver le Wi-Fi via l'app n'est plus possible
 * — [openWifiSettings] redirige vers les réglages système.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { WifiHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * val state = wifiHelper.getState()
 * val info  = wifiHelper.getCurrentNetworkInfo()
 *
 * wifiHelper.stateFlow.onEach { … }.launchIn(viewModelScope)
 *
 * wifiHelper.scanFlow()
 *     .onEach { results -> showNetworks(results) }
 *     .launchIn(viewModelScope)
 * ```
 */
class WifiHelper(context: Context) {

    internal val appContext  = context.applicationContext
    @SuppressLint("WifiManagerPotentialLeak")
    internal val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // ── State ─────────────────────────────────────────────────────────────────

    @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
    fun getState(): WifiState = when (wifiManager.wifiState) {
        WifiManager.WIFI_STATE_ENABLED   -> WifiState.Enabled
        WifiManager.WIFI_STATE_DISABLED  -> WifiState.Disabled
        WifiManager.WIFI_STATE_ENABLING  -> WifiState.Enabling
        WifiManager.WIFI_STATE_DISABLING -> WifiState.Disabling
        else                             -> WifiState.Unknown
    }

    @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
    fun isEnabled(): Boolean = wifiManager.isWifiEnabled

    /** Sur API 29+, redirige vers les réglages — modification programmatique interdite. */
    fun openWifiSettings(context: Context) {
        context.startActivity(
            Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    // ── Current network ───────────────────────────────────────────────────────

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE])
    fun getCurrentNetworkInfo(): WifiNetworkInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+ : connectionInfo déprécié, on passe par ConnectivityManager
            val connectivityManager =
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return null
            val caps    = connectivityManager.getNetworkCapabilities(network) ?: return null
            val info    = caps.transportInfo as? WifiInfo ?: return null
            return info.toNetworkInfo()
        } else {
            @Suppress("DEPRECATION")
            val info = wifiManager.connectionInfo ?: return null
            if (info.networkId == -1) return null
            return info.toNetworkInfo()
        }
    }

    private fun WifiInfo.toNetworkInfo(): WifiNetworkInfo? {
        if (networkId == -1) return null
        return WifiNetworkInfo(
            ssid          = ssid.removePrefix("\"").removeSuffix("\""),
            bssid         = bssid ?: "",
            rssi          = rssi,
            linkSpeedMbps = linkSpeed,
            frequency     = frequency
        )
    }

    // ── State flow ────────────────────────────────────────────────────────────

    @get:RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
    val stateFlow: Flow<WifiState> get() = callbackFlow {
        trySend(getState())
        val receiver = object : BroadcastReceiver() {
            @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == WifiManager.WIFI_STATE_CHANGED_ACTION)
                    trySend(getState())
            }
        }
        appContext.registerReceiver(receiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        awaitClose { appContext.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    // ── Scan flow ─────────────────────────────────────────────────────────────

    /**
     * Flow émettant la liste des réseaux Wi-Fi découverts à chaque scan.
     * Chaque émission est déclenchée par [WifiManager.startScan].
     */
    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CHANGE_WIFI_STATE
    ])
    fun scanFlow(): Flow<List<ScanResult>> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    trySend(wifiManager.scanResults ?: emptyList())
                }
            }
        }
        appContext.registerReceiver(receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        @Suppress("DEPRECATION")
        if (!wifiManager.startScan()) {
            appContext.unregisterReceiver(receiver)
            close(IllegalStateException("WifiManager.startScan() failed — throttled or unavailable"))
            return@callbackFlow
        }

        awaitClose { appContext.unregisterReceiver(receiver) }
    }
}