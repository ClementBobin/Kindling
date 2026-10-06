package dev.kindling.core.components.ui.gesture

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

/**
 * Pull-to-refresh behavior that works from **any edge** and draws **nothing by itself**:
 * the loading UI is whatever you put in [indicator].
 *
 * The gesture is detected through nested scrolling, so [content] must contain a scrollable
 * along the pull axis (`LazyColumn`, `Column(Modifier.verticalScroll(…))`, `LazyRow`, pager…).
 * The pull only starts once that scrollable reached its bound on the chosen [edge].
 *
 * ```kotlin
 * var refreshing by remember { mutableStateOf(false) }
 * val scope = rememberCoroutineScope()
 *
 * KEdgeRefresh(
 *     isRefreshing = refreshing,
 *     onRefresh = {
 *         refreshing = true
 *         scope.launch { reload(); refreshing = false }
 *     },
 *     edge = KRefreshEdge.Top,
 *     indicator = { state ->
 *         // Your own UI. state.progress goes 0f → 1f (threshold) → 2f (max pull).
 *         KSpinner(modifier = Modifier.padding(16.dp).alpha(state.progress.coerceIn(0f, 1f)))
 *     }
 * ) {
 *     LazyColumn { items(rows) { RowItem(it) } }
 * }
 * ```
 *
 * @param isRefreshing Whether a refresh is running. Set it to `true` **inside** [onRefresh];
 *   the indicator stays visible until you set it back to `false`. If it is not set to `true`
 *   within ~250 ms of the release, the indicator retracts on its own.
 * @param onRefresh Called once when the user releases after passing [threshold].
 * @param edge Edge to pull from. [KRefreshEdge.Start]/[KRefreshEdge.End] respect RTL.
 * @param enabled Disables the gesture when `false`.
 * @param threshold Pull distance required to trigger a refresh.
 * @param dragMultiplier Finger-to-indicator ratio (0.5f = the indicator moves half as far as the finger).
 * @param moveContent When `true`, the content slides along with the indicator.
 * @param state Hoist it to read the pull progress from outside.
 * @param indicator Your loading UI. It sits on the chosen edge and slides in from outside
 *   the bounds as the user pulls.
 * @param content The refreshable content.
 */
@Composable
fun KEdgeRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    edge: KRefreshEdge = KRefreshEdge.Top,
    enabled: Boolean = true,
    threshold: Dp = 80.dp,
    dragMultiplier: Float = 0.5f,
    moveContent: Boolean = true,
    state: KRefreshState = rememberKRefreshState(),
    indicator: @Composable (state: KRefreshState) -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val side = edge.resolve(LocalLayoutDirection.current)
    val thresholdPx = with(density) { threshold.toPx() }

    val currentOnRefresh by rememberUpdatedState(onRefresh)
    val currentEnabled by rememberUpdatedState(enabled)
    val currentMultiplier by rememberUpdatedState(dragMultiplier)

    SideEffect { state.thresholdPx = thresholdPx }

    // Keeps the indicator parked at the threshold while refreshing, and sends it back afterwards.
    LaunchedEffect(isRefreshing, thresholdPx) {
        state.isRefreshing = isRefreshing
        state.animateTo(if (isRefreshing) thresholdPx else 0f)
    }

    val connection = remember(state, side) {
        KEdgeRefreshConnection(
            state = state,
            side = side,
            enabled = { currentEnabled },
            dragMultiplier = { currentMultiplier },
            onRefresh = { currentOnRefresh() }
        )
    }

    var indicatorSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .clipToBounds()
            .nestedScroll(connection)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (moveContent) {
                        val t = side.toOffset(state.distance)
                        translationX = t.x
                        translationY = t.y
                    }
                }
        ) {
            content()
        }

        Box(
            modifier = Modifier
                .align(side.alignment)
                .onSizeChanged { indicatorSize = it }
                .graphicsLayer {
                    val extent = if (side.orientation == Orientation.Vertical) {
                        indicatorSize.height
                    } else {
                        indicatorSize.width
                    }
                    val t = side.toOffset(state.distance - extent)
                    translationX = t.x
                    translationY = t.y
                }
        ) {
            indicator(state)
        }
    }
}

// ── Internals ────────────────────────────────────────────────────────────────

private const val MAX_PULL_FACTOR = 2f
private const val MIN_MULTIPLIER = 0.05f

/** Physical side (RTL already resolved). */
private enum class Side(val orientation: Orientation, val alignment: Alignment) {
    Top(Orientation.Vertical, BiasAbsoluteAlignment(0f, -1f)),
    Bottom(Orientation.Vertical, BiasAbsoluteAlignment(0f, 1f)),
    Left(Orientation.Horizontal, BiasAbsoluteAlignment(-1f, 0f)),
    Right(Orientation.Horizontal, BiasAbsoluteAlignment(1f, 0f));

    /** Component of [o] that moves the content away from this edge (positive = pulling). */
    fun pullOf(o: Offset): Float = when (this) {
        Top -> o.y
        Bottom -> -o.y
        Left -> o.x
        Right -> -o.x
    }

    /** Converts a pull amount back to a screen-space offset. */
    fun toOffset(pull: Float): Offset = when (this) {
        Top -> Offset(0f, pull)
        Bottom -> Offset(0f, -pull)
        Left -> Offset(pull, 0f)
        Right -> Offset(-pull, 0f)
    }

    /** Keeps only the velocity along the pull axis. */
    fun axisOf(v: Velocity): Velocity =
        if (orientation == Orientation.Vertical) Velocity(0f, v.y) else Velocity(v.x, 0f)
}

private fun KRefreshEdge.resolve(direction: LayoutDirection): Side = when (this) {
    KRefreshEdge.Top -> Side.Top
    KRefreshEdge.Bottom -> Side.Bottom
    KRefreshEdge.Start -> if (direction == LayoutDirection.Ltr) Side.Left else Side.Right
    KRefreshEdge.End -> if (direction == LayoutDirection.Ltr) Side.Right else Side.Left
}

private class KEdgeRefreshConnection(
    private val state: KRefreshState,
    private val side: Side,
    private val enabled: () -> Boolean,
    private val dragMultiplier: () -> Float,
    private val onRefresh: () -> Unit,
) : NestedScrollConnection {

    private val isActive: Boolean get() = enabled() && !state.isRefreshing

    /** The user drags back toward the content while the indicator is out: retract it first. */
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!isActive || source != NestedScrollSource.UserInput) return Offset.Zero
        val pull = side.pullOf(available)
        return if (pull < 0f && state.distance > 0f) consume(pull) else Offset.Zero
    }

    /** The child hit its bound and left some drag over: turn it into a pull. */
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (!isActive || source != NestedScrollSource.UserInput) return Offset.Zero
        val pull = side.pullOf(available)
        return if (pull > 0f) consume(pull) else Offset.Zero
    }

    /** Release: refresh if the threshold was passed, otherwise spring back. */
    override suspend fun onPreFling(available: Velocity): Velocity {
        if (!isActive || state.distance <= 0f) return Velocity.Zero

        if (state.isThresholdReached) {
            onRefresh()
            state.holdForRefresh()
        } else {
            state.animateTo(0f)
        }
        return side.axisOf(available)
    }

    private fun consume(pull: Float): Offset {
        val multiplier = dragMultiplier().coerceAtLeast(MIN_MULTIPLIER)
        val newDistance = (state.distance + pull * multiplier)
            .coerceIn(0f, state.thresholdPx * MAX_PULL_FACTOR)
        val consumedPull = (newDistance - state.distance) / multiplier
        state.dragTo(newDistance)
        return side.toOffset(consumedPull)
    }
}
