package dev.kindling.core.components.ui.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.GeoJsonData
import dev.sargunv.maplibrecompose.expressions.dsl.const
import io.github.mundosk.geojson.Feature
import io.github.mundosk.geojson.FeatureCollection
import io.github.mundosk.geojson.GeoJsonObject
import io.github.mundosk.geojson.Point
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// Style tiles gratuits — peut être remplacé par n'importe quel style MapLibre
private const val DEFAULT_STYLE = "https://tiles.openfreemap.org/styles/liberty"

/**
 * KMap — composant de carte interactif pour Kindling.
 *
 * Miroir Compose de mapcn/map pour Android (et iOS, Desktop via maplibre-compose).
 *
 * @param modifier Modifier standard Compose
 * @param state État de la caméra ([rememberKMapState])
 * @param markers Liste de marqueurs à afficher ([KMapMarker])
 * @param routes Liste de tracés/routes à afficher ([KMapRoute])
 * @param styleUrl URL du style MapLibre (par défaut : OpenFreeMap Liberty)
 * @param scrollGesturesEnabled Active le pan tactile
 * @param zoomGesturesEnabled Active le zoom tactile
 * @param rotationGesturesEnabled Active la rotation
 * @param onMapClick Callback lors d'un clic sur la carte (lat, lng)
 * @param onMarkerClick Callback lors d'un clic sur un marqueur
 * @param popupContent Contenu Composable custom pour le popup d'un marqueur sélectionné
 */
@Composable
fun KMap(
    modifier: Modifier = Modifier,
    state: KMapState = rememberKMapState(),
    markers: List<KMapMarker> = emptyList(),
    routes: List<KMapRoute> = emptyList(),
    styleUrl: String = DEFAULT_STYLE,
    scrollGesturesEnabled: Boolean = true,
    zoomGesturesEnabled: Boolean = true,
    rotationGesturesEnabled: Boolean = true,
    onMapClick: ((latitude: Double, longitude: Double) -> Unit)? = null,
    onMarkerClick: ((KMapMarker) -> Unit)? = null,
    popupContent: (@Composable (KMapMarker) -> Unit)? = null,
) {
    var selectedMarker by remember { mutableStateOf<KMapMarker?>(null) }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = dev.sargunv.maplibrecompose.core.LatLng(state.latitude, state.longitude),
            zoom = state.zoom,
        )
    )

    Box(modifier = modifier) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            styleUri = styleUrl,
            cameraState = cameraState,
            isScrollGesturesEnabled = scrollGesturesEnabled,
            isZoomGesturesEnabled = zoomGesturesEnabled,
            isRotateGesturesEnabled = rotationGesturesEnabled,
            onMapClick = { latLng ->
                selectedMarker = null
                onMapClick?.invoke(latLng.latitude, latLng.longitude)
            },
        ) {
            // ── Routes ──────────────────────────────────────────────
            routes.forEach { route ->
                val geojson = buildRouteGeoJson(route)
                val source = rememberGeoJsonSource(GeoJsonData.JsonString(geojson))
                LineLayer(
                    id = "k-route-${route.id}",
                    source = source,
                    color = const(route.color),
                    width = const(route.widthDp),
                )
            }

            // ── Markers ─────────────────────────────────────────────
            if (markers.isNotEmpty()) {
                val markersGeoJson = buildMarkersGeoJson(markers)
                val markersSource = rememberGeoJsonSource(
                    GeoJsonData.JsonString(markersGeoJson)
                )

                CircleLayer(
                    id = "k-markers-shadow",
                    source = markersSource,
                    color = const(Color.Black.copy(alpha = 0.15f)),
                    radius = const(10f),
                    blur = const(1f),
                    translate = const(androidx.compose.ui.unit.DpOffset(0f.dp, 2f.dp)),
                )

                CircleLayer(
                    id = "k-markers",
                    source = markersSource,
                    color = const(Color(0xFFEF4444)), // override per-feature via expression si besoin
                    radius = const(8f),
                    strokeColor = const(Color.White),
                    strokeWidth = const(2f),
                    onClick = { features ->
                        val clickedId = features.firstOrNull()
                            ?.properties
                            ?.get("id")
                            ?.toString()
                            ?.removeSurrounding("\"")
                        val marker = markers.find { it.id == clickedId }
                        if (marker != null) {
                            selectedMarker = if (selectedMarker?.id == marker.id) null else marker
                            onMarkerClick?.invoke(marker)
                        }
                        dev.sargunv.maplibrecompose.compose.layer.ClickResult.Consume
                    },
                )
            }
        }

        // ── Popup au-dessus du marqueur sélectionné ─────────────
        selectedMarker?.let { marker ->
            Box(
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                if (popupContent != null) {
                    popupContent(marker)
                } else {
                    KMapPopup(
                        title = marker.title,
                        snippet = marker.snippet,
                    )
                }
            }
        }
    }
}

// ── Helpers GeoJSON ────────────────────────────────────────────────────────────

private fun buildMarkersGeoJson(markers: List<KMapMarker>): String {
    val features = markers.joinToString(",") { marker ->
        """
        {
          "type": "Feature",
          "properties": { "id": "${marker.id}", "title": ${marker.title?.let { "\"$it\"" } ?: "null"} },
          "geometry": { "type": "Point", "coordinates": [${marker.longitude}, ${marker.latitude}] }
        }
        """.trimIndent()
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}

private fun buildRouteGeoJson(route: KMapRoute): String {
    val coords = route.coordinates.joinToString(",") { (lat, lng) -> "[$lng,$lat]" }
    return """
    {
      "type": "FeatureCollection",
      "features": [{
        "type": "Feature",
        "properties": {},
        "geometry": { "type": "LineString", "coordinates": [$coords] }
      }]
    }
    """.trimIndent()
}