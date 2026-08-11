package dev.kindling.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ─── Data & Types ────────────────────────────────────────────────────────────

enum class KLogLevel {
    INFO, WARN, ERROR, DEBUG, VERBOSE
}

data class KLogEntry(
    val level: KLogLevel,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

object KLogViewerDefaults {
    val MaxHeight: Dp = 400.dp
    val MinimalMaxHeight: Dp = 300.dp
}

private val LEVEL_LABELS = mapOf(
    KLogLevel.ERROR to "ERR",
    KLogLevel.WARN to "WRN",
    KLogLevel.INFO to "INF",
    KLogLevel.DEBUG to "DBG",
    KLogLevel.VERBOSE to "VRB"
)

@Composable
private fun getLevelColor(level: KLogLevel): Color {
    return when (level) {
        KLogLevel.ERROR -> Color(0xFFF43F5E) // Rose
        KLogLevel.WARN -> Color(0xFFF59E0B)  // Amber
        KLogLevel.INFO -> Color(0xFF0EA5E9)  // Sky
        KLogLevel.DEBUG -> Color(0xFF8B5CF6) // Violet
        KLogLevel.VERBOSE -> Color(0xFF71717A) // Zinc
    }
}

private fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    return sdf.format(Date(ts))
}

// ─── Component 1: Terminal Log Viewer ────────────────────────────────────────

/**
 * Full CLI-style interface with toolbar, line numbers, search filtering, and timestamps.
 */
@Composable
fun KLogViewerTerminal(
    entries: List<KLogEntry>,
    modifier: Modifier = Modifier,
    title: String = "Logs",
    maxHeight: Dp = KLogViewerDefaults.MaxHeight,
    lineNumbers: Boolean = true,
    timestamps: Boolean = true,
    autoScroll: Boolean = true,
    onClear: (() -> Unit)? = null
) {
    var paused by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val filteredEntries = remember(entries, searchQuery) {
        if (searchQuery.isBlank()) entries
        else entries.filter { it.message.contains(searchQuery, ignoreCase = true) }
    }

    // Auto-scroll effect
    LaunchedEffect(filteredEntries.size, paused) {
        if (autoScroll && !paused && filteredEntries.isNotEmpty()) {
            listState.animateScrollToItem(filteredEntries.size - 1)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${filteredEntries.size}${if (searchQuery.isNotBlank()) " / ${entries.size}" else ""} lines",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = {
                            searchOpen = !searchOpen
                            if (!searchOpen) searchQuery = ""
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp))
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
                    IconButton(
                        onClick = {
                            val text = entries.joinToString("\n") { "[${formatTimestamp(it.timestamp)}] [${LEVEL_LABELS[it.level]}] ${it.message}" }
                            clipboardManager.setText(AnnotatedString(text))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy all", modifier = Modifier.size(16.dp))
                    }
                    if (onClear != null) {
                        IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Search Bar
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

            // Log Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
            ) {
                if (filteredEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching log entries." else "No log entries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        itemsIndexed(filteredEntries) { index, entry ->
                            val levelColor = getLevelColor(entry.level)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                if (lineNumbers) {
                                    Text(
                                        text = "${index + 1}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.width(32.dp)
                                    )
                                }
                                if (timestamps) {
                                    Text(
                                        text = formatTimestamp(entry.timestamp),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = LEVEL_LABELS[entry.level] ?: "INF",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = levelColor,
                                    modifier = Modifier.width(28.dp)
                                )
                                Text(
                                    text = entry.message,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Component 2: Minimal Log Viewer ─────────────────────────────────────────

/**
 * Simple scrolling log lines for compact contexts without toolbars.
 */
@Composable
fun KLogViewerMinimal(
    entries: List<KLogEntry>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = KLogViewerDefaults.MinimalMaxHeight,
    timestamps: Boolean = false,
    autoScroll: Boolean = true
) {
    val listState = rememberLazyListState()

    LaunchedEffect(entries.size) {
        if (autoScroll && entries.isNotEmpty()) {
            listState.animateScrollToItem(entries.size - 1)
        }
    }

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
                val levelColor = getLevelColor(entry.level)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(levelColor)
                    )
                    if (timestamps) {
                        Text(
                            text = formatTimestamp(entry.timestamp),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = entry.message,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─── Component 3: Filterable Log Viewer ──────────────────────────────────────

/**
 * Includes level filtering chips (info/warn/error/debug) and inline search capabilities.
 */
@Composable
fun KLogViewerFilterable(
    entries: List<KLogEntry>,
    modifier: Modifier = Modifier,
    title: String = "Logs",
    maxHeight: Dp = KLogViewerDefaults.MaxHeight,
    timestamps: Boolean = true,
    autoScroll: Boolean = true,
    levels: List<KLogLevel> = listOf(KLogLevel.ERROR, KLogLevel.WARN, KLogLevel.INFO, KLogLevel.DEBUG),
    onClear: (() -> Unit)? = null
) {
    var activeLevels by remember { mutableStateOf(levels.toSet()) }
    var searchQuery by remember { mutableStateOf("") }
    
    val clipboardManager = LocalClipboardManager.current
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

    LaunchedEffect(filteredEntries.size) {
        if (autoScroll && filteredEntries.isNotEmpty()) {
            listState.animateScrollToItem(filteredEntries.size - 1)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${filteredEntries.size} / ${entries.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = {
                            val text = filteredEntries.joinToString("\n") { "[${formatTimestamp(it.timestamp)}] [${LEVEL_LABELS[it.level]}] ${it.message}" }
                            clipboardManager.setText(AnnotatedString(text))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy filtered", modifier = Modifier.size(16.dp))
                    }
                    if (onClear != null) {
                        IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Filter Bar
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
                    val color = getLevelColor(level)

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

            // Log Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
            ) {
                if (filteredEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "No matching log entries.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (searchQuery.isNotBlank() || activeLevels.size < levels.size) {
                                TextButton(onClick = {
                                    searchQuery = ""
                                    activeLevels = levels.toSet()
                                }) {
                                    Text("Reset filters", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        itemsIndexed(filteredEntries) { _, entry ->
                            val levelColor = getLevelColor(entry.level)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(levelColor)
                                )
                                if (timestamps) {
                                    Text(
                                        text = formatTimestamp(entry.timestamp),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = LEVEL_LABELS[entry.level] ?: "INF",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = levelColor,
                                    modifier = Modifier.width(28.dp)
                                )
                                Text(
                                    text = entry.message,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}