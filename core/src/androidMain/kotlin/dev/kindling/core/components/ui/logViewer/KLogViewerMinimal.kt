package dev.kindling.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun KLogViewerMinimal(
    entries: List<KLogEntry>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = KLogViewerDefaults.MinimalMaxHeight,
    timestamps: Boolean = false,
    autoScroll: Boolean = true
) {
    val listState = rememberLazyListState()
    rememberAutoScroll(listState, entries.size, autoScroll)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(entries) { _, entry ->
                KLogRowContent(
                    entry = entry,
                    index = null,
                    timestamps = timestamps,
                    showIndicatorDot = true
                )
            }
        }
    }
}