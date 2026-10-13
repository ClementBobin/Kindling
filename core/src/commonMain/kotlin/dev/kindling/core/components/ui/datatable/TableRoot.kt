package dev.kindling.core.components.ui.datatable

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shadcn/ui-style Table root — mirrors `Table` from `table.tsx`.
 *
 * Wraps content in a rounded border. It fills the available width (so weighted cells work) and scrolls
 * horizontally when that is narrower than [minWidth].
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
    minWidth: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    // Rows lay cells out with weights, which need a bounded width: inside a horizontalScroll they collapse to
    // zero. So the table gets the container's width (at least minWidth) and scrolls only when that is narrower.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val tableWidth = if (constraints.hasBoundedWidth) maxOf(maxWidth, minWidth) else maxOf(minWidth, 480.dp)
        Box(Modifier.horizontalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier
                    .width(tableWidth)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, cs.outline, RoundedCornerShape(8.dp)),
                content = content
            )
        }
    }
}