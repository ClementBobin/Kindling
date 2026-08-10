package dev.kindling.utils.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds a value and a capped history of past values, supporting undo/redo.
 *
 * Port of the `useStateWithHistory` and `useUndoRedo` React hooks.
 *
 * ```kotlin
 * val history = StateWithHistory("hello", capacity = 10)
 *
 * history.set("world")
 * history.set("!")
 *
 * history.back()             // → "world"
 * history.forward()          // → "!"
 *
 * println(history.canUndo)   // true
 * println(history.canRedo)   // false
 *
 * // Observe reactively:
 * history.state.collect { v -> println(v) }
 * ```
 *
 * @param defaultValue Initial value pushed into history.
 * @param capacity     Maximum number of history entries retained. Default: 10.
 */
class StateWithHistory<T>(
    defaultValue: T,
    private val capacity: Int = 10,
) {
    init { require(capacity >= 1) { "capacity must be at least 1" } }

    private val history = ArrayDeque<T>(capacity + 1).also { it.addLast(defaultValue) }
    private var pointer = 0

    private val _state = MutableStateFlow(defaultValue)

    /** The current value as a [StateFlow]. */
    val state: StateFlow<T> = _state.asStateFlow()

    /** The current value. */
    val value: T get() = _state.value

    /** Snapshot of the history list (oldest → newest, up to [capacity] entries). */
    val historySnapshot: List<T> get() = history.toList()

    /** Index of the current entry within [historySnapshot]. */
    val currentPointer: Int get() = pointer

    /** `true` when there is at least one step to undo. */
    val canUndo: Boolean get() = pointer > 0

    /** `true` when there is at least one step to redo. */
    val canRedo: Boolean get() = pointer < history.size - 1

    /**
     * Updates the value and records it in history.
     * Any redo history ahead of the current pointer is discarded.
     */
    fun set(newValue: T) {
        if (history[pointer] == newValue) return

        // Drop any forward history
        while (history.size - 1 > pointer) history.removeLast()

        history.addLast(newValue)

        // Enforce capacity — drop oldest
        while (history.size > capacity) {
            history.removeFirst()
        }

        pointer = history.size - 1
        _state.value = newValue
    }

    /** Moves one step back in history. No-op if already at the oldest entry. */
    fun back() {
        if (!canUndo) return
        pointer--
        _state.value = history[pointer]
    }

    /** Moves one step forward in history. No-op if already at the newest entry. */
    fun forward() {
        if (!canRedo) return
        pointer++
        _state.value = history[pointer]
    }

    /** Jumps to an arbitrary history [index]. No-op if out of bounds. */
    fun go(index: Int) {
        if (index < 0 || index >= history.size) return
        pointer = index
        _state.value = history[pointer]
    }

    // Convenience aliases matching useUndoRedo naming
    /** Alias for [back]. */
    fun undo() = back()

    /** Alias for [forward]. */
    fun redo() = forward()

    override fun toString() =
        "StateWithHistory(value=$value, pointer=$pointer, historySize=${history.size})"
}