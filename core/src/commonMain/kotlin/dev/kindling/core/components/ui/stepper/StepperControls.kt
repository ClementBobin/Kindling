package dev.kindling.core.components.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Navigates to the previous step.
 * [content] receives an `onClick` lambda — wire it to your button.
 * Mirrors `StepperPrev`.
 *
 * ```kotlin
 * StepperPrev { KButton("Back", onClick = onClick) }
 * ```
 */
@Composable
fun StepperPrev(
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val state = useStepper()
    val scope = rememberCoroutineScope()
    content({
        if (state.canGoPrev) {
            scope.launch {
                val prev = state.steps[state.currentIndex - 1]
                state.navigateTo(prev, KNavigationDirection.Prev)
            }
        }
    })
}

/**
 * Navigates to the next step (runs validation if configured).
 * [content] receives an `onClick` lambda — wire it to your button.
 * Mirrors `StepperNext`.
 *
 * ```kotlin
 * StepperNext { KButton("Continue", onClick = onClick) }
 * ```
 */
@Composable
fun StepperNext(
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val state = useStepper()
    val scope = rememberCoroutineScope()
    content({
        if (state.canGoNext) {
            scope.launch {
                val next = state.steps[state.currentIndex + 1]
                state.navigateTo(next, KNavigationDirection.Next)
            }
        }
    })
}