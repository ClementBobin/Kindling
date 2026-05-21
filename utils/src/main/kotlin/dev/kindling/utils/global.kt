package dev.kindling.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

/**
 * Leading-edge debounce for [Flow] values.
 *
 * @param duration The quiet period after which the next value may be emitted.
 * @return A new [Flow] that emits debounced values.
 */
fun <T> Flow<T>.debounceLeading(duration: Duration): Flow<T> = flow {
    var lastEmitTime = 0L
    collect { value ->
        val now = System.currentTimeMillis()
        if (now - lastEmitTime >= duration.inWholeMilliseconds) {
            lastEmitTime = now
            emit(value)
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
