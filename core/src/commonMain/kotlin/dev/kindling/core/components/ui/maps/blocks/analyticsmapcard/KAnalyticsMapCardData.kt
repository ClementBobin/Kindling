package dev.kindling.core.components.ui.maps.blocks.analyticsmapcard

// ── Trend direction ───────────────────────────────────────────────────────────

enum class KMetricTrend { Up, Down, Neutral }

// ── Model ─────────────────────────────────────────────────────────────────────

/**
 * One statistic displayed in the header strip of [KAnalyticsMapCard].
 *
 * @param label       Short label shown below the value ("Active users", "Sessions"…)
 * @param value       Formatted display value ("1.2M", "94.3%"…)
 * @param trend       Direction of change vs the previous period.
 * @param changeText  Human-readable delta shown next to the trend arrow ("↑ 12%").
 *                    When null the arrow is shown without additional text.
 */
data class KAnalyticsMetric(
    val label: String,
    val value: String,
    val trend: KMetricTrend = KMetricTrend.Neutral,
    val changeText: String? = null,
)

/**
 * A geographic hotspot shown as a dot on the map.
 *
 * @param id        Unique identifier (used as GeoJSON feature id).
 * @param latitude  WGS-84 latitude.
 * @param longitude WGS-84 longitude.
 * @param weight    Relative intensity (0f–1f). Drives dot radius on the map.
 * @param label     Optional tooltip label (city name, region…).
 */
data class KAnalyticsHotspot(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val weight: Float = 1f,        // 0f–1f
    val label: String? = null,
)