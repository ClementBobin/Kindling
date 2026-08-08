package dev.kindling.library.utils.method

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    suspend fun <T> run(block: suspend () -> T): T {
        mutex.lock()
        _isLocked.value = true
        return try {
            block()
        } finally {
            mutex.unlock()
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
            mutex.unlock()
            _isLocked.value = mutex.isLocked
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SingleFlight
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Deduplicates concurrent calls to the same async operation.
 *
 * If [get] is called while a previous call is still in progress, **all callers
 * share the same in-flight result** rather than triggering duplicate work.
 * Once the in-flight call finishes, the next call starts a fresh execution.
 *
 * Inspired by Go's `singleflight` package and the "single-flight" cache pattern.
 *
 * ```kotlin
 * val flight = SingleFlight<User>()
 *
 * // Two simultaneous callers both get the same result from one API call:
 * coroutineScope {
 *     val a = async { flight.get { api.fetchCurrentUser() } }
 *     val b = async { flight.get { api.fetchCurrentUser() } }
 *     // api.fetchCurrentUser() is called exactly once
 *     assertEquals(a.await(), b.await())
 * }
 * ```
 */
class SingleFlight<T> @OptIn(DelicateCoroutinesApi::class) constructor(private val scope: CoroutineScope = GlobalScope) {
    private val mutex = Mutex()
    private var inFlight: Deferred<T>? = null

    suspend fun get(block: suspend () -> T): T {
        val deferred = mutex.withLock {
            inFlight ?: scope.async {
                try {
                    block()
                } finally {
                    mutex.withLock { if (inFlight?.isActive != true) inFlight = null }
                }
            }.also { inFlight = it }
        }

        return deferred.await()
    }
}
