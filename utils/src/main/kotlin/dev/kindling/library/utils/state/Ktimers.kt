package dev.kindling.library.utils.state

import kotlinx.coroutines.*
import kotlin.time.Duration

// ─────────────────────────────────────────────────────────────────────────────
//  KTimer  (port of useTimeout)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A cancellable one-shot timer that fires [block] after [delay].
 *
 * Port of the `useTimeout` React hook.
 *
 * ```kotlin
 * val timer = KTimer(scope = viewModelScope, delay = 2.seconds) {
 *     println("fired!")
 * }
 * // Runs automatically. To restart:
 * timer.reset()
 * // To stop:
 * timer.clear()
 * ```
 *
 * @param scope  [CoroutineScope] that owns the timer job.
 * @param delay  How long to wait before firing [block]. Pass `null` to start paused.
 * @param block  Suspending action called once when the timer fires.
 */
class KTimer(
    private val scope: CoroutineScope,
    var delay: Duration?,
    private val block: suspend () -> Unit,
) {
    private var job: Job? = null

    init { if (delay != null) set() }

    /** Starts (or restarts) the timer using its current [delay]. */
    fun set() {
        val d = delay ?: return
        start(d)
    }

    /** Starts (or restarts) the timer with a new [delay]. */
    fun set(delay: Duration) {
        this.delay = delay
        start(delay)
    }

    private fun start(d: Duration) {
        job?.cancel()
        job = scope.launch {
            kotlinx.coroutines.delay(d)
            block()
        }
    }

    /** Cancels the pending timer without firing [block]. */
    fun clear() {
        job?.cancel()
        job = null
    }

    /** Cancels the current timer and starts a fresh one. */
    fun reset() { clear(); set() }
}

// ─────────────────────────────────────────────────────────────────────────────
//  KInterval  (port of useInterval)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A repeating interval that invokes [block] every [period].
 *
 * Port of the `useInterval` React hook.
 *
 * ```kotlin
 * val interval = KInterval(scope = viewModelScope, period = 1.seconds) {
 *     println("tick")
 * }
 * // Stops:
 * interval.clear()
 * ```
 *
 * Pass `null` for [period] to create the interval in a paused state (call [set] later).
 *
 * @param scope   [CoroutineScope] that owns the interval job.
 * @param period  Time between invocations. Pass `null` to start paused.
 * @param block   Suspending action called on each tick.
 */
class KInterval(
    private val scope: CoroutineScope,
    var period: Duration?,
    private val block: suspend () -> Unit,
) {
    private var job: Job? = null

    init { if (period != null) set() }

    /** Starts the interval using its current [period]. */
    fun set() {
        val p = period ?: return
        start(p)
    }

    /** Starts the interval with a new [period]. */
    fun set(period: Duration) {
        this.period = period
        start(period)
    }

    private fun start(p: Duration) {
        require(p > Duration.ZERO) { "period must be positive" }
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(p)
                block()
            }
        }
    }

    /** Stops the interval. */
    fun clear() {
        job?.cancel()
        job = null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Convenience top-level factories
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Creates a [KTimer] that fires [block] after [delay].
 *
 * ```kotlin
 * val timer = timer(viewModelScope, 500.milliseconds) { dismiss() }
 * timer.reset() // restart
 * ```
 */
fun timer(
    scope: CoroutineScope,
    delay: Duration,
    block: suspend () -> Unit,
): KTimer = KTimer(scope, delay, block)

/**
 * Creates a [KInterval] that fires [block] every [period].
 *
 * ```kotlin
 * val clock = interval(viewModelScope, 1.seconds) { tick() }
 * clock.clear() // stop
 * ```
 */
fun interval(
    scope: CoroutineScope,
    period: Duration,
    block: suspend () -> Unit,
): KInterval = KInterval(scope, period, block)