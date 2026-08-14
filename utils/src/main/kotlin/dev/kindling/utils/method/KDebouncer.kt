package dev.kindling.utils.method

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview

// ─────────────────────────────────────────────────────────────────────────────
//  Debouncer
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A coroutine-based debouncer that delays forwarding values until no new
 * values have arrived for the specified [delay] duration.
 *
 * This is typically used for search fields, auto-saves, or any input-triggered
 * actions that should only execute after the user has stopped typing.
 *
 * ### Basic usage (Flow-based)
 * ```kotlin
 * val debouncer = KDebouncer<String>(scope = viewModelScope, delay = 300.milliseconds)
 *
 * // Emit from a text-field's onValueChange:
 * onValueChange = { text -> debouncer.emit(text) }
 *
 * // Collect the stable values:
 * viewModelScope.launch {
 *     debouncer.flow.collect { stableText -> search(stableText) }
 * }
 * ```
 *
 * ### Callback-style usage
 * ```kotlin
 * val debouncer = KDebouncer<String>(scope = viewModelScope)
 * debouncer.onDebounced { query -> viewModel.search(query) }
 * 
 * // Then in UI:
 * onValueChange = { debouncer.emit(it) }
 * ```
 *
 * @param T The type of values being debounced.
 * @param scope The [CoroutineScope] in which the debouncing coroutine runs.
 * @param delay The quiet period duration before a value is forwarded. Default: 300ms.
 * @param leading If `true`, emit the first value immediately and suppress subsequent
 *                values until the quiet period elapses. Default: `false`.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class KDebouncer<T>(
    private val scope: CoroutineScope,
    val delay: Duration = 300.milliseconds,
    val leading: Boolean = false
) {
    private val _input = MutableSharedFlow<T>(extraBufferCapacity = 64, replay = 1)

    /** Debounced output — collect this to receive stable values. */
    val flow: Flow<T> = if (leading) {
        _input.debounceLeading(delay)
    } else {
        _input.debounce(delay.inWholeMilliseconds)
    }

    private var callbackJob: Job? = null

    /**
     * Sends a new value into the debouncer.
     *
     * Safe to call from any thread / coroutine context.
     */
    fun emit(value: T) {
        _input.tryEmit(value)
    }

    /**
     * Registers a suspending [block] that is called each time a debounced
     * value arrives.  Replaces any previously registered callback.
     *
     * ```kotlin
     * debouncer.onDebounced { query -> search(query) }
     * ```
     */
    fun onDebounced(block: suspend (T) -> Unit) {
        callbackJob?.cancel()
        _input.resetReplayCache()
        val supervisor = SupervisorJob(scope.coroutineContext[Job])
        callbackJob = supervisor
        (scope + supervisor).launch {
            flow.collect { block(it) }
        }
    }

    /** Cancels the internal callback job (if any). Does not cancel the scope. */
    fun cancel() {
        callbackJob?.cancel()
        callbackJob = null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Throttler
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A coroutine-based throttler that ensures at most one value is forwarded per [period].
 *
 * Throttling is useful for rate-limiting expensive operations like window resizing,
 * scrolling events, or "fast-click" button protection.
 *
 * Uses leading-edge semantics: the first value in each window is emitted
 * immediately; subsequent values within the same [period] are dropped.
 *
 * ### Example usage:
 * ```kotlin
 * val throttler = KThrottler<Unit>(scope = viewModelScope, period = 500.milliseconds)
 * 
 * throttler.onThrottled { 
 *     println("Button clicked - performing action") 
 * }
 * 
 * // In UI:
 * Button(onClick = { throttler.emit(Unit) }) { Text("Click Me") }
 * ```
 *
 * @param T The type of values being throttled.
 * @param scope The [CoroutineScope] in which the throttler runs.
 * @param period The minimum time interval between consecutive emissions. Default: 500ms.
 */
class KThrottler<T>(
    private val scope: CoroutineScope,
    val period: Duration = 500.milliseconds
) {
    private val _input = MutableSharedFlow<T>(extraBufferCapacity = 64, replay = 1)

    /** Throttled output flow. */
    val flow: Flow<T> = _input.throttleFirst(period)

    private var callbackJob: Job? = null

    fun emit(value: T) { _input.tryEmit(value) }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onThrottled(block: suspend (T) -> Unit) {
        callbackJob?.cancel()
        _input.resetReplayCache()
        val supervisor = SupervisorJob(scope.coroutineContext[Job])
        callbackJob = supervisor
        (scope + supervisor).launch {
            flow.collect { block(it) }
        }
    }

    fun cancel() { callbackJob?.cancel(); callbackJob = null }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Convenience top-level factories
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Creates and starts a [KDebouncer] with an immediate callback.
 *
 * This is a shorthand for creating a [KDebouncer] and calling [KDebouncer.onDebounced].
 *
 * ### Example usage:
 * ```kotlin
 * private val searchDebouncer = Kdebounce<String>(viewModelScope) { query ->
 *     performSearch(query)
 * }
 * 
 * // In UI:
 * onValueChange = { searchDebouncer.emit(it) }
 * ```
 */
fun <T> Kdebounce(
    scope: CoroutineScope,
    delay: Duration = 300.milliseconds,
    leading: Boolean = false,
    block: suspend (T) -> Unit
): KDebouncer<T> = KDebouncer<T>(scope, delay, leading).also { it.onDebounced(block) }

/**
 * Creates and starts a [KThrottler] with an immediate callback.
 *
 * This is a shorthand for creating a [KThrottler] and calling [KThrottler.onThrottled].
 *
 * ### Example usage:
 * ```kotlin
 * private val clickThrottler = Kthrottle<Unit>(viewModelScope, 500.milliseconds) {
 *     handleSubmit()
 * }
 * 
 * // In UI:
 * Button(onClick = { clickThrottler.emit(Unit) }) { ... }
 * ```
 */
fun <T> Kthrottle(
    scope: CoroutineScope,
    period: Duration = 500.milliseconds,
    block: suspend (T) -> Unit
): KThrottler<T> = KThrottler<T>(scope, period).also { it.onThrottled(block) }

/**
 * Leading-edge debounce for [Flow] values.
 *
 * @param duration The quiet period after which the next value may be emitted.
 * @return A new [Flow] that emits debounced values.
 */
fun <T> Flow<T>.debounceLeading(duration: Duration): Flow<T> = channelFlow {
    var timerJob: Job? = null

    collect { value ->
        if (timerJob?.isActive != true) {
            send(value)
            timerJob = launch {
                delay(duration)
            }
        }
    }
}

/**
 * Throttles a [Flow] to emit at most one item per [period].
 *
 * @param period Minimum interval between emissions.
 * @return A new [Flow] that emits throttled values.
 */
fun <T> Flow<T>.throttleFirst(period: Duration): Flow<T> = flow {
    var gateJob: Job? = null
    collect { value ->
        if (gateJob?.isActive != true) {
            emit(value)
            gateJob = currentCoroutineContext()[Job]?.let { parentJob ->
                CoroutineScope(parentJob).launch {
                    delay(period)
                }
            }
        }
    }
}