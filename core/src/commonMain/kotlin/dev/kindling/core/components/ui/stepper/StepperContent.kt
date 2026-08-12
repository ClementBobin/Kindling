package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Panel shown only when [value] is the active step.
 * Pass [forceMount] = true to always render (hidden via alpha).
 * Mirrors `StepperContent`.
 */
@Composable
fun StepperContent(
    value: String,
    modifier: Modifier = Modifier,
    forceMount: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val state = useStepper()
    if (state.value != value && !forceMount) return
    Box(modifier = modifier.fillMaxWidth(), content = content)
}