package dev.kindling.core.components.ui.maps

import androidx.compose.ui.graphics.Color

data class KMapRoute(
    val id: String,
    val coordinates: List<Pair<Double, Double>>, // (lat, lng)
    val color: Color = Color(0xFF3B82F6),
    val widthDp: Float = 4f,
)