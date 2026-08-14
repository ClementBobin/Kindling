package dev.kindling.core.components.ui.maps.blocks.logisticsnetwork

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.layer.ClickResult
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.GeoJsonData
import dev.sargunv.maplibrecompose.core.LatLng
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.feature
import dev.sargunv.maplibrecompose.expressions.dsl.match
import dev.sargunv.maplibrecompose.expressions.value.LineCap
import dev.sargunv.maplibrecompose.expressions.value.LineJoin

// ── Style ─────────────────────────────────────────────────────────────────────

private const val STYLE_LIGHT = "https://tiles.openfreemap.org/styles/positron"
private const val STYLE_DARK  = "https://tiles.openfreemap.org/styles/dark"

/**
 * KLogisticsNetworkBlock
 *
 * Domestic logistics map showing warehouses, distribution centres, and stores
 * connected by routes, with a left-hand filter sidebar and stats panel.
 *
 * Mirrors the mapcn `logistics-network` block.
 *
 * @param nodes           All logistics nodes. Default: [kSampleLogisticsNodes].
 * @param routes          All routes between nodes. Default: [kSampleLogisticsRoutes].
 * @param modifier        Applied to the root [Row].
 * @param onNodeClick     Optional callback when a node is selected.
 */
@Composable
fun KLogisticsNetworkBlock(
    nodes: List<KLogisticsNode>   = kSampleLogisticsNodes,
    routes: List<KLogisticsRoute> = kSampleLogisticsRoutes,
    modifier: Modifier = Modifier,
    onNodeClick: ((KLogisticsNode) -> Unit)? = null,
) {
    // ── Filter state ──────────────────────────────────────────────────────
    var activeNodeTypes by rememberSaveable {
        mutableStateOf(KLogisticsNodeType.entries.toSet())
    }
    var activeRouteStatuses by rememberSaveable {
        mutableStateOf(KLogisticsRouteStatus.entries.toSet())
    }
    var selectedNode by remember { mutableStateOf<KLogisticsNode?>(null) }

    val isDark = isSystemInDarkTheme()

    // ── Filtered data ─────────────────────────────────────────────────────
    val visibleNodes  = remember(nodes, activeNodeTypes) {
        nodes.filter { it.type in activeNodeTypes }
    }
    val visibleRoutes = remember(routes, activeRouteStatuses, visibleNodes) {
        val visibleIds = visibleNodes.map { it.id }.toSet()
        routes.filter {
            it.status in activeRouteStatuses &&
            it.fromId in visibleIds &&
            it.toId   in visibleIds
        }
    }

    // ── GeoJSON ───────────────────────────────────────────────────────────
    val nodesGeoJson  = remember(visibleNodes)  { buildNodesGeoJson(visibleNodes) }
    val routesGeoJson = remember(visibleRoutes, nodes) { buildRoutesGeoJson(visibleRoutes, nodes) }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = LatLng(39.5, -98.35),   // centre US
            zoom   = 3.2,
        )
    )

    Row(modifier = modifier.fillMaxSize()) {
        // ── Sidebar ───────────────────────────────────────────────────────
        KLogisticsFilterSidebar(
            nodes                = nodes,
            routes               = routes,
            activeNodeTypes      = activeNodeTypes,
            activeRouteStatuses  = activeRouteStatuses,
            onNodeTypeToggle     = { type ->
                activeNodeTypes = if (type in activeNodeTypes)
                    activeNodeTypes - type
                else
                    activeNodeTypes + type
            },
            onRouteStatusToggle  = { status ->
                activeRouteStatuses = if (status in activeRouteStatuses)
                    activeRouteStatuses - status
                else
                    activeRouteStatuses + status
            },
        )

        // ── Map ───────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            val nodeSource  = rememberGeoJsonSource(GeoJsonData.JsonString(nodesGeoJson))
            val routeSource = rememberGeoJsonSource(GeoJsonData.JsonString(routesGeoJson))

            MaplibreMap(
                modifier    = Modifier.fillMaxSize(),
                styleUri    = if (isDark) STYLE_DARK else STYLE_LIGHT,
                cameraState = cameraState,
                onMapClick  = { selectedNode = null },
            ) {
                // ── Routes ──────────────────────────────────────────────
                // Inactive routes — dashed appearance via low opacity
                LineLayer(
                    id     = "log-routes-inactive",
                    source = routeSource,
                    color  = match(
                        input   = feature.get("status"),
                        default = const(KLogisticsRouteStatus.Active.color()),
                        "Active"   to const(KLogisticsRouteStatus.Active.color()),
                        "Delayed"  to const(KLogisticsRouteStatus.Delayed.color()),
                        "Inactive" to const(KLogisticsRouteStatus.Inactive.color()),
                    ),
                    width  = const(2f),
                    opacity = match(
                        input   = feature.get("status"),
                        default = const(0.9f),
                        "Inactive" to const(0.35f),
                    ),
                    cap  = const(LineCap.Round),
                    join = const(LineJoin.Round),
                )

                // Animated pulse casing on active routes
                LineLayer(
                    id      = "log-routes-casing",
                    source  = routeSource,
                    color   = const(Color.White),
                    width   = const(4f),
                    opacity = const(0.25f),
                    cap     = const(LineCap.Round),
                    join    = const(LineJoin.Round),
                )

                // ── Node glow ────────────────────────────────────────────
                CircleLayer(
                    id      = "log-nodes-glow",
                    source  = nodeSource,
                    radius  = const(18f),
                    color   = match(
                        input   = feature.get("type"),
                        default = const(KLogisticsNodeType.Warehouse.color()),
                        "Warehouse"          to const(KLogisticsNodeType.Warehouse.color()),
                        "DistributionCenter" to const(KLogisticsNodeType.DistributionCenter.color()),
                        "Store"              to const(KLogisticsNodeType.Store.color()),
                    ),
                    opacity = const(0.15f),
                )

                // ── Node dots ────────────────────────────────────────────
                CircleLayer(
                    id          = "log-nodes",
                    source      = nodeSource,
                    radius      = match(
                        input   = feature.get("type"),
                        default = const(9f),
                        "Warehouse"          to const(12f),
                        "DistributionCenter" to const(10f),
                        "Store"              to const(7f),
                    ),
                    color       = match(
                        input   = feature.get("type"),
                        default = const(KLogisticsNodeType.Warehouse.color()),
                        "Warehouse"          to const(KLogisticsNodeType.Warehouse.color()),
                        "DistributionCenter" to const(KLogisticsNodeType.DistributionCenter.color()),
                        "Store"              to const(KLogisticsNodeType.Store.color()),
                    ),
                    strokeColor = const(Color.White),
                    strokeWidth = const(2f),
                    onClick     = { features ->
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

            // ── Node popup ────────────────────────────────────────────────
            selectedNode?.let { node ->
                KLogisticsNodePopup(
                    node     = node,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                )
            }
        }
    }
}

// ── GeoJSON builders ──────────────────────────────────────────────────────────

private fun buildNodesGeoJson(nodes: List<KLogisticsNode>): String {
    val features = nodes.joinToString(",") { n ->
        """
        {
          "type": "Feature",
          "properties": {
            "id":         "${n.id}",
            "name":       "${n.name}",
            "city":       "${n.city}",
            "type":       "${n.type.name}",
            "shipments":  ${n.shipments},
            "capacity":   ${n.capacity},
            "onTimeRate": ${n.onTimeRate}
          },
          "geometry": { "type": "Point", "coordinates": [${n.longitude}, ${n.latitude}] }
        }
        """.trimIndent()
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}

private fun buildRoutesGeoJson(
    routes: List<KLogisticsRoute>,
    nodes: List<KLogisticsNode>,
): String {
    val nodeMap = nodes.associateBy { it.id }
    val features = routes.mapNotNull { r ->
        val from = nodeMap[r.fromId] ?: return@mapNotNull null
        val to   = nodeMap[r.toId]   ?: return@mapNotNull null
        """
        {
          "type": "Feature",
          "properties": {
            "id":               "${r.id}",
            "status":           "${r.status.name}",
            "dailyShipments":   ${r.dailyShipments},
            "avgTransitHours":  ${r.avgTransitHours}
          },
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [${from.longitude}, ${from.latitude}],
              [${to.longitude},   ${to.latitude}]
            ]
          }
        }
        """.trimIndent()
    }.joinToString(",")
    return """{"type":"FeatureCollection","features":[$features]}"""
}