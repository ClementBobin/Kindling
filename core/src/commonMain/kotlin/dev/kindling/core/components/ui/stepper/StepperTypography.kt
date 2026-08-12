package dev.kindling.core.components.ui.stepper

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Step title text — mirrors `StepperTitle`. */
@Composable
fun StepperTitle(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = useStepper()
    val itemValue = LocalStepperItemValue.current
    val active = state.dataState(itemValue) == KStepState.Active
    ProvideTextStyle(
        MaterialTheme.typography.labelMedium.copy(
            fontSize   = 13.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (active) MaterialTheme.colorScheme.onBackground
                         else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) { Box(modifier = modifier) { content() } }
}

/** Step description text — mirrors `StepperDescription`. */
@Composable
fun StepperDescription(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ProvideTextStyle(
        MaterialTheme.typography.bodySmall.copy(
            fontSize = 11.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) { Box(modifier = modifier) { content() } }
}