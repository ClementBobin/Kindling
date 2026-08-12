package dev.kindling.core.components.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Places the toast stack in your composable tree.
 *
 * Place **once** at the root, typically overlaid on your Scaffold / NavHost.
 * Call [KToastManager.show] / [KToastManager.success] etc. from anywhere.
 *
 * Mirrors shadcn/ui `Toaster` backed by Sonner.
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     NavHost(…)
 *     Toaster()
 * }
 * ```
 */
@Composable
fun Toaster(
    modifier: Modifier = Modifier,
    maxVisible: Int = 3,
    alignment: Alignment = Alignment.BottomCenter
) {
    val toasts = remember { mutableStateListOf<KToastData>() }

    LaunchedEffect(Unit) {
        KToastManager.flow.collect { toast ->
            toasts.add(0, toast)
            if (toasts.size > maxVisible) toasts.removeLastOrNull()
        }
    }

    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            toasts.forEach { toast ->
                key(toast.id) {
                    ToastAnimatedItem(toast = toast, onClose = { toasts.remove(toast) })
                }
            }
        }
    }
}

@Composable
private fun ToastAnimatedItem(toast: KToastData, onClose: () -> Unit) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(toast.id) {
        delay(toast.durationMs)
        visible = false
        delay(300)
        onClose()
    }

    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
        exit    = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
    ) {
        Toast(
            data    = toast,
            onClose = { onClose() }
        )
    }
}