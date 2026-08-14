package dev.kindling.core.components.ui.maps.blocks.analyticsmapcard

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.GeoJsonData
import dev.sargunv.maplibrecompose.core.LatLng
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.feature
import dev.sargunv.maplibrecompose.expressions.dsl.interpolate
import dev.sargunv.maplibrecompose.expressions.dsl.linear
import dev.sargunv.maplibrecompose.expressions.value.CirclePitchAlignment

// ── Style URLs ────────────────────────────────────────────────────────────────

/**
 * Blank (no labels) basemap — keeps the card visually clean.
 * Can be swapped for any MapLibre style URL.
 */
private const val STYLE_BLANK_LIGHT =
    "https://tiles.openfreemap.org/styles/positron"
private const val STYLE_BLANK_DARK =
    "https://tiles.openfreemap.org/styles/dark"

// ── Dot colour ────────────────────────────────────────────────────────────────

private val DOT_COLOR      = Color(0xFF6366F1) // indigo-500
private val DOT_GLOW_COLOR = Color(0xFF6366F1)

/**
 * KAnalyticsMapCard
 *
 * Compact analytics card showing a headline metric (or a scrollable row of
 * metrics) over a world map with weighted activity hotspots.
 *
 * Mirrors the mapcn `analytics-card` block.
 *
 * ---
 * @param title       Card title shown above the metric strip ("Global traffic"…)
 * @param subtitle    Optional subtitle / time range ("Last 30 days")
 * @param metrics     List of [KAnalyticsMetric] shown in the scrollable strip.
 *                    Pass a single-item list to get the "headline metric" look.
 * @param hotspots    Geographic activity dots rendered on the map.
 * @param mapHeight   Height of the embedded map area (default 220.dp).
 * @param styleLight  MapLibre style URL for light mode.
 * @param styleDark   MapLibre style URL for dark mode.
 * @param modifier    Modifier applied to the root [Card].
 */
@Composable
fun KAnalyticsMapCard(
    title: String = "Global traffic",
    subtitle: String? = "Last 30 days",
    metrics: List<KAnalyticsMetric> = kSampleAnalyticsMetrics,
    hotspots: List<KAnalyticsHotspot> = kSampleAnalyticsHotspots,
    mapHeight: Dp = 220.dp,
    styleLight: String = STYLE_BLANK_LIGHT,
    styleDark: String  = STYLE_BLANK_DARK,
    modifier: Modifier = Modifier,
) {
    val isDark   = isSystemInDarkTheme()
    val geojson  = remember(hotspots) { buildHotspotGeoJson(hotspots) }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = LatLng(20.0, 10.0),
            zoom   = 0.8,
        )
    )

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column {
            // ── Header ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            ) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Metrics strip ──────────────────────────────────────────────
            LazyRow(
                contentPadding    = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            ) {
                items(metrics) { metric ->
                    KAnalyticsMetricChip(metric = metric)
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )

            // ── Map ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
            ) {
                val source = rememberGeoJsonSource(GeoJsonData.JsonString(geojson))

                MaplibreMap(
                    modifier   = Modifier.fillMaxSize(),
                    styleUri   = if (isDark) styleDark else styleLight,
                    cameraState = cameraState,
                    // Disable gestures — it's a card widget, not an interactive map
                    isScrollGesturesEnabled   = false,
                    isZoomGesturesEnabled     = false,
                    isRotateGesturesEnabled   = false,
                    isTiltGesturesEnabled     = false,
                ) {
                    // Glow ring — radius driven by weight property
                    CircleLayer(
                        id      = "amc-glow",
                        source  = source,
                        radius  = interpolate(
                            type  = linear(),
                            input = feature.getNumber("weight"),
                            0f to const(8f),
                            1f to const(22f),
                        ),
                        color   = const(DOT_GLOW_COLOR),
                        opacity = const(0.20f),
                        pitchAlignment = const(CirclePitchAlignment.Map),
                    )

                    // Core dot — smaller, full opacity
                    CircleLayer(
                        id      = "amc-dots",
                        source  = source,
                        radius  = interpolate(
                            type  = linear(),
                            input = feature.getNumber("weight"),
                            0f to const(3f),
                            1f to const(8f),
                        ),
                        color   = const(DOT_COLOR),
                        strokeColor = const(Color.White),
                        strokeWidth = const(1.5f),
                        pitchAlignment = const(CirclePitchAlignment.Map),
                    )
                }

                // Bottom scrim so the card edge blends into the surface
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                )
                            )
                        ),
                )
            }
        }
    }
}

// ── GeoJSON helper ────────────────────────────────────────────────────────────

private fun buildHotspotGeoJson(hotspots: List<KAnalyticsHotspot>): String {
    val features = hotspots.joinToString(",") { h ->
        """
        {
          "type": "Feature",
          "properties": {
            "id":     "${h.id}",
            "weight": ${h.weight},
            "label":  ${h.label?.let { "\"$it\"" } ?: "null"}
          },
          "geometry": {
            "type": "Point",
            "coordinates": [${h.longitude}, ${h.latitude}]
          }
        }
        """.trimIndent()
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}