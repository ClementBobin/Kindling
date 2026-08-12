package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

val LocalStepperItemValue = compositionLocalOf { "" }

/**
 * Container for one step — mirrors `StepperItem`.
 *
 * @param value     Unique step identifier matching one of [StepperState.steps].
 * @param completed Override completion state (auto-derived from [StepperState] by default).
 * @param disabled  Disables interaction for this step only.
 */
@Composable
fun RowScope.StepperItem(
    value: String,
    modifier: Modifier = Modifier,
    completed: Boolean? = null,
    disabled: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val state = useStepper()
    val isLast = state.steps.lastOrNull() == value

    CompositionLocalProvider(LocalStepperItemValue provides value) {
        Row(
            modifier = if (isLast) modifier else modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}