package dev.kindling.utils.method

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.math.pow
import kotlin.random.Random

/**
 * Retries a suspending operation with configurable exponential back-off.
 *
 * Port of the `useRetry` React hook.
 *
 * ```kotlin
 * val runner = RetryRunner<String>(
 *     scope = viewModelScope,
 *     retries = 3,
 *     delay = 250.milliseconds,
 *     backoffFactor = 2.0,
 *     onSuccess = { value -> println("Got: $value") },
 *     onError = { err -> println("Attempt failed: $err") },
 * )
 *
 * // Manually trigger:
 * runner.run { fetchData() }
 *
 * // Or auto-run with a fixed operation:
 * // autoRun cancels the previous auto-run job and launches block once.
 * runner.autoRun { fetchData() }
 *
 * // Observe state:
 * runner.isLoading.collect { loading -> showSpinner(loading) }
 * runner.value.collect { v -> if (v != null) render(v) }
 * ```
 *
 * @param T              The result type of the async operation.
 * @param scope          [CoroutineScope] that owns retry jobs.
 * @param retries        Maximum number of *additional* attempts after the first. Default: 3.
 * @param delay          Initial wait between attempts. Default: 250 ms.
 * @param backoffFactor  Multiplier applied to the delay after each failure. Default: 2.0.
 * @param maxDelay       Ceiling on the computed delay. Default: 10 s.
 * @param onSuccess      Optional callback invoked once with the successful value.
 * @param onError        Optional callback invoked after each failed attempt.
 */
class KRetryRunner<T>(
    private val scope: CoroutineScope,
    val retries: Int = 3,
    val delay: Duration = 250.milliseconds,
    val backoffFactor: Double = 2.0,
    val maxDelay: Duration = 10.seconds,
    val onSuccess: ((T) -> Unit)? = null,
    val onError: ((Throwable) -> Unit)? = null,
) {
    private val _isLoading = MutableStateFlow(false)
    private val _attempt = MutableStateFlow(0)
    private val _value = MutableStateFlow<T?>(null)
    private val _error = MutableStateFlow<Throwable?>(null)

    /** Whether an operation is currently running. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** The 1-based index of the current (or last completed) attempt. */
    val attempt: StateFlow<Int> = _attempt.asStateFlow()

    /** The last successful value, or `null`. */
    val value: StateFlow<T?> = _value.asStateFlow()

    /** The last error seen, or `null` when successful. */
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    private var currentJob: Job? = null
    private var autoRunJob: Job? = null

    /**
     * Runs [block] with up to [retries] retries. Returns the successful value.
     * Throws the last exception if all attempts are exhausted.
     *
     * Any previously running job is cancelled first.
     */
    suspend fun run(block: suspend () -> T): T {
        var lastError: Throwable? = null

        _isLoading.value = true
        _error.value = null
        _attempt.value = 0

        try {
            for (i in 0..retries) {
                _attempt.value = i + 1
                try {
                    val result = block()
                    _value.value = result
                    _error.value = null
                    onSuccess?.invoke(result)
                    return result
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    lastError = e
                    _error.value = e
                    onError?.invoke(e)
                    if (i < retries) {
                        val baseDelay = delay.inWholeMilliseconds * backoffFactor.pow(i)
                        val jitter = Random.nextLong(0, (baseDelay * 0.1).toLong().coerceAtLeast(1))
                        val nextDelay = minOf(
                            maxDelay.inWholeMilliseconds,
                            (baseDelay + jitter).toLong()
                        ).milliseconds
                        kotlinx.coroutines.delay(nextDelay)
                    }
                }
            }
            throw lastError!!
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Launches [block] in [scope] with retries, returning the [Job].
     * Cancels any existing launch first.
     */
    fun launch(block: suspend () -> T): Job {
        val oldJob = currentJob
        return scope.launch {
            oldJob?.cancelAndJoin()
            runCatching { run(block) }
        }.also { currentJob = it }
    }

    /**
     * Keeps calling [block] immediately (useful for re-running when dependencies change).
     * Cancels any previous auto-run job.
     */
    fun autoRun(block: suspend () -> T) {
        autoRunJob?.cancel()
        autoRunJob = launch(block)
    }

    /** Resets all state. Does not cancel a running job. */
    fun reset() {
        _isLoading.value = false
        _attempt.value = 0
        _value.value = null
        _error.value = null
    }

    /** Cancels any in-progress job without resetting state. */
    fun cancel() {
        currentJob?.cancel()
        autoRunJob?.cancel()
    }

    override fun toString() =
        "RetryRunner(isLoading=${_isLoading.value}, attempt=${_attempt.value}, " +
        "hasValue=${_value.value != null}, hasError=${_error.value != null})"
}

// ─────────────────────────────────────────────────────────────────────────────
//  Internal helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun Double.pow(n: Int): Double = this.pow(n.toDouble())
