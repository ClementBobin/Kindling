package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Convenience data-table built on top of the primitive Table slots.
 *
 * For full structural control, use [Table] / [TableHeader] / [TableBody] directly.
 *
 * ```kotlin
 * val columns = listOf(
 *     KTableColumn<Payment>("email",  "Email",  sortable = true) { Text(it.email) },
 *     KTableColumn<Payment>("amount", "Amount") { Text("$${it.amount}") },
 * )
 * KDataTable(columns = columns, data = payments)
 * ```
 */
@Composable
fun <T> KDataTable(
    columns: List<KTableColumn<T>>,
    data: List<T>,
    modifier: Modifier = Modifier,
    striped: Boolean = false,
    pageSize: Int = Int.MAX_VALUE,
    onSort: ((key: String, direction: KSortDirection) -> Unit)? = null,
    emptyContent: @Composable () -> Unit = {
        Box(
            modifier         = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No results.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
) {
    var sortKey by remember { mutableStateOf<String?>(null) }
    var sortDir by remember { mutableStateOf(KSortDirection.None) }
    var page    by remember { mutableStateOf(1) }

    val totalPages  = if (pageSize == Int.MAX_VALUE) 1 else maxOf(1, (data.size + pageSize - 1) / pageSize)
    val displayData = if (pageSize == Int.MAX_VALUE) data
    else data.drop((page - 1) * pageSize).take(pageSize)

    Column(modifier = modifier) {
        Table {
            // Header
            TableHeader {
                TableRow {
                    columns.forEach { col ->
                        TableHead(
                            weight        = col.weight,
                            align         = col.align,
                            sortable      = col.sortable,
                            sortDirection = if (sortKey == col.key) sortDir else KSortDirection.None,
                            onSort        = if (col.sortable && onSort != null) {
                                {
                                    val newDir = when {
                                        sortKey != col.key          -> KSortDirection.Asc
                                        sortDir == KSortDirection.Asc -> KSortDirection.Desc
                                        else                        -> KSortDirection.None
                                    }
                                    sortKey = if (newDir == KSortDirection.None) null else col.key
                                    sortDir = newDir
                                    onSort(col.key, newDir)
                                }
                            } else null
                        ) { Text(col.header, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    }
                }
            }

            // Body
            TableBody {
                if (displayData.isEmpty()) {
                    emptyContent()
                } else {
                    displayData.forEachIndexed { i, row ->
                        if (i > 0) HorizontalDivider(
                            color     = MaterialTheme.colorScheme.outline.copy(.5f),
                            thickness = 1.dp
                        )
                        TableRow(striped = striped && i % 2 == 1) {
                            columns.forEach { col ->
                                TableCell(weight = col.weight, align = col.align) { col.cell(this, row) }
                            }
                        }
                    }
                }
            }
        }

        // Pagination
        if (totalPages > 1) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Page $page of $totalPages",
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Pagination {
                    PaginationContent {
                        PaginationItem {
                            PaginationPrevious(
                                onClick = { page-- },
                                enabled = page > 1,
                                text    = "Previous"
                            )
                        }
                        // Window of pages
                        val range = maxOf(1, page - 1)..minOf(totalPages, page + 1)
                        if (range.first > 1) {
                            PaginationItem { PaginationLink(1, isActive = false, onClick = { page = 1 }) }
                            if (range.first > 2) PaginationItem { PaginationEllipsis() }
                        }
                        range.forEach { p ->
                            PaginationItem {
                                PaginationLink(p, isActive = p == page, onClick = { page = p })
                            }
                        }
                        if (range.last < totalPages) {
                            if (range.last < totalPages - 1) PaginationItem { PaginationEllipsis() }
                            PaginationItem { PaginationLink(totalPages, isActive = false, onClick = { page = totalPages }) }
                        }
                        PaginationItem {
                            PaginationNext(
                                onClick = { page++ },
                                enabled = page < totalPages,
                                text    = "Next"
                            )
                        }
                    }
                }
            }
        }
    }
}