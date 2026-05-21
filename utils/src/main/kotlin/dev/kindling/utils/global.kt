package dev.kindling.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.currentCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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
    var lastEmitTime = 0L
    collect { value ->
        val now = System.currentTimeMillis()
        if (now - lastEmitTime >= period.inWholeMilliseconds) {
            lastEmitTime = now
            emit(value)
        }
    }
}
