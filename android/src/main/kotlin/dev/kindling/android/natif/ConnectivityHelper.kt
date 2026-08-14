package dev.kindling.android.natif

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// ─────────────────────────────────────────────
//  NetworkStatus
// ─────────────────────────────────────────────

/**
 * Represents the current network connectivity status.
 *
 * - [NetworkStatus.Available]: Device is online (transport type specified).
 * - [NetworkStatus.Unavailable]: Device is offline.
 * - [NetworkStatus.Losing]: Connection is currently being lost (e.g., transitioning between networks).
 */
sealed class NetworkStatus {
    data class Available(val transport: NetworkTransport) : NetworkStatus()
    data object Unavailable : NetworkStatus()
    data object Losing      : NetworkStatus()
}

enum class NetworkTransport { Wifi, Cellular, Ethernet, Other }

// ─────────────────────────────────────────────
//  ConnectivityHelper
// ─────────────────────────────────────────────

/**
 * Centralized network connectivity helper for Android.
 *
 * This utility provides synchronous checks and reactive flows for monitoring
 * internet availability and transport types (Wifi, Cellular, etc.).
 *
 * **Requires permission:** `ACCESS_NETWORK_STATE`
 *
 * ### Example usage:
 * ```kotlin
 * val connectivityHelper = ConnectivityHelper(context)
 * 
 * // Synchronous check
 * if (connectivityHelper.isOnline()) {
 *     refreshContent()
 * }
 * 
 * // Reactive observation
 * viewModelScope.launch {
 *     connectivityHelper.statusFlow.collect { status ->
 *         when (status) {
 *             is NetworkStatus.Available -> showOnlineMode()
 *             is NetworkStatus.Unavailable -> showOfflineBanner()
 *             is NetworkStatus.Losing -> Unit
 *         }
 *     }
 * }
 * ```
 */
class ConnectivityHelper(context: Context) {

    internal val manager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Retourne les [NetworkCapabilities] du réseau actif, ou `null`.
     * Guard API 23 intégré : retourne `null` sur API < 23.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun activeCapabilities(): NetworkCapabilities? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.activeNetwork?.let { manager.getNetworkCapabilities(it) }
        } else {
            null // API < 23 : pas de NetworkCapabilities synchrone
        }
    }

    private fun NetworkCapabilities.resolveTransport(): NetworkTransport = when {
        hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> NetworkTransport.Wifi
        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkTransport.Cellular
        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkTransport.Ethernet
        else                                                  -> NetworkTransport.Other
    }

    /** `true` if [caps] represents a usable internet connection (internet + validated). */
    private fun NetworkCapabilities.isUsable(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))

    // ── Synchronous ───────────────────────────────────────────────────────────

    /** `true` si une connexion réseau validée est disponible. */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isOnline(): Boolean =
        activeCapabilities()?.isUsable() == true || isOnlineLegacy()

    @Suppress("DEPRECATION")
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isOnlineLegacy(): Boolean =
        manager.activeNetworkInfo?.isConnected == true

    /** Transport actif, ou `null` si hors ligne. */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun currentTransport(): NetworkTransport? =
        activeCapabilities()?.takeIf { it.isUsable() }?.resolveTransport()

    // ── Reactive ──────────────────────────────────────────────────────────────

    /**
     * A [Flow] that emits [NetworkStatus] whenever the network connectivity changes.
     * It emits the current status immediately upon subscription.
     *
     * The `ACCESS_NETWORK_STATE` permission must be declared in the Manifest.
     */
    @get:RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    val statusFlow: Flow<NetworkStatus> get() = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
            override fun onAvailable(network: Network) {
                val caps = manager.getNetworkCapabilities(network)
                if (caps?.isUsable() == true) {
                    trySend(NetworkStatus.Available(caps.resolveTransport()))
                }
            }

            @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
            override fun onLost(network: Network) {
                // Only emit Unavailable if no other validated network remains.
                val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.activeNetwork?.let { manager.getNetworkCapabilities(it) }
                } else null
                if (current?.isUsable() == true) {
                    trySend(NetworkStatus.Available(current.resolveTransport()))
                } else {
                    trySend(NetworkStatus.Unavailable)
                }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                trySend(NetworkStatus.Losing)
            }

            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        manager.registerNetworkCallback(request, callback)

        // Émet l'état courant immédiatement
        val current = activeCapabilities()
        if (current?.isUsable() == true) {
            trySend(NetworkStatus.Available(current.resolveTransport()))
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && isOnlineLegacy()) {
            trySend(NetworkStatus.Available(NetworkTransport.Other))
        } else {
            trySend(NetworkStatus.Unavailable)
        }

        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}