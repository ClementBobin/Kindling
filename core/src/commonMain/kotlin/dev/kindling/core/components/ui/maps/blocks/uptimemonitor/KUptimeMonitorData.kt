package dev.kindling.core.components.ui.maps.blocks.uptimemonitor

import androidx.compose.ui.graphics.Color

// ── Status ────────────────────────────────────────────────────────────────────

enum class KEdgeStatus {
    Healthy,
    Degraded,
    Down,
}

fun KEdgeStatus.color(): Color = when (this) {
    KEdgeStatus.Healthy  -> Color(0xFF22C55E) // green-500
    KEdgeStatus.Degraded -> Color(0xFFF59E0B) // amber-500
    KEdgeStatus.Down     -> Color(0xFFEF4444) // red-500
}

fun KEdgeStatus.label(): String = when (this) {
    KEdgeStatus.Healthy  -> "Healthy"
    KEdgeStatus.Degraded -> "Degraded"
    KEdgeStatus.Down     -> "Down"
}

// ── Model ─────────────────────────────────────────────────────────────────────

data class KEdgeNode(
    val id: String,
    val city: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val status: KEdgeStatus,
    val latencyMs: Int,
    val uptimePercent: Float,   // 0f–100f
    val requestsPerSec: Int,
)