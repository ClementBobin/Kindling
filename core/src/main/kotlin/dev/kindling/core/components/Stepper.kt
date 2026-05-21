package dev.kindling.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class KStep(
    val label: String,
    val description: String? = null,
    val state: KStepState = KStepState.Upcoming
)

/**
 * Shadcn/ui-style multi-step progress indicator.
 *
 * ```kotlin
 * var step by remember { mutableStateOf(0) }
 * KStepper(steps = steps, currentStep = step)
 * KButton("Next", onClick = { step++ })
 * ```
 */
@Composable
fun KStepper(
    steps: List<KStep>,
    currentStep: Int,
    modifier: Modifier = Modifier,
    orientation: KStepperOrientation = KStepperOrientation.Horizontal,
    onStepClick: ((Int) -> Unit)? = null
) {
    val resolved = steps.mapIndexed { i, s ->
        when {
            s.state != KStepState.Upcoming -> s
            i < currentStep                -> s.copy(state = KStepState.Completed)
            i == currentStep               -> s.copy(state = KStepState.Current)
            else                           -> s.copy(state = KStepState.Upcoming)
        }
    }
    if (orientation == KStepperOrientation.Horizontal)
        HorizontalStepper(resolved, modifier, onStepClick)
    else
        VerticalStepper(resolved, modifier, onStepClick)
}

@Composable
private fun HorizontalStepper(steps: List<KStep>, modifier: Modifier, onStepClick: ((Int) -> Unit)?) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        steps.forEachIndexed { i, step ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (i > 0) StepConnector(filled = step.state == KStepState.Completed, modifier = Modifier.weight(1f))
                    StepBubble(step, i + 1, if (onStepClick != null) ({ onStepClick(i) }) else null)
                    if (i < steps.lastIndex) StepConnector(
                        filled = steps[i + 1].state == KStepState.Completed || steps[i + 1].state == KStepState.Current,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(step.label, fontSize = 12.sp, fontWeight = if (step.state == KStepState.Current) FontWeight.SemiBold else FontWeight.Normal, color = stepLabelColor(step.state))
                if (step.description != null) Text(step.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun VerticalStepper(steps: List<KStep>, modifier: Modifier, onStepClick: ((Int) -> Unit)?) {
    Column(modifier = modifier) {
        steps.forEachIndexed { i, step ->
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StepBubble(step, i + 1, if (onStepClick != null) ({ onStepClick(i) }) else null)
                    if (i < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp).height(32.dp)
                                .background(
                                    if (steps[i + 1].state == KStepState.Completed || steps[i + 1].state == KStepState.Current)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(step.label, fontSize = 14.sp, fontWeight = if (step.state == KStepState.Current) FontWeight.SemiBold else FontWeight.Normal, color = stepLabelColor(step.state))
                    if (step.description != null) Text(step.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (i < steps.lastIndex) Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun StepBubble(step: KStep, number: Int, onClick: (() -> Unit)?) {
    val cs = MaterialTheme.colorScheme
    val bgColor by animateColorAsState(
        when (step.state) {
            KStepState.Completed, KStepState.Current -> cs.primary
            KStepState.Error                          -> cs.error
            KStepState.Upcoming                       -> Color.Transparent
        }, tween(300), label = "stepBg"
    )
    val borderColor by animateColorAsState(
        when (step.state) {
            KStepState.Completed, KStepState.Current -> cs.primary
            KStepState.Error                          -> cs.error
            KStepState.Upcoming                       -> cs.outline
        }, tween(300), label = "stepBorder"
    )
    val contentColor = when (step.state) {
        KStepState.Completed, KStepState.Current -> cs.onPrimary
        KStepState.Error                          -> cs.onError
        KStepState.Upcoming                       -> cs.onSurfaceVariant
    }

    Surface(
        shape  = CircleShape,
        color  = bgColor,
        border = if (step.state == KStepState.Upcoming) androidx.compose.foundation.BorderStroke(1.5.dp, borderColor) else null,
        modifier = Modifier.size(28.dp).then(
            if (onClick != null) Modifier.clip(CircleShape).clickable(onClick = onClick) else Modifier
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (step.state == KStepState.Completed) {
                Icon(Icons.Default.Check, null, tint = contentColor, modifier = Modifier.size(14.dp))
            } else {
                Text(number.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
            }
        }
    }
}

@Composable
private fun StepConnector(filled: Boolean, modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        tween(300), label = "connectorColor"
    )
    Box(modifier = modifier.height(2.dp).background(color))
}

@Composable
private fun stepLabelColor(state: KStepState): Color {
    val cs = MaterialTheme.colorScheme
    return when (state) {
        KStepState.Current, KStepState.Completed -> cs.onBackground
        KStepState.Error                          -> cs.error
        KStepState.Upcoming                       -> cs.onSurfaceVariant
    }
}
