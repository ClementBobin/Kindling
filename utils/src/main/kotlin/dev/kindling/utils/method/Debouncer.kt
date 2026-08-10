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
 * values have arrived for [delay].
 *
 * ### Basic usage
 * ```kotlin
 * val debouncer = Debouncer<String>(scope = viewModelScope, delay = 300.milliseconds)
 *
 * // Emit from a text-field's onValueChange:
 * onValueChange = { text -> debouncer.emit(text) }
 *
 * // Collect the stable values:
 * debouncer.flow.collect { stableText -> search(stableText) }
 * ```
 *
 * ### One-shot callback style
 * ```kotlin
 * val debouncer = Debouncer<String>(scope = viewModelScope)
 * debouncer.onDebounced { query -> viewModel.search(query) }
 * // then in UI:
 * debouncer.emit(searchText)
 * ```
 *
 * ### Trailing-edge (default) vs leading-edge
 * By default the debouncer fires *after* the quiet period ends (trailing edge).
 * Set [leading] = `true` to fire immediately on the first value, then suppress
 * subsequent values for [delay].
 *
 * @param T        The type of values being debounced.
 * @param scope    The [CoroutineScope] that owns the internal job.
 * @param delay    Quiet period before a value is forwarded.  Default: 300 ms.
 * @param leading  If `true`, emit the first value immediately and suppress
 *                 the rest until the quiet period elapses.  Default: `false`.
 */
@OptIn(FlowPreview::class)
class Debouncer<T>(
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
        callbackJob = CoroutineScope(SupervisorJob() + scope.coroutineContext).launch(start = CoroutineStart.UNDISPATCHED) {
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
 * A coroutine-based throttler that forwards at most one value per [period].
 *
 * Uses leading-edge semantics: the first value in each window is emitted
 * immediately; subsequent values within the same window are dropped.
 *
 * ```kotlin
 * val throttler = Throttler<ClickEvent>(scope = viewModelScope, period = 500.milliseconds)
 * throttler.onThrottled { event -> handleClick(event) }
 * // in UI:
 * throttler.emit(ClickEvent)
 * ```
 */
class Throttler<T>(
    private val scope: CoroutineScope,
    val period: Duration = 500.milliseconds
) {
    private val _input = MutableSharedFlow<T>(extraBufferCapacity = 64, replay = 1)

    /** Throttled output flow. */
    val flow: Flow<T> = _input.throttleFirst(period)

    private var callbackJob: Job? = null

    fun emit(value: T) { _input.tryEmit(value) }

    fun onThrottled(block: suspend (T) -> Unit) {
        callbackJob?.cancel()
        callbackJob = CoroutineScope(SupervisorJob() + scope.coroutineContext).launch(start = CoroutineStart.UNDISPATCHED) { flow.collect { block(it) } }
    }

    fun cancel() { callbackJob?.cancel(); callbackJob = null }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Convenience top-level factories
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Creates a [Debouncer] and immediately registers a callback.
 *
 * ```kotlin
 * private val searchDebouncer = debounce(viewModelScope, 300.milliseconds) { q -> search(q) }
 * // in UI: onValueChange = { searchDebouncer.emit(it) }
 * ```
 */
fun <T> debounce(
    scope: CoroutineScope,
    delay: Duration = 300.milliseconds,
    leading: Boolean = false,
    block: suspend (T) -> Unit
): Debouncer<T> = Debouncer<T>(scope, delay, leading).also { it.onDebounced(block) }

/**
 * Creates a [Throttler] and immediately registers a callback.
 *
 * ```kotlin
 * private val clickThrottler = throttle(viewModelScope, 500.milliseconds) { handleClick() }
 * ```
 */
fun <T> throttle(
    scope: CoroutineScope,
    period: Duration = 500.milliseconds,
    block: suspend (T) -> Unit
): Throttler<T> = Throttler<T>(scope, period).also { it.onThrottled(block) }

/**
 * Leading-edge debounce for [Flow] values.
 *
 * @param duration The quiet period after which the next value may be emitted.
 * @return A new [Flow] that emits debounced values.
 */
fun <T> Flow<T>.debounceLeading(duration: Duration): Flow<T> = flow {
    var lastEmitTime = 0L
    var pendingJob: Job? = null

    collect { value ->
        val now = System.currentTimeMillis()
        if (now - lastEmitTime >= duration.inWholeMilliseconds) {
            pendingJob?.cancel()
            lastEmitTime = now
            emit(value)
        } else {
            pendingJob?.cancel()
            pendingJob = currentCoroutineContext()[Job]?.let { null } // reset
            // Schedule trailing emit after quiet period
            delay(duration - (now - lastEmitTime).milliseconds)
            emit(value)
            lastEmitTime = System.currentTimeMillis()
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