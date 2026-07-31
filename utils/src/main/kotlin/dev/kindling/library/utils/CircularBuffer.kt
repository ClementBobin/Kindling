package dev.kindling.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fixed-capacity ring buffer.
 *
 * When the buffer is full, adding a new item silently drops the **oldest** entry.
 * Useful for bounded logs, recent-search history, rolling telemetry windows, etc.
 *
 * ```kotlin
 * val log = CircularBuffer<String>(capacity = 3)
 * log.add("a")  // [a]
 * log.add("b")  // [a, b]
 * log.add("c")  // [a, b, c]
 * log.add("d")  // [b, c, d]  ← "a" dropped
 *
 * println(log.toList()) // [b, c, d]
 * println(log.latest()) // "d"
 *
 * log.state.collect { items -> render(items) }
 * ```
 *
 * @param capacity Maximum number of items retained. Must be ≥ 1.
 * @param initialItems Optional items to pre-populate (oldest first).
 *                     Items beyond [capacity] are trimmed from the front.
 */
class CircularBuffer<T>(
    val capacity: Int,
    private val initialItems: List<T> = emptyList(), 
) {
    init { require(capacity >= 1) { "capacity must be at least 1" } }

    private fun initialDeque(): ArrayDeque<T> {
        val d = ArrayDeque<T>(capacity)
        val trimmed = if (initialItems.size > capacity)
            initialItems.takeLast(capacity) else initialItems
        d.addAll(trimmed)
        return d
    }

    private val _state = MutableStateFlow(initialDeque())

    /**
     * Snapshot of all items as a [StateFlow] (oldest → newest).
     * Emits on every structural change.
     */
    val state: StateFlow<ArrayDeque<T>> = _state.asStateFlow()

    /** Number of items currently in the buffer. */
    val size: Int get() = _state.value.size

    /** `true` when the buffer is at full [capacity]. */
    val isFull: Boolean get() = _state.value.size >= capacity

    /** `true` when the buffer contains no items. */
    val isEmpty: Boolean get() = _state.value.isEmpty()

    /**
     * Adds [item] to the buffer.
     * If the buffer is full, the oldest item is removed first.
     */
    fun add(item: T) {
        val next = ArrayDeque(_state.value)
        if (next.size >= capacity) next.removeFirst()
        next.addLast(item)
        _state.value = next
    }

    /**
     * Adds all [items] in order, dropping oldest entries as needed to stay
     * within [capacity].
     */
    fun addAll(items: Iterable<T>) = items.forEach { add(it) }

    /** Returns the most recently added item, or `null` if empty. */
    fun latest(): T? = _state.value.lastOrNull()

    /** Returns the oldest item currently in the buffer, or `null` if empty. */
    fun oldest(): T? = _state.value.firstOrNull()

    /** Returns a snapshot list ordered oldest → newest. */
    fun toList(): List<T> = _state.value.toList()

    /** remove a single item from the buffer. */
    fun remove(item: T) {
        val next = ArrayDeque(_state.value)
        next.remove(item)
        _state.value = next
    }

    /** Removes all items from the buffer. */
    fun clear() { _state.value = ArrayDeque() }

    override fun toString() = "CircularBuffer(capacity=$capacity, size=$size)"
}
