package dev.kindling.utilsls.method

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A boolean toggle with optional forced-set support.
 *
 * Port of the `useToggle` React hook.
 *
 * ```kotlin
 * val toggle = Toggle(defaultValue = false)
 * toggle.toggle()       // → true
 * toggle.toggle()       // → false
 * toggle.set(true)      // → true
 * toggle.set(false)     // → false
 *
 * toggle.state.collect { enabled -> applyTheme(enabled) }
 * ```
 *
 * @param defaultValue Initial state. Default: `false`.
 */
class Toggle(defaultValue: Boolean = false) {

    private val _state = MutableStateFlow(defaultValue)

    /** The current toggle value as a [StateFlow]. */
    val state: StateFlow<Boolean> = _state.asStateFlow()

    /** The current toggle value. */
    val value: Boolean get() = _state.value

    /** Flips the current value. */
    fun toggle() { _state.value = !_state.value }

    /** Forces the value to [newValue]. */
    fun set(newValue: Boolean) { _state.value = newValue }

    override fun toString() = "Toggle(value=$value)"
}