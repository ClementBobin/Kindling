package dev.kindling.core.components.ui.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class KMapState(
    initialLatitude: Double = 48.8566,
    initialLongitude: Double = 2.3522,
    initialZoom: Double = 10.0,
) {
    var latitude by mutableStateOf(initialLatitude)
    var longitude by mutableStateOf(initialLongitude)
    var zoom by mutableStateOf(initialZoom)

    fun moveTo(latitude: Double, longitude: Double, zoom: Double = this.zoom) {
        this.latitude = latitude
        this.longitude = longitude
        this.zoom = zoom
    }
}

@Composable
fun rememberKMapState(
    initialLatitude: Double = 48.8566,
    initialLongitude: Double = 2.3522,
    initialZoom: Double = 10.0,
): KMapState = remember {
    KMapState(initialLatitude, initialLongitude, initialZoom)
}