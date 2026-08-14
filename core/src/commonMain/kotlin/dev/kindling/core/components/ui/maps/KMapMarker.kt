package dev.kindling.core.components.ui.maps

import androidx.compose.ui.graphics.Color

data class KMapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String? = null,
    val snippet: String? = null,
    val color: Color = Color(0xFFEF4444), // rouge par défaut comme mapcn
)