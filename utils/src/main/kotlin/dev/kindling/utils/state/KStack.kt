package dev.kindling.utils.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A reactive LIFO stack.
 *
 * Pairs naturally with [KQueue] for producer/consumer patterns.
 *
 * ```kotlin
 * val stack = KStack<String>()
 * stack.push("first")
 * stack.push("second")
 *
 * println(stack.peek())  // "second"
 * println(stack.pop())   // "second"
 * println(stack.size)    // 1
 *
 * stack.state.collect { items -> render(items) }
 * ```
 *
 * @param initialItems Items to pre-populate (index 0 = bottom of stack).
 */
class KStack<T>(initialItems: List<T> = emptyList()) {

    private val _state = MutableStateFlow(initialItems.toList())

    /** Stack contents as a [StateFlow] (index 0 = bottom, last = top). */
    val state: StateFlow<List<T>> = _state.asStateFlow()

    /** Number of items currently in the stack. */
    val size: Int get() = _state.value.size

    /** `true` when the stack contains no items. */
    val isEmpty: Boolean get() = _state.value.isEmpty()

    /** Pushes [item] onto the top of the stack. */
    fun push(item: T) {
        _state.value = (_state.value + item)
    }

    /**
     * Removes and returns the top item, or `null` if empty.
     */
    fun pop(): T? {
        val current = _state.value
        if (current.isEmpty()) return null
        val item = current.last()
        _state.value = current.dropLast(1)
        return item
    }

    /** Returns the top item without removing it, or `null` if empty. */
    fun peek(): T? = _state.value.lastOrNull()

    /** Removes all items. */
    fun clear() { _state.value = emptyList() }

    override fun toString() = "KStack(size=$size, top=${peek()})"
}