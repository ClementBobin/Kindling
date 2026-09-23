package dev.kindling.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    val state: StateFlow<Int> = _state.asStateFlow()

    val count: Int get() = _state.value

    private fun clamp(value: Int) = value.coerceIn(min, max)

    fun increment() {
        _state.value = ((_state.value.toLong() + step).coerceIn(min.toLong(), max.toLong())).toInt()
    }

    fun decrement() {
        _state.value = ((_state.value.toLong() - step).coerceIn(min.toLong(), max.toLong())).toInt()
    }

    fun set(value: Int) {
        _state.update { clamp(value) }
    }

    fun reset() {
        _state.update { initialValue }
    }

    private val initialValue = initialValue.coerceIn(min, max)
}
