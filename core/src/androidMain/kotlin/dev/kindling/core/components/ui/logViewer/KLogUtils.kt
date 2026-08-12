package dev.kindling.core.components.ui.logViewer

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.kindling.android.natif.ClipboardHelper

@Composable
fun rememberResolvedClipboardHelper(clipboardHelper: ClipboardHelper?): ClipboardHelper {
    val context = LocalContext.current
    return remember(clipboardHelper, context) {
        clipboardHelper ?: ClipboardHelper(context)
    }
}

@Composable
fun rememberAutoScroll(listState: LazyListState, itemCount: Int, enabled: Boolean, pauseCondition: Boolean = false) {
    LaunchedEffect(itemCount, pauseCondition) {
        if (enabled && !pauseCondition && itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }
}