package dev.kindling.core.components.ui.maps.blocks.uptimemonitor

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.layer.ClickResult
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.GeoJsonData
import dev.sargunv.maplibrecompose.core.LatLng
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.feature
import dev.sargunv.maplibrecompose.expressions.dsl.match
import dev.sargunv.maplibrecompose.expressions.value.CirclePitchAlignment

// ── Map style URLs (light / dark) ─────────────────────────────────────────────
private const val STYLE_LIGHT = "https://tiles.openfreemap.org/styles/liberty"
private const val STYLE_DARK  = "https://tiles.openfreemap.org/styles/dark"

/**
 * KUptimeMonitorBlock
 *
 * Status-page style map of edge-network nodes showing live health, latency,
 * and uptime. Mirrors the mapcn `uptime-monitor` block.
 *
 * @param nodes         List of edge nodes to display. Defaults to [kSampleEdgeNodes].
 * @param modifier      Modifier applied to the root scaffold.
 * @param onNodeClick   Optional callback invoked when a node is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KUptimeMonitorBlock(
    nodes: List<KEdgeNode> = kSampleEdgeNodes,
    modifier: Modifier = Modifier,
    onNodeClick: ((KEdgeNode) -> Unit)? = null,
) {
    var selectedNode by remember { mutableStateOf<KEdgeNode?>(null) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val isDark = isSystemInDarkTheme()

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = LatLng(20.0, 0.0),
            zoom = 1.5,
        )
    )

    // Build GeoJSON — one feature per node, status encoded as property
    val geojson = remember(nodes) { buildUptimeGeoJson(nodes) }
    val statusValues = remember(nodes) {
        nodes.associate { it.id to it.status.name }
    }

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetPeekHeight = 260.dp,
        sheetContent = {
            // ── Bottom sheet: summary + node list ────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // Header
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Edge Network",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${nodes.size} locations worldwide",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                KUptimeSummaryBar(
                    nodes = nodes,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )

                LazyColumn {
                    items(nodes, key = { it.id }) { node ->
                        KUptimeNodeRow(
                            node = node,
                            isSelected = selectedNode?.id == node.id,
                            onClick = {
                                selectedNode = if (selectedNode?.id == node.id) null else node
                                onNodeClick?.invoke(node)
                                cameraState.animateTo(
                                    CameraPosition(
                                        target = LatLng(node.latitude, node.longitude),
                                        zoom = 4.0,
                                    )
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        // ── Map ───────────────────────────────────────────────────────────────
        Box(modifier = Modifier.padding(innerPadding)) {
            val source = rememberGeoJsonSource(GeoJsonData.JsonString(geojson))

            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                styleUri = if (isDark) STYLE_DARK else STYLE_LIGHT,
                cameraState = cameraState,
                onMapClick = { selectedNode = null },
            ) {
                // ── Glow / halo ring ─────────────────────────────────────
                CircleLayer(
                    id = "uptime-glow",
                    source = source,
                    radius = const(18f),
                    color = colorByStatus(KEdgeStatus.Healthy),
                    opacity = const(0.18f),
                    pitchAlignment = const(CirclePitchAlignment.Map),
                )

                // ── Main dot ─────────────────────────────────────────────
                CircleLayer(
                    id = "uptime-dots",
                    source = source,
                    radius = const(9f),
                    // Color driven by "status" feature property via match expression
                    color = match(
                        input  = feature.get("status"),
                        default = const(KEdgeStatus.Healthy.color()),
                        "Healthy"  to const(KEdgeStatus.Healthy.color()),
                        "Degraded" to const(KEdgeStatus.Degraded.color()),
                        "Down"     to const(KEdgeStatus.Down.color()),
                    ),
                    strokeColor = const(androidx.compose.ui.graphics.Color.White),
                    strokeWidth = const(2f),
                    pitchAlignment = const(CirclePitchAlignment.Map),
                    onClick = { features ->
                        val clickedId = features.firstOrNull()
                            ?.properties
                            ?.get("id")
                            ?.toString()
                            ?.removeSurrounding("\"")
                        val node = nodes.find { it.id == clickedId }
                        if (node != null) {
                            selectedNode = if (selectedNode?.id == node.id) null else node
                            onNodeClick?.invoke(node)
                        }
                        ClickResult.Consume
                    },
                )
            }

            // ── Floating popup above selected node ────────────────────────
            selectedNode?.let { node ->
                KUptimePopup(
                    node = node,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                )
            }
        }
    }
}

// ── GeoJSON builder ────────────────────────────────────────────────────────────

private fun buildUptimeGeoJson(nodes: List<KEdgeNode>): String {
    val features = nodes.joinToString(",") { node ->
        """
        {
          "type": "Feature",
          "properties": {
            "id": "${node.id}",
            "city": "${node.city}",
            "status": "${node.status.name}",
            "latencyMs": ${node.latencyMs},
            "uptimePercent": ${node.uptimePercent},
            "requestsPerSec": ${node.requestsPerSec}
          },
          "geometry": {
            "type": "Point",
            "coordinates": [${node.longitude}, ${node.latitude}]
          }
        }
        """.trimIndent()
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}

// Workaround helper — returns a const color expression for a given status.
// The real per-feature coloring is done via the match expression in the layer.
@Composable
private fun colorByStatus(status: KEdgeStatus) = const(status.color())