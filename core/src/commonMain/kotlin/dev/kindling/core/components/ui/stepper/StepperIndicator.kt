package dev.kindling.core.components.ui.stepper

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Numbered / checkmark bubble — mirrors `StepperIndicator`.
 *
 * ```kotlin
 * StepperIndicator()                                     // auto: number or check
 * StepperIndicator { dataState -> MyIcon(dataState) }  // custom
 * ```
 */
@Composable
fun StepperIndicator(
    modifier: Modifier = Modifier,
    content: (@Composable (KStepState) -> Unit)? = null
) {
    val state = useStepper()
    val itemValue = LocalStepperItemValue.current
    val dsState = state.dataState(itemValue)
    val stepPos = state.steps.indexOf(itemValue) + 1
    val cs = MaterialTheme.colorScheme

    val bg by animateColorAsState(
        when (dsState) {
            KStepState.Active, KStepState.Completed -> cs.primary
            KStepState.Error                         -> cs.error
            else                                     -> Color.Transparent
        }, tween(200), label = "stepBg"
    )
    val border by animateColorAsState(
        when (dsState) {
            KStepState.Active, KStepState.Completed -> cs.primary
            KStepState.Error                         -> cs.error
            else                                     -> cs.outline
        }, tween(200), label = "stepBorder"
    )
    val fg = when (dsState) {
        KStepState.Active, KStepState.Completed -> cs.onPrimary
        KStepState.Error                         -> cs.onError
        else                                     -> cs.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bg)
            .border(if (dsState == KStepState.Inactive) 2.dp else 0.dp, border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            content != null -> content(dsState)
            dsState == KStepState.Completed ->
                Icon(Icons.Default.Check, null, tint = fg, modifier = Modifier.size(14.dp))
            else ->
                Text(stepPos.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
        }
    }
}