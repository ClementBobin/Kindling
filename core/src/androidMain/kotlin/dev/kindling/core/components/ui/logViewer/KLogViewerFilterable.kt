package dev.kindling.core.components.ui.logViewer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kindling.android.natif.ClipboardHelper
import kotlin.collections.get

@Composable
fun KLogViewerFilterable(
    entries: List<KLogEntry>,
    modifier: Modifier = Modifier,
    title: String = "Logs",
    maxHeight: Dp = KLogViewerDefaults.MaxHeight,
    timestamps: Boolean = true,
    autoScroll: Boolean = true,
    levels: List<KLogLevel> = listOf(KLogLevel.ERROR, KLogLevel.WARN, KLogLevel.INFO, KLogLevel.DEBUG),
    clipboardHelper: ClipboardHelper? = null,
    onClear: (() -> Unit)? = null
) {
    var activeLevels by remember { mutableStateOf(levels.toSet()) }
    var searchQuery by remember { mutableStateOf("") }
    
    val resolvedClipboardHelper = rememberResolvedClipboardHelper(clipboardHelper)
    val listState = rememberLazyListState()

    val levelCounts = remember(entries) {
        entries.groupingBy { it.level }.eachCount()
    }

    val filteredEntries = remember(entries, activeLevels, searchQuery) {
        entries.filter { entry ->
            if (!activeLevels.contains(entry.level)) return@filter false
            if (searchQuery.isNotBlank() && !entry.message.contains(searchQuery, ignoreCase = true)) return@filter false
            true
        }
    }

    rememberAutoScroll(listState, filteredEntries.size, autoScroll)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            KLogHeader(
                title = title,
                icon = Icons.Default.FilterList,
                entryCountText = "${filteredEntries.size} / ${entries.size}",
                onCopy = {
                    val text = filteredEntries.joinToString("\n") {
                        "[${
                            formatTimestamp(
                                it.timestamp
                            )
                        }] [${LEVEL_LABELS[it.level]}] ${it.message}"
                    }
                    resolvedClipboardHelper.copy(text)
                },
                onClear = onClear
            )

            // Filter Chips Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                levels.forEach { level ->
                    val isActive = activeLevels.contains(level)
                    val count = levelCounts[level] ?: 0
                    val color =
                        getLevelColor(
                            level
                        )

                    FilterChip(
                        selected = isActive,
                        onClick = {
                            activeLevels = if (isActive) activeLevels - level else activeLevels + level
                        },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) color else color.copy(alpha = 0.3f))
                                )
                                Text(LEVEL_LABELS[level] ?: "")
                                if (count > 0) {
                                    Text("($count)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
            ) {
                if (filteredEntries.isEmpty()) {
                    KLogEmptyState(
                        message = "No matching log entries.",
                        onResetFilters = if (searchQuery.isNotBlank() || activeLevels.size < levels.size) {
                            {
                                searchQuery = ""
                                activeLevels = levels.toSet()
                            }
                        } else null
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        itemsIndexed(filteredEntries) { _, entry ->
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
        }
    }
}