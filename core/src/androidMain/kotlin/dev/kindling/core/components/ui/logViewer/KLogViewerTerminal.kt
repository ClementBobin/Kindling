package dev.kindling.core.components.ui.logViewer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kindling.android.natif.ClipboardHelper

@Composable
fun KLogViewerTerminal(
    entries: List<KLogEntry>,
    modifier: Modifier = Modifier,
    title: String = "Logs",
    maxHeight: Dp = KLogViewerDefaults.MaxHeight,
    lineNumbers: Boolean = true,
    timestamps: Boolean = true,
    autoScroll: Boolean = true,
    clipboardHelper: ClipboardHelper? = null,
    onClear: (() -> Unit)? = null
) {
    var paused by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val resolvedClipboardHelper = rememberResolvedClipboardHelper(clipboardHelper)
    val listState = rememberLazyListState()

    val filteredEntries = remember(entries, searchQuery) {
        if (searchQuery.isBlank()) entries
        else entries.filter { it.message.contains(searchQuery, ignoreCase = true) }
    }

    rememberAutoScroll(listState, filteredEntries.size, autoScroll, paused)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            KLogHeader(
                title = title,
                icon = Icons.Default.Terminal,
                entryCountText = "${filteredEntries.size}${if (searchQuery.isNotBlank()) " / ${entries.size}" else ""} lines",
                onCopy = {
                    val text = entries.joinToString("\n") {
                        "[${
                            formatTimestamp(
                                it.timestamp
                            )
                        }] [${LEVEL_LABELS[it.level]}] ${it.message}"
                    }
                    resolvedClipboardHelper.copy(text)
                },
                onClear = onClear,
                extraActions = {
                    IconButton(
                        onClick = {
                            searchOpen = !searchOpen
                            if (!searchOpen) searchQuery = ""
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { paused = !paused },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (paused) "Resume" else "Pause",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )

            if (searchOpen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter logs...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
            ) {
                if (filteredEntries.isEmpty()) {
                    KLogEmptyState(message = if (searchQuery.isNotBlank()) "No matching log entries." else "No log entries.")
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        itemsIndexed(filteredEntries) { index, entry ->
                            KLogRowContent(
                                entry = entry,
                                index = if (lineNumbers) index + 1 else null,
                                timestamps = timestamps,
                                showIndicatorDot = false
                            )
                        }
                    }
                }
            }
        }
    }
}