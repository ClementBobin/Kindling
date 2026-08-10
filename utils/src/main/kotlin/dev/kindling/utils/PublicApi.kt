package dev.kindling.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Public API entrypoint for the Kindling utils module.
 *
 * ## Available utilities
 *
 * ### Async / timing
 * - [Debouncer] / [KDebounce] — trailing-edge (or leading-edge) debounce
 * - [Throttler] / [KThrottle] — leading-edge throttle
 * - [KTimer]    / [timer]     — one-shot cancellable timer  (port of `useTimeout`)
 * - [KInterval] / [interval]  — repeating interval          (port of `useInterval`)
 * - [RetryRunner]             — async retry with exponential back-off (port of `useRetry`)
 *
 * ### State
 * - [Counter]          — bounded integer counter with step   (port of `useCounter`)
 * - [Toggle]           — boolean toggle                      (port of `useToggle`)
 * - [StateWithHistory] — value + undo/redo history           (port of `useStateWithHistory` / `useUndoRedo`)
 *
 * ### Collections
 * - [KQueue] — reactive FIFO queue  (port of `useQueue`)
 * - [KMap]   — reactive key-value map (port of `useMap`)
 *
 * ### Flow extensions
 * - [Flow.kDebounceLeading] — leading-edge debounce on any Flow
 * - [Flow.kThrottleFirst]  — throttle to one emission per window
 */
object KindlingUtilsApi

// ── Type aliases ─────────────────────────────────────────────────────────────

typealias KDebouncer<T>  = Debouncer<T>
typealias KThrottler<T>  = Throttler<T>
typealias KCounter       = Counter
typealias KToggle        = Toggle
typealias KRetryRunner<T> = RetryRunner<T>

// ── Debounce / Throttle factories ────────────────────────────────────────────

/**
 * Creates a [Debouncer] and immediately registers a suspending callback.
 *
 * ```kotlin
 * private val searchDebouncer = KDebounce(viewModelScope, 300.milliseconds) { q -> search(q) }
 * onValueChange = { searchDebouncer.emit(it) }
 * ```
 */
fun <T> KDebounce(
    scope: CoroutineScope,
    delay: Duration = 300.milliseconds,
    leading: Boolean = false,
    block: suspend (T) -> Unit,
): KDebouncer<T> = debounce(scope, delay, leading, block)

/**
 * Creates a [Throttler] and immediately registers a suspending callback.
 *
 * ```kotlin
 * private val clickThrottler = KThrottle(viewModelScope, 500.milliseconds) { handleClick() }
 * ```
 */
fun <T> KThrottle(
    scope: CoroutineScope,
    period: Duration = 500.milliseconds,
    block: suspend (T) -> Unit,
): KThrottler<T> = throttle(scope, period, block)

// ── Timer / Interval factories ───────────────────────────────────────────────

/**
 * Creates a [KTimer] that fires [block] once after [delay].
 *
 * ```kotlin
 * val dismiss = KTimer(viewModelScope, 3.seconds) { hideToast() }
 * dismiss.reset() // restart
 * ```
 */
fun KTimer(
    scope: CoroutineScope,
    delay: Duration,
    block: suspend () -> Unit,
): KTimer = dev.kindling.utils.KTimer(scope, delay, block)

/**
 * Creates a [KInterval] that fires [block] every [period].
 *
 * ```kotlin
 * val clock = KInterval(viewModelScope, 1.seconds) { tick() }
 * clock.clear()
 * ```
 */
fun KInterval(
    scope: CoroutineScope,
    period: Duration,
    block: suspend () -> Unit,
): KInterval = dev.kindling.utils.KInterval(scope, period, block)

// ── Flow extension re-exports ─────────────────────────────────────────────────

/**
 * Leading-edge debounce on any [Flow].
 *
 * ```kotlin
 * searchFlow.kDebounceLeading(300.milliseconds).collect { query -> search(query) }
 * ```
 */
fun <T> Flow<T>.kDebounceLeading(duration: Duration): Flow<T> = debounceLeading(duration)

/**
 * Throttles a [Flow] to emit at most one item per [period] (leading edge).
 *
 * ```kotlin
 * clickFlow.kThrottleFirst(500.milliseconds).collect { handleClick() }
 * ```
 */
fun <T> Flow<T>.kThrottleFirst(period: Duration): Flow<T> = throttleFirst(period)