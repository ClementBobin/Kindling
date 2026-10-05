package dev.kindling.utils.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A reactive FIFO queue.
 *
 * Port of the `useQueue` React hook (which itself built on `useArray`).
 *
 * ```kotlin
 * val queue = KQueue<String>()
 * queue.enqueue("first")
 * queue.enqueue("second")
 *
 * println(queue.peek())    // "first"
 * println(queue.dequeue()) // "first"
 * println(queue.size)      // 1
 *
 * // Observe reactively:
 * queue.state.collect { items -> render(items) }
 * ```
 *
 * @param initialItems Items to pre-populate the queue with (front → back order).
 */
class KQueue<T>(initialItems: List<T> = emptyList()) {

    private val _state = MutableStateFlow(initialItems.toList())

    /** The underlying queue contents as a [StateFlow] (front → back). */
    val state: StateFlow<List<T>> = _state.asStateFlow()

    /** Number of items currently in the queue. */
    val size: Int get() = _state.value.size

    /** `true` when the queue contains no items. */
    val isEmpty: Boolean get() = _state.value.isEmpty()

    /** Adds [item] to the back of the queue. */
    fun enqueue(item: T) {
        _state.value = (_state.value + item)
    }

    /**
     * Removes and returns the front item, or `null` if the queue is empty.
     */
    fun dequeue(): T? {
        val current = _state.value
        if (current.isEmpty()) return null
        val item = current.first()
        _state.value = current.drop(1)
        return item
    }

    /** Returns the front item without removing it, or `null` if empty. */
    fun peek(): T? = _state.value.firstOrNull()

    /** Removes all items from the queue. */
    fun clear() { _state.value = emptyList() }

    override fun toString() = "KQueue(size=$size, front=${peek()})"
}