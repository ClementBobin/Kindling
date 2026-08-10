package dev.kindling.library.utils.state

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

    private val _state = MutableStateFlow(ArrayDeque<T>(initialItems.size).also { it.addAll(initialItems) })

    /** Stack contents as a [StateFlow] (index 0 = bottom, last = top). */
    val state: StateFlow<ArrayDeque<T>> = _state.asStateFlow()

    /** Number of items currently in the stack. */
    val size: Int get() = _state.value.size

    /** `true` when the stack contains no items. */
    val isEmpty: Boolean get() = _state.value.isEmpty()

    /** Pushes [item] onto the top of the stack. */
    fun push(item: T) {
        _state.value = ArrayDeque(_state.value).also { it.addLast(item) }
    }

    /**
     * Removes and returns the top item, or `null` if empty.
     */
    fun pop(): T? {
        val current = _state.value
        if (current.isEmpty()) return null
        val next = ArrayDeque(current)
        val item = next.removeLast()
        _state.value = next
        return item
    }

    /** Returns the top item without removing it, or `null` if empty. */
    fun peek(): T? = _state.value.lastOrNull()

    /** Removes all items. */
    fun clear() { _state.value = ArrayDeque() }

    override fun toString() = "KStack(size=$size, top=${peek()})"
}