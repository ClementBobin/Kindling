package dev.kindling.core.components.ui.gesture

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Observable state of a [KEdgeRefresh]. It is handed to the `indicator` slot so the
 * loading UI can react to the gesture.
 *
 * Every property is Compose state: reading them inside `graphicsLayer { }` / `drawBehind { }`
 * avoids recomposing on each frame of the drag.
 */
@Stable
class KRefreshState internal constructor(private val scope: CoroutineScope) {

    /** Current pull distance in pixels, along the pull axis (always >= 0). */
    var distance: Float by mutableFloatStateOf(0f)
        private set

    /** Distance in pixels at which releasing triggers a refresh. */
    var thresholdPx: Float by mutableFloatStateOf(1f)
        internal set

    /** Mirrors the `isRefreshing` parameter of [KEdgeRefresh]. */
    var isRefreshing: Boolean by mutableStateOf(false)
        internal set

    /** Pull distance relative to the threshold: 0f = idle, 1f = release will refresh, up to 2f. */
    val progress: Float
        get() = if (thresholdPx > 0f) distance / thresholdPx else 0f

    /** `true` once the pull passed the threshold (releasing now triggers a refresh). */
    val isThresholdReached: Boolean
        get() = progress >= 1f

    /** `true` while the user is pulling (or the indicator is settling) and no refresh is running. */
    val isPulling: Boolean
        get() = distance > 0f && !isRefreshing

    private var animationJob: Job? = null
    private var graceJob: Job? = null

    internal fun dragTo(value: Float) {
        animationJob?.cancel()
        distance = value
    }

    /**
     * Parks the indicator at the threshold after a successful release. If the caller does not
     * flip `isRefreshing` to `true` shortly after (e.g. a no-op `onRefresh`), it retracts instead
     * of staying stuck.
     */
    internal fun holdForRefresh() {
        animateTo(thresholdPx)
        graceJob?.cancel()
        graceJob = scope.launch {
            delay(REFRESH_GRACE_MS)
            if (!isRefreshing) animateTo(0f)
        }
    }

    internal fun animateTo(target: Float) {
        animationJob?.cancel()
        animationJob = scope.launch {
            animate(
                initialValue = distance,
                targetValue = target,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) { value, _ -> distance = value }
        }
    }
}

/** Creates and remembers a [KRefreshState]. */
@Composable
fun rememberKRefreshState(): KRefreshState {
    val scope = rememberCoroutineScope()
    return remember(scope) { KRefreshState(scope) }
}

private const val REFRESH_GRACE_MS = 250L
