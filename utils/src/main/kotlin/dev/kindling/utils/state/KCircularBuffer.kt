package dev.kindling.utils.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fixed-capacity ring buffer (Circular Buffer).
 *
 * When the buffer is full, adding a new item silently drops the **oldest** entry.
 * This implementation is ideal for bounded logs, recent-search history, rolling telemetry windows,
 * or any scenario where you only need to keep the last `N` items.
 *
 * The buffer state is exposed as a [StateFlow], making it easy to observe changes in a reactive UI.
 *
 * ### Example usage:
 * ```kotlin
 * val log = KCircularBuffer<String>(capacity = 3)
 * log.add("a")  // Buffer: [a]
 * log.add("b")  // Buffer: [a, b]
 * log.add("c")  // Buffer: [a, b, c]
 * log.add("d")  // Buffer: [b, c, d]  ← "a" is automatically dropped
 *
 * println(log.toList()) // Prints: [b, c, d]
 * println(log.latest()) // Prints: "d"
 * 
 * // In a Composable:
 * val items by log.state.collectAsState()
 * LazyColumn {
 *     items(items) { Text(it) }
 * }
 * ```
 *
 * @param T The type of elements held in this buffer.
 * @param capacity The maximum number of items the buffer can retain. Must be ≥ 1.
 * @param initialItems Optional list of items to pre-populate the buffer with (ordered oldest to newest).
 *                     If the list size exceeds [capacity], only the last [capacity] items are kept.
 */
class KCircularBuffer<T>(
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

    private val _state = MutableStateFlow(initialDeque().toList())

    /**
     * Snapshot of all items as a [StateFlow] (oldest → newest).
     * Emits on every structural change.
     */
    val state: StateFlow<List<T>> = _state.asStateFlow()

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
        _state.value = next.toList()
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
        _state.value = next.toList()
    }

    /** Removes all items from the buffer. */
    fun clear() { _state.value = emptyList() }

    override fun toString() = "CircularBuffer(capacity=$capacity, size=$size)"
}
