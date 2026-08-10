package dev.kindling.android.http

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface KCircuitState {
    data object Closed : KCircuitState
    data object Open : KCircuitState
    data object HalfOpen : KCircuitState
}

class KCircuitBreaker(
    private val failureThreshold: Int = 5,
    private val successThreshold: Int = 2,
    private val resetTimeout: Duration = 30.seconds
) {
    private val mutex = Mutex()
    private var state: KCircuitState = KCircuitState.Closed
    private var failureCount = 0
    private var successCount = 0
    private var lastFailureTimestamp = 0L

    suspend fun <T> execute(block: suspend () -> T): T {
        mutex.withLock {
            checkStateLocked()
            if (state is KCircuitState.Open) {
                throw IllegalStateException("Circuit breaker is OPEN. Request blocked.")
            }
        }

        return try {
            val result = block()
            onSuccess()
            result
        } catch (e: Throwable) {
            onFailure()
            throw e
        }
    }

    private fun checkStateLocked() {
        if (state is KCircuitState.Open) {
            val now = System.currentTimeMillis()
            if (now - lastFailureTimestamp >= resetTimeout.inWholeMilliseconds) {
                state = KCircuitState.HalfOpen
                successCount = 0
            }
        }
    }

    private suspend fun onSuccess() {
        mutex.withLock {
            when (state) {
                is KCircuitState.HalfOpen -> {
                    successCount++
                    if (successCount >= successThreshold) {
                        state = KCircuitState.Closed
                        failureCount = 0
                        successCount = 0
                    }
                }
                is KCircuitState.Closed -> {
                    failureCount = 0
                }
                else -> {}
            }
        }
    }

    private suspend fun onFailure() {
        mutex.withLock {
            lastFailureTimestamp = System.currentTimeMillis()
            when (state) {
                is KCircuitState.Closed -> {
                    failureCount++
                    if (failureCount >= failureThreshold) {
                        state = KCircuitState.Open
                    }
                }
                is KCircuitState.HalfOpen -> {
                    state = KCircuitState.Open
                }
                is KCircuitState.Open -> {
                    // Already open, reset failure timestamp
                }
            }
        }
    }
}