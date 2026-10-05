package dev.kindling.utils.method

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A reactive mutable counter with optional min/max bounds and a configurable step.
 *
 * This utility is a Kotlin port of common reactive counter hooks (like `useCounter`).
 * It encapsulates the logic for incrementing, decrementing, and clamping values,
 * while exposing the current count through a [StateFlow].
 *
 * ### Example usage:
 * ```kotlin
 * val counter = KCounter(initialValue = 0, min = 0, max = 10, step = 2)
 * 
 * counter.increment()  // count becomes 2
 * counter.decrement()  // count becomes 0
 * counter.set(7)       // count becomes 7
 * counter.increment()  // count becomes 9
 * counter.increment()  // count becomes 10 (clamped to max)
 * counter.reset()      // count returns to 0
 *
 * // Reactively observing in a ViewModel or Composable:
 * viewModelScope.launch {
 *     counter.state.collect { value -> 
 *         println("Current count: $value") 
 *     }
 * }
 * ```
 *
 * @param initialValue Starting value (automatically clamped to [[min], [max]]).
 * @param min Lower bound, inclusive. Default: [Int.MIN_VALUE].
 * @param max Upper bound, inclusive. Default: [Int.MAX_VALUE].
 * @param step The amount to add or subtract on each [increment] or [decrement]. Must be positive. Default: 1.
 */
class KCounter(
    initialValue: Int = 0,
    val min: Int = Int.MIN_VALUE,
    val max: Int = Int.MAX_VALUE,
    val step: Int = 1,
) {
    init {
        require(min <= max) { "min ($min) must be <= max ($max)" }
        require(step > 0) { "step ($step) must be positive" }
    }

    private val _state = MutableStateFlow(initialValue.coerceIn(min, max))

    /** The current counter value as a [StateFlow]. */
    val state: StateFlow<Int> = _state.asStateFlow()

    /** The current counter value. */
    val count: Int get() = _state.value

    private fun clamp(value: Int) = value.coerceIn(min, max)

    /** Increments the counter by [step], clamped to [max]. */
    fun increment() {
        _state.value = ((_state.value.toLong() + step).coerceIn(min.toLong(), max.toLong())).toInt()
    }

    /** Decrements the counter by [step], clamped to [min]. */
    fun decrement() {
        _state.value = ((_state.value.toLong() - step).coerceIn(min.toLong(), max.toLong())).toInt()
    }

    /** Sets the counter to [value], clamped to [[min], [max]]. */
    fun set(value: Int) {
        _state.update { clamp(value) }
    }

    /** Resets the counter to the original [initialValue]. */
    fun reset() {
        _state.update { initialValue }
    }

    private val initialValue = initialValue.coerceIn(min, max)

    override fun toString() = "Counter(count=$count, min=$min, max=$max, step=$step)"
}