package dev.kindling.core.components.ui.stepper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Observable state for a [KStepper].
 *
 * Access via [useKStepper] inside any composable that descends from [KStepper].
 * Mirrors the `useStepper` hook from `stepper.tsx`.
 */
@Stable
class KStepperState internal constructor(
    initialValue: String,
    val steps: List<String>,
    val onValueChange: (String) -> Unit,
    val onValidate: (suspend (value: String, direction: KNavigationDirection) -> Boolean)?
) {
    var value by mutableStateOf(initialValue)
        internal set

    val currentIndex: Int get() = steps.indexOf(value)

    val canGoNext: Boolean get() = currentIndex < steps.size - 1
    val canGoPrev: Boolean get() = currentIndex > 0

    fun dataState(stepValue: String): KStepState {
        val idx     = steps.indexOf(stepValue)
        val current = currentIndex
        return when {
            idx < current  -> KStepState.Completed
            idx == current -> KStepState.Active
            else           -> KStepState.Inactive
        }
    }

    internal suspend fun navigateTo(
        target: String,
        direction: KNavigationDirection
    ): Boolean {
        if (onValidate != null) {
            val ok = onValidate.invoke(target, direction)
            if (!ok) return false
        }
        value = target
        onValueChange(target)
        return true
    }
}

/**
 * Creates and remembers a [KStepperState].
 *
 * ```kotlin
 * val stepper = rememberStepperState(
 *     steps        = listOf("account", "billing", "review"),
 *     defaultValue = "account"
 * )
 * Stepper(state = stepper) { … }
 * ```
 */
@Composable
fun rememberKStepperState(
    steps: List<String>,
    defaultValue: String = steps.firstOrNull() ?: "",
    value: String? = null,
    onValueChange: (String) -> Unit = {},
    onValidate: (suspend (value: String, direction: KNavigationDirection) -> Boolean)? = null
): KStepperState {
    val state = remember(steps) {
        KStepperState(value ?: defaultValue, steps, onValueChange, onValidate)
    }
    LaunchedEffect(value) { if (value != null) state.value = value }
    return state
}