package dev.kindling.utils.method

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// ─────────────────────────────────────────────────────────────────────────────
//  MutexGuard
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Ensures that only **one coroutine at a time** can execute a critical section.
 *
 * Wraps [Mutex] with an observable [isLocked] state and optional
 * "skip if busy" semantics via [tryRun].
 *
 * ### Serialize concurrent writes
 * ```kotlin
 * val guard = MutexGuard()
 *
 * // Multiple coroutines calling save() will queue up safely:
 * suspend fun save(data: Data) = guard.run {
 *     repository.write(data)
 * }
 * ```
 *
 * ### Skip if already running (debounce-style)
 * ```kotlin
 * val guard = MutexGuard()
 *
 * // A second refresh call while the first is in progress does nothing:
 * suspend fun refresh() = guard.tryRun {
 *     api.fetchLatest()
 * }
 * ```
 *
 * ### Observe lock state
 * ```kotlin
 * guard.isLocked.collect { locked -> showSpinner(locked) }
 * ```
 */
class MutexGuard {

    private val mutex = Mutex()

    private val _isLocked = MutableStateFlow(false)

    /** `true` while a coroutine holds the lock. */
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    /**
     * Suspends until the lock is available, then executes [block] exclusively.
     * Always runs [block], even if other coroutines are queued behind it.
     *
     * @return The return value of [block].
     */
    suspend fun <T> run(block: suspend () -> T): T = mutex.withLock {
        _isLocked.value = true
        try {
            block()
        } finally {
            _isLocked.value = mutex.isLocked
        }
    }

    /**
     * Executes [block] only if the lock is **not** currently held.
     * If another coroutine is already inside [run] or [tryRun], this call
     * returns `null` immediately without suspending.
     *
     * @return The return value of [block], or `null` if skipped.
     */
    suspend fun <T> tryRun(block: suspend () -> T): T? {
        if (!mutex.tryLock()) return null
        _isLocked.value = true
        return try {
            block()
        } finally {
            _isLocked.value = false
            mutex.unlock()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SingleFlight
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Deduplicates concurrent calls to the same async operation.
 *
 * If [get] is called while a previous call with the same [key] is still in progress,
 * **all callers share the same in-flight result** rather than triggering duplicate work.
 * Once the in-flight call finishes, the next call starts a fresh execution.
 *
 * Inspired by Go's `singleflight` package and the "single-flight" cache pattern.
 *
 * ```kotlin
 * val flight = SingleFlight<User>()
 *
 * // Two simultaneous callers both get the same result from one API call:
 * coroutineScope {
 *     val a = async { flight.get("fetch_user") { api.fetchCurrentUser() } }
 *     val b = async { flight.get("fetch_user") { api.fetchCurrentUser() } }
 *     // api.fetchCurrentUser() is called exactly once
 *     assertEquals(a.await(), b.await())
 * }
 * ```
 */
class SingleFlight<T> {
    private val mutex = Mutex()
    private val inFlight = mutableMapOf<Any, Deferred<T>>()

    /**
     * Executes [block] or awaits an existing in-flight execution sharing the same [key].
     *
     * @param key Request key to distinguish distinct concurrent operations. Defaults to [Unit].
     * @param block The suspending work to execute.
     */
    suspend fun get(key: Any = Unit, block: suspend () -> T): T {
        val deferred = mutex.withLock {
            inFlight[key] ?: CoroutineScope(currentCoroutineContext()).async {
                block()
            }.also { newDeferred ->
                inFlight[key] = newDeferred
            }
        }

        return try {
            deferred.await()
        } finally {
            mutex.withLock {
                if (inFlight[key] === deferred) {
                    inFlight.remove(key)
                }
            }
        }
    }
}