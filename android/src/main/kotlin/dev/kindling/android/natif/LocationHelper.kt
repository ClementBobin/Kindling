package dev.kindling.android.natif

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// ─────────────────────────────────────────────
//  LocationConfig
// ─────────────────────────────────────────────

/**
 * Describes the accuracy and frequency of location updates.
 *
 * Use the provided presets:
 * - [LocationConfig.HighAccuracy] -> Precise GPS fixes, frequent updates.
 * - [LocationConfig.Balanced]     -> Balance between accuracy and battery life (Wifi/Cellular).
 * - [LocationConfig.LowPower]     -> Network-only fixes, optimized for battery.
 * - [LocationConfig.Passive]      -> Receives updates triggered by other apps only.
 */
data class LocationConfig(
    val priority: Int       = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
    val intervalMs: Long    = 10_000L,
    val minUpdateMs: Long   = 5_000L,
    val minUpdateMeters: Float = 0f
) {
    companion object {
        val HighAccuracy = LocationConfig(
            priority      = Priority.PRIORITY_HIGH_ACCURACY,
            intervalMs    = 5_000L,
            minUpdateMs   = 2_000L
        )
        val Balanced = LocationConfig(
            priority      = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            intervalMs    = 10_000L,
            minUpdateMs   = 5_000L
        )
        val LowPower = LocationConfig(
            priority      = Priority.PRIORITY_LOW_POWER,
            intervalMs    = 30_000L,
            minUpdateMs   = 15_000L
        )
        val Passive = LocationConfig(
            priority      = Priority.PRIORITY_PASSIVE,
            intervalMs    = 60_000L,
            minUpdateMs   = 30_000L
        )
    }
}

// ─────────────────────────────────────────────
//  LocationHelper
// ─────────────────────────────────────────────

/**
 * Centralized location helper based on Google Play Services FusedLocationProviderClient.
 *
 * This utility provides high-level APIs for fetching the last known location,
 * getting a single fresh fix, or observing a continuous stream of updates.
 *
 * **Requirements:**
 * - Google Play Services dependency in `android/build.gradle.kts`
 * - Permissions: `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`
 *
 * ### Example usage:
 * ```kotlin
 * val locationHelper = LocationHelper(context)
 *
 * // Get last known location (fast, can be null)
 * val last = locationHelper.getLastLocation()
 *
 * // Get single fresh fix
 * val location = locationHelper.getCurrentLocation(LocationConfig.HighAccuracy)
 *
 * // Continuous stream
 * viewModelScope.launch {
 *     locationHelper.locationFlow(LocationConfig.Balanced).collect { location ->
 *         updateMap(location)
 *     }
 * }
 * ```
 */
class LocationHelper(context: Context) {

    internal val appContext  = context.applicationContext
    internal val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)
    internal val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // ── Availability ──────────────────────────────────────────────────────────

    fun isGpsEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    fun isNetworkLocationEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    fun isAnyProviderEnabled(): Boolean = isGpsEnabled() || isNetworkLocationEnabled()

    // ── Last known location ───────────────────────────────────────────────────

    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    suspend fun getLastLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener  { cont.resume(it) }
                .addOnFailureListener  { cont.resumeWithException(it) }
                .addOnCanceledListener { cont.cancel() }
        }

    // ── Single fix ────────────────────────────────────────────────────────────

    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    suspend fun getCurrentLocation(config: LocationConfig = LocationConfig.Balanced): Location? =
        suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(config.priority, null)
                .addOnSuccessListener  { cont.resume(it) }
                .addOnFailureListener  { cont.resumeWithException(it) }
                .addOnCanceledListener { cont.cancel() }
        }

    // ── Continuous flow ───────────────────────────────────────────────────────

    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    fun locationFlow(config: LocationConfig = LocationConfig.Balanced): Flow<Location> =
        callbackFlow {
            val request = LocationRequest.Builder(config.priority, config.intervalMs)
                .setMinUpdateIntervalMillis(config.minUpdateMs)
                .setMinUpdateDistanceMeters(config.minUpdateMeters)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { trySend(it) }
                }
            }

            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnFailureListener { e ->
                    fusedClient.removeLocationUpdates(callback)
                    close(e)
                }

            awaitClose { fusedClient.removeLocationUpdates(callback) }
        }
}