package dev.kindling.core.components.ui.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

// ─── Data Models ─────────────────────────────────────────────────────────────

/**
 * Represents the current runtime positional state of a dashboard item node.
 *
 * @property id Unique identifier for the widget instance.
 * @property column Current grid column coordinate.
 * @property row Current grid row coordinate.
 * @property widthCells Number of horizontal grid units spanned by the widget.
 * @property heightCells Number of vertical grid units spanned by the widget.
 */
data class KDashboardItemState(
    val id: String,
    val column: Int,
    val row: Int,
    val widthCells: Int = 1,
    val heightCells: Int = 1
)

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * A responsive dashboard grid layout engine supporting item dragging, action buttons,
 * dynamic cell sizing, and persistent position callbacks.
 *
 * @param cellSize The dimension size of a base single cell grid unit (default is 80.dp).
 * @param modifier Layout modifiers applied to the outer host container.
 * @param onItemMoved Callback invoked when a draggable widget settles into its final coordinates.
 * @param content Declarative DSL scope block for adding grid components and action tiles.
 */
@Composable
fun KDashboardGrid(
    cellSize: Dp = 80.dp,
    modifier: Modifier = Modifier,
    onItemMoved: ((id: String, column: Int, row: Int) -> Unit)? = null,
    content: @Composable KDashboardGridScope.() -> Unit
) {
    val scope = remember(cellSize) { KDashboardGridScopeImpl(cellSize) }
    scope.content()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val availableWidth = maxWidth
        val computedColumns = max(1, (availableWidth / cellSize).toInt())

        val totalRows = scope.items.maxOfOrNull { it.initialRow + it.heightCells } ?: 1
        val gridHeight = cellSize * totalRows

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight)
        ) {
            scope.items.forEach { item ->
                KDashboardCellContainer(
                    item = item,
                    cellSize = cellSize,
                    maxColumns = computedColumns,
                    availableWidth = availableWidth,
                    onItemMoved = onItemMoved
                )
            }
        }
    }
}

/**
 * DSL scope interface for structuring items inside a [KDashboardGrid].
 */
interface KDashboardGridScope {
    /**
     * Adds a standard, user-draggable widget node to the dashboard grid.
     *
     * @param id Unique identifier string for the widget view instance.
     * @param initialColumn Starting horizontal cell column index.
     * @param initialRow Starting vertical cell row index.
     * @param widthCells Total horizontal cell slots occupied.
     * @param heightCells Total vertical cell slots occupied.
     * @param content Composable slot lambda rendering the internal widget content.
     */
    fun item(
        id: String,
        initialColumn: Int,
        initialRow: Int,
        widthCells: Int = 1,
        heightCells: Int = 1,
        content: @Composable () -> Unit
    )

    /**
     * Adds a static or click-interactive action cell that cannot be dragged.
     *
     * @param id Unique identifier string for the action tile.
     * @param initialColumn Starting horizontal cell column index.
     * @param initialRow Starting vertical cell row index.
     * @param widthCells Total horizontal cell slots occupied.
     * @param heightCells Total vertical cell slots occupied.
     * @param onClick Action callback triggered upon clicking the container surface.
     * @param content Composable slot lambda rendering the internal action content.
     */
    fun actionItem(
        id: String,
        initialColumn: Int,
        initialRow: Int,
        widthCells: Int = 1,
        heightCells: Int = 1,
        onClick: () -> Unit,
        content: @Composable () -> Unit
    )
}

internal class KDashboardGridScopeImpl(
    val cellSize: Dp
) : KDashboardGridScope {
    val items = mutableStateListOf<DashboardGridItemData>()

    override fun item(
        id: String,
        initialColumn: Int,
        initialRow: Int,
        widthCells: Int,
        heightCells: Int,
        content: @Composable () -> Unit
    ) {
        items.add(
            DashboardGridItemData(
                id = id,
                initialColumn = initialColumn,
                initialRow = initialRow,
                widthCells = widthCells,
                heightCells = heightCells,
                isDraggable = true,
                onClick = null,
                content = content
            )
        )
    }

    override fun actionItem(
        id: String,
        initialColumn: Int,
        initialRow: Int,
        widthCells: Int,
        heightCells: Int,
        onClick: () -> Unit,
        content: @Composable () -> Unit
    ) {
        items.add(
            DashboardGridItemData(
                id = id,
                initialColumn = initialColumn,
                initialRow = initialRow,
                widthCells = widthCells,
                heightCells = heightCells,
                isDraggable = false,
                onClick = onClick,
                content = content
            )
        )
    }
}

internal data class DashboardGridItemData(
    val id: String,
    val initialColumn: Int,
    val initialRow: Int,
    val widthCells: Int,
    val heightCells: Int,
    val isDraggable: Boolean,
    val onClick: (() -> Unit)?,
    val content: @Composable () -> Unit
)

@Composable
internal fun KDashboardCellContainer(
    item: DashboardGridItemData,
    cellSize: Dp,
    maxColumns: Int,
    availableWidth: Dp,
    onItemMoved: ((id: String, column: Int, row: Int) -> Unit)?
) {
    val coroutineScope = rememberCoroutineScope()
    var currentColumn by remember(item.initialColumn) { mutableStateOf(item.initialColumn) }
    var currentRow by remember(item.initialRow) { mutableStateOf(item.initialRow) }

    val offsetX = remember(item.initialColumn) { Animatable(item.initialColumn * cellSize.value) }
    val offsetY = remember(item.initialRow) { Animatable(item.initialRow * cellSize.value) }

    LaunchedEffect(item.initialColumn, item.initialRow) {
        if (currentColumn != item.initialColumn || currentRow != item.initialRow) {
            currentColumn = item.initialColumn
            currentRow = item.initialRow
            offsetX.animateTo(item.initialColumn * cellSize.value, spring(dampingRatio = 0.8f, stiffness = 400f))
            offsetY.animateTo(item.initialRow * cellSize.value, spring(dampingRatio = 0.8f, stiffness = 400f))
        }
    }

    val effectiveWidth = if (item.widthCells >= maxColumns) availableWidth else cellSize * item.widthCells
    val itemHeight = cellSize * item.heightCells

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = offsetX.value.roundToInt(),
                    y = offsetY.value.roundToInt()
                )
            }
            .size(width = effectiveWidth, height = itemHeight)
            .then(
                if (item.isDraggable) {
                    Modifier.pointerInput(maxColumns) {
                        detectDragGestures(
                            onDragEnd = {
                                val targetCol = (offsetX.value / cellSize.value).roundToInt()
                                    .coerceIn(0, max(0, maxColumns - item.widthCells))
                                val targetRow = (offsetY.value / cellSize.value).roundToInt()
                                    .coerceAtLeast(0)

                                currentColumn = targetCol
                                currentRow = targetRow

                                onItemMoved?.invoke(item.id, targetCol, targetRow)

                                coroutineScope.launch {
                                    offsetX.animateTo(targetCol * cellSize.value, spring(dampingRatio = 0.8f, stiffness = 400f))
                                    offsetY.animateTo(targetRow * cellSize.value, spring(dampingRatio = 0.8f, stiffness = 400f))
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y)
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
            .padding(4.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (item.onClick != null) {
                        Modifier.clickable { item.onClick.invoke() }
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            color = if (item.isDraggable) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            tonalElevation = if (item.isDraggable) 4.dp else 1.dp
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                item.content()
            }
        }
    }
}