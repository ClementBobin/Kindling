package dev.kindling.library.utils.method

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A mutable counter with optional min/max bounds and a configurable step.
 *
 * Port of the `useCounter` React hook.
 *
 * ```kotlin
 * val counter = Counter(initialValue = 0, min = 0, max = 10, step = 2)
 * counter.increment()  // → 2
 * counter.decrement()  // → 0
 * counter.set(7)       // → 7 (clamped to [min, max] only)
 * counter.reset()      // → 0
 *
 * // Observe reactively:
 * counter.state.collect { value -> println(value) }
 * ```
 *
 * @param initialValue Starting value (clamped to [min, max]).
 * @param min          Lower bound, inclusive. Default: [Int.MIN_VALUE].
 * @param max          Upper bound, inclusive. Default: [Int.MAX_VALUE].
 * @param step         Amount added/subtracted on each increment/decrement. Default: 1.
 */
class Counter(
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
    fun set(value: Int) { _state.value = clamp(value) }

    /** Resets the counter to the original [initialValue]. */
    fun reset() { _state.value = clamp(initialValue) }

    private val initialValue = initialValue.coerceIn(min, max)

    override fun toString() = "Counter(count=$count, min=$min, max=$max, step=$step)"
}