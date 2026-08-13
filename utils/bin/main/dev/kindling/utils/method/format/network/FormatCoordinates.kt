package dev.kindling.utils.method.format.network

import kotlin.math.*

// ─── FormatCoordinates ────────────────────────────────────────────────────────

/**
 * Represents a geographic coordinate pair.
 */
data class GeoCoordinate(val latitude: Double, val longitude: Double)

/**
 * Formats a decimal latitude as a DMS string with N/S indicator.
 * Example: `48.8566.latToDms()` → `"48° 51' 24\" N"`
 */
fun Double.latToDms(): String {
    val totalSeconds = round(abs(this) * 3600).toInt()
    val degrees = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val dir     = if (this >= 0) "N" else "S"
    return "$degrees° $minutes' $seconds\" $dir"
}

/**
 * Formats a decimal longitude as a DMS string with E/W indicator.
 * Example: `2.3522.lngToDms()` → `"2° 21' 8\" E"`
 */
fun Double.lngToDms(): String {
    val totalSeconds = round(abs(this) * 3600).toInt()
    val degrees = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val dir     = if (this >= 0) "E" else "W"
    return "$degrees° $minutes' $seconds\" $dir"
}

/**
 * Formats a [GeoCoordinate] as a DMS string.
 * Example: `GeoCoordinate(48.8566, 2.3522).toDms()` → `"48° 51' 24\" N, 2° 21' 8\" E"`
 */
fun GeoCoordinate.toDms(): String = "${latitude.latToDms()}, ${longitude.lngToDms()}"

/**
 * Formats a [GeoCoordinate] as a decimal degrees string.
 * Example: `GeoCoordinate(48.8566, 2.3522).toDecimalDegrees()` → `"48.8566°N, 2.3522°E"`
 */
fun GeoCoordinate.toDecimalDegrees(decimals: Int = 4): String {
    val lat = "${"%.${decimals}f".format(abs(latitude))}°${if (latitude >= 0) "N" else "S"}"
    val lng = "${"%.${decimals}f".format(abs(longitude))}°${if (longitude >= 0) "E" else "W"}"
    return "$lat, $lng"
}

/**
 * Computes the Haversine great-circle distance in kilometers between two coordinates.
 * Example: `GeoCoordinate(48.8566, 2.3522).distanceTo(GeoCoordinate(51.5074, -0.1278))` → `~341.0 km`
 */
fun GeoCoordinate.distanceTo(other: GeoCoordinate): Double {
    val r    = 6371.0
    val dLat = Math.toRadians(other.latitude - latitude)
    val dLng = Math.toRadians(other.longitude - longitude)
    val a    = sin(dLat / 2).pow(2) +
               cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) *
               sin(dLng / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

/**
 * Formats the distance between two coordinates as a human-readable string.
 * Example: `paris.distanceLabelTo(london)` → `"341 km"`
 */
fun GeoCoordinate.distanceLabelTo(other: GeoCoordinate, inMiles: Boolean = false): String {
    val km = distanceTo(other)
    return if (inMiles) "${"%.1f".format(km * 0.621371)} mi"
           else "${"%.1f".format(km)} km"
}

/**
 * Returns a Google Maps URL for this coordinate.
 * Example: `GeoCoordinate(48.8566, 2.3522).toGoogleMapsUrl()` → `"https://maps.google.com/?q=48.8566,2.3522"`
 */
fun GeoCoordinate.toGoogleMapsUrl(): String =
    "https://maps.google.com/?q=${latitude},${longitude}"

/**
 * Parses a `"lat,lng"` string into a [GeoCoordinate].
 * Example: `"48.8566,2.3522".toGeoCoordinate()` → `GeoCoordinate(48.8566, 2.3522)`
 */
fun String.toGeoCoordinate(): GeoCoordinate? {
    val parts = split(",").map { it.trim().toDoubleOrNull() }
    if (parts.size != 2 || parts.any { it == null }) return null
    val lat = parts[0]!!
    val lng = parts[1]!!
    if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return null
    return GeoCoordinate(lat, lng)
}

/**
 * Returns true if this coordinate is within [radiusKm] of [other].
 */
fun GeoCoordinate.isWithinKm(other: GeoCoordinate, radiusKm: Double): Boolean =
    distanceTo(other) <= radiusKm
