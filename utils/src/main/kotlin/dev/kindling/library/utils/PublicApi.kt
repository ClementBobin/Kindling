package dev.kindling.utils

import dev.kindling.utils.Debouncer
import dev.kindling.utils.Throttler
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Public API entrypoint package for Kindling utils module.
 */
object KindlingUtilsApi

// ── Type aliases ─────────────────────────────────────────────────────────────

typealias KDebouncer<T> = Debouncer<T>
typealias KThrottler<T> = Throttler<T>

// ── Factory helpers (mirrors the internal top-level functions) ────────────────

/**
 * Creates a [Debouncer] and immediately registers a suspending callback.
 *
 * ```kotlin
 * private val searchDebouncer = KDebounce(viewModelScope, 300.milliseconds) { q -> search(q) }
 * // in UI: onValueChange = { searchDebouncer.emit(it) }
 * ```
 */
fun <T> KDebounce(
    scope: CoroutineScope,
    delay: Duration = 300.milliseconds,
    leading: Boolean = false,
    block: suspend (T) -> Unit
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
    block: suspend (T) -> Unit
): KThrottler<T> = throttle(scope, period, block)

// ── Flow extension re-exports ─────────────────────────────────────────────────

/**
 * Leading-edge debounce on any [Flow].
 *
 * Emits the first item in a burst immediately, then suppresses further items
 * until [duration] has elapsed with no new items.
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
