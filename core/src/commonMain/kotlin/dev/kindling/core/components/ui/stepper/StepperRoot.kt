package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

// Internal composition locals for child slots
val LocalStepperState = compositionLocalOf<StepperState?> { null }
val LocalStepperDisabled = compositionLocalOf { false }
val LocalStepperNonInteractive = compositionLocalOf { false }
val LocalStepperOrientation = compositionLocalOf { KStepperOrientation.Horizontal }

/**
 * Reads the nearest [StepperState] — mirrors `useStepper` from `stepper.tsx`.
 *
 * ```kotlin
 * val stepper = useStepper()
 * Text("Step ${stepper.currentIndex + 1} of${stepper.steps.size}")
 * ```
 */
@Composable
fun useStepper(): StepperState =
    LocalStepperState.current
        ?: error("`useStepper()` must be called inside a `Stepper` composable")

/**
 * Shadcn/ui-style Stepper root — mirrors `Stepper` from `stepper.tsx`.
 *
 * Provides [StepperState] to all child slots via [useStepper].
 * Supports horizontal and vertical orientations, RTL via [LocalLayoutDirection].
 *
 * ```kotlin
 * val state = rememberStepperState(steps = listOf("step-1", "step-2", "step-3"))
 *
 * Stepper(state = state) {
 *     StepperList {
 *         listOf("Account", "Billing", "Review").forEachIndexed { i, label ->
 *             StepperItem(value = "step-${i+1}") {
 *                 StepperTrigger {
 *                     StepperIndicator()
 *                     Column {
 *                         StepperTitle { Text(label) }
 *                         StepperDescription { Text("Details") }
 *                     }
 *                 }
 *                 StepperSeparator()
 *             }
 *         }
 *     }
 *     StepperContent(value = "step-1") { Text("Step 1 content") }
 *     StepperContent(value = "step-2") { Text("Step 2 content") }
 *     StepperContent(value = "step-3") { Text("Step 3 content") }
 *     Row {
 *         StepperPrev { KButton("Back",  onClick = it) }
 *         StepperNext { KButton("Next",  onClick = it) }
 *     }
 * }
 * ```
 */
@Composable
fun Stepper(
    state: StepperState,
    modifier: Modifier = Modifier,
    orientation: KStepperOrientation = KStepperOrientation.Horizontal,
    disabled: Boolean = false,
    nonInteractive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalStepperState provides state,
        LocalStepperDisabled provides disabled,
        LocalStepperNonInteractive provides nonInteractive,
        LocalStepperOrientation provides orientation
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}