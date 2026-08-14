package dev.kindling.core.components.ui.maps.blocks.logisticsnetwork

import androidx.compose.ui.graphics.Color

// ── Node type ─────────────────────────────────────────────────────────────────

enum class KLogisticsNodeType {
    Warehouse,
    DistributionCenter,
    Store,
}

fun KLogisticsNodeType.label(): String = when (this) {
    KLogisticsNodeType.Warehouse          -> "Warehouse"
    KLogisticsNodeType.DistributionCenter -> "Distribution center"
    KLogisticsNodeType.Store              -> "Store"
}

fun KLogisticsNodeType.color(): Color = when (this) {
    KLogisticsNodeType.Warehouse          -> Color(0xFF6366F1) // indigo
    KLogisticsNodeType.DistributionCenter -> Color(0xFFF59E0B) // amber
    KLogisticsNodeType.Store              -> Color(0xFF22C55E) // green
}

// ── Route status ──────────────────────────────────────────────────────────────

enum class KLogisticsRouteStatus {
    Active,
    Delayed,
    Inactive,
}

fun KLogisticsRouteStatus.color(): Color = when (this) {
    KLogisticsRouteStatus.Active   -> Color(0xFF6366F1)
    KLogisticsRouteStatus.Delayed  -> Color(0xFFF59E0B)
    KLogisticsRouteStatus.Inactive -> Color(0xFF94A3B8)
}

fun KLogisticsRouteStatus.label(): String = when (this) {
    KLogisticsRouteStatus.Active   -> "Active"
    KLogisticsRouteStatus.Delayed  -> "Delayed"
    KLogisticsRouteStatus.Inactive -> "Inactive"
}

// ── Models ────────────────────────────────────────────────────────────────────

data class KLogisticsNode(
    val id: String,
    val name: String,
    val city: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val type: KLogisticsNodeType,
    val shipments: Int,
    val capacity: Int,         // percent 0–100
    val onTimeRate: Float,     // percent 0–100
)

data class KLogisticsRoute(
    val id: String,
    val fromId: String,
    val toId: String,
    val status: KLogisticsRouteStatus,
    val dailyShipments: Int,
    val avgTransitHours: Int,
)