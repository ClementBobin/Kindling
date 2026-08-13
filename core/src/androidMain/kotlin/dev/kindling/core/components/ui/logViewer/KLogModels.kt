package dev.kindling.core.components.ui.logViewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class KLogLevel {
    INFO, WARN, ERROR, DEBUG, VERBOSE
}

data class KLogEntry(
    val level: KLogLevel,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

object KLogViewerDefaults {
    val MaxHeight: androidx.compose.ui.unit.Dp = 400.dp
    val MinimalMaxHeight: androidx.compose.ui.unit.Dp = 300.dp
}

val LEVEL_LABELS = mapOf(
    KLogLevel.ERROR to "ERR",
    KLogLevel.WARN to "WRN",
    KLogLevel.INFO to "INF",
    KLogLevel.DEBUG to "DBG",
    KLogLevel.VERBOSE to "VRB"
)

@Composable
fun getLevelColor(level: KLogLevel): Color {
    return when (level) {
        KLogLevel.ERROR -> Color(0xFFF43F5E) // Rose
        KLogLevel.WARN -> Color(0xFFF59E0B)  // Amber
        KLogLevel.INFO -> Color(0xFF0EA5E9)  // Sky
        KLogLevel.DEBUG -> Color(0xFF8B5CF6) // Violet
        KLogLevel.VERBOSE -> Color(0xFF71717A) // Zinc
    }
}

fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    return sdf.format(Date(ts))
}