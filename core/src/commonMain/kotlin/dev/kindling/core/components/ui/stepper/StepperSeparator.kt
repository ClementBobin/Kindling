package dev.kindling.core.components.ui.stepper

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Connector line between steps — mirrors `StepperSeparator`.
 * Hidden automatically on the last step.
 */
@Composable
fun RowScope.StepperSeparator(modifier: Modifier = Modifier) {
    val state = useStepper()
    val itemValue = LocalStepperItemValue.current
    val isLast = state.steps.lastOrNull() == itemValue
    if (isLast) return

    val dsState = state.dataState(itemValue)
    val color by animateColorAsState(
        if (dsState == KStepState.Completed || dsState == KStepState.Active)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline,
        tween(200), label = "separator"
    )
    Box(
        modifier = modifier
            .weight(1f)
            .height(2.dp)
            .background(color)
    )
}