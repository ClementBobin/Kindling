package dev.kindling.core.components.ui.stepper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.kindling.core.theme.LocalKindlingShapes
import kotlinx.coroutines.launch

/**
 * Tappable trigger for a step — mirrors `StepperTrigger`.
 *
 * Navigates to this step on tap (unless `nonInteractive`).
 */
@Composable
fun StepperTrigger(
    modifier: Modifier = Modifier,
    hasLabel: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val state = useStepper()
    val itemValue = LocalStepperItemValue.current
    val nonInteractive = LocalStepperNonInteractive.current
    val disabled = LocalStepperDisabled.current
    val scope = rememberCoroutineScope()

    val shape = if (hasLabel)
        LocalKindlingShapes.current.radiusMd
    else
        CircleShape

    Row(
        modifier = modifier
            .clip(shape)
            .then(
                if (!nonInteractive && !disabled && itemValue.isNotEmpty())
                    Modifier.clickable {
                        scope.launch {
                            val idx = state.steps.indexOf(itemValue)
                            val current = state.currentIndex
                            val dir = if (idx > current) KNavigationDirection.Next
                                      else KNavigationDirection.Prev
                            state.navigateTo(itemValue, dir)
                        }
                    }
                else Modifier
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}