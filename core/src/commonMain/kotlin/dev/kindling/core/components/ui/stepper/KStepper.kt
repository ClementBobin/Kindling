package dev.kindling.core.components.ui.stepper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Internal composition locals for child slots
val LocalStepperState = compositionLocalOf<KStepperState?> { null }
val LocalStepperDisabled = compositionLocalOf { false }
val LocalStepperNonInteractive = compositionLocalOf { false }
val LocalStepperOrientation = compositionLocalOf { KStepperOrientation.Horizontal }

/**
 * Reads the nearest [KStepperState] — mirrors `useStepper` from `stepper.tsx`.
 *
 * ```kotlin
 * val stepper = useKStepper()
 * Text("Step ${stepper.currentIndex + 1} of${stepper.steps.size}")
 * ```
 */
@Composable
fun useKStepper(): KStepperState =
    LocalStepperState.current
        ?: error("`useKStepper()` must be called inside a `Stepper` composable")

/**
 * Shadcn/ui-style Stepper root — mirrors `Stepper` from `stepper.tsx`.
 *
 * Provides [KStepperState] to all child slots via [useKStepper].
 *
 * ```kotlin
 * val state = rememberKStepperState(steps = listOf("step-1", "step-2", "step-3"))
 *
 * KStepper(state = state) {
 *     KStepperList {
 *         listOf("Account", "Billing", "Review").forEachIndexed { i, label ->
 *             KStepperItem(value = "step-${i+1}") {
 *                 KStepperTrigger {
 *                     KStepperIndicator()
 *                     Column {
 *                         KStepperTitle { Text(label) }
 *                         KStepperDescription { Text("Details") }
 *                     }
 *                 }
 *                 KStepperSeparator()
 *             }
 *         }
 *     }
 *     KStepperContent(value = "step-1") { Text("Step 1 content") }
 *     KStepperContent(value = "step-2") { Text("Step 2 content") }
 *     KStepperContent(value = "step-3") { Text("Step 3 content") }
 *     Row {
 *         KStepperPrev { KButton("Back",  onClick = it) }
 *         KStepperNext { KButton("Next",  onClick = it) }
 *     }
 * }
 * ```
 */
@Composable
fun KStepper(
    state: KStepperState,
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