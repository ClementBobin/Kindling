package dev.kindling.core.components.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Shadcn/ui-style Table root — mirrors `Table` from `table.tsx`.
 *
 * Wraps content in a horizontally-scrollable container with a rounded border.
 * Respects [androidx.compose.ui.platform.LocalLayoutDirection] for RTL.
 *
 * ```kotlin
 * Table {
 *     TableHeader {
 *         TableRow {
 *             TableHead { Text("Name") }
 *             TableHead { Text("Amount") }
 *         }
 *     }
 *     TableBody {
 *         rows.forEach { row ->
 *             TableRow {
 *                 TableCell { Text(row.name) }
 *                 TableCell { Text(row.amount) }
 *             }
 *         }
 *     }
 *     TableFooter {
 *         TableRow { TableHead(colSpan = 2) { Text("Total: $100") } }
 *     }
 *     TableCaption { Text("Payment history") }
 * }
 * ```
 */
@Composable
fun Table(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, cs.outline, RoundedCornerShape(8.dp)),
            content = content
        )
    }
}