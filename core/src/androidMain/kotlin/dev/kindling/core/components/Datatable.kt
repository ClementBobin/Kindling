package dev.kindling.core.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Table (root container)
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Table root — mirrors `Table` from `table.tsx`.
 *
 * Wraps content in a horizontally-scrollable container with a rounded border.
 * Respects [LocalLayoutDirection] for RTL.
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

// ─────────────────────────────────────────────
//  TableHeader
// ─────────────────────────────────────────────

/**
 * Wraps header [TableRow]s — mirrors `TableHeader` (`<thead>`).
 */
@Composable
fun TableHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        content  = content
    )
    HorizontalDivider(color = cs.outline, thickness = 1.dp)
}

// ─────────────────────────────────────────────
//  TableBody
// ─────────────────────────────────────────────

/**
 * Wraps body [TableRow]s — mirrors `TableBody` (`<tbody>`).
 *
 * Renders rows lazily for large datasets when [lazy] = true (default false).
 * For large lists, prefer the [KDataTable] preset which handles pagination.
 */
@Composable
fun TableBody(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth(), content = content)
}

// ─────────────────────────────────────────────
//  TableFooter
// ─────────────────────────────────────────────

/**
 * Footer section — mirrors `TableFooter` (`<tfoot>`).
 *
 * Rendered with a muted background and top border.
 */
@Composable
fun TableFooter(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    HorizontalDivider(color = cs.outline, thickness = 1.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(cs.surfaceVariant.copy(.5f)),
        content = content
    )
}

// ─────────────────────────────────────────────
//  TableRow
// ─────────────────────────────────────────────

/**
 * A table row — mirrors `TableRow` (`<tr>`).
 *
 * @param selected  Applies selected-row background (muted).
 * @param onClick   Makes the row clickable when non-null.
 * @param striped   Applies alternate-row background (pass `index % 2 == 1`).
 */
@Composable
fun TableRow(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    striped: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val bg = when {
        selected -> cs.surfaceVariant.copy(.6f)
        striped  -> cs.surfaceVariant.copy(.3f)
        else     -> Color.Transparent
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content           = content
    )
}

// ─────────────────────────────────────────────
//  TableHead
// ─────────────────────────────────────────────

/**
 * Header cell — mirrors `TableHead` (`<th>`).
 *
 * @param weight   Flex weight of the column.
 * @param align    Text alignment override.
 * @param sortable Whether to show a sort indicator icon.
 * @param sortDirection Current sort direction when this column is sorted.
 * @param onSort   Called when the header is tapped (only when [sortable]).
 */
@Composable
fun RowScope.TableHead(
    modifier: Modifier = Modifier,
    weight: Float = 1f,
    align: TextAlign = TextAlign.Start,
    sortable: Boolean = false,
    sortDirection: KSortDirection = KSortDirection.None,
    onSort: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .weight(weight)
            .then(
                if (sortable && onSort != null) Modifier.clickable(onClick = onSort) else Modifier
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = when (align) {
            TextAlign.End    -> Arrangement.End
            TextAlign.Center -> Arrangement.Center
            else             -> Arrangement.Start
        }
    ) {
        ProvideTextStyle(
            MaterialTheme.typography.labelMedium.copy(
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = cs.onSurfaceVariant
            )
        ) { content() }

        if (sortable && onSort != null) {
            Spacer(Modifier.width(4.dp))
            Text(
                text  = when (sortDirection) {
                    KSortDirection.Asc  -> "↑"
                    KSortDirection.Desc -> "↓"
                    KSortDirection.None -> "↕"
                },
                fontSize = 11.sp,
                color    = if (sortDirection != KSortDirection.None) cs.primary
                else cs.onSurface.copy(.38f)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  TableCell
// ─────────────────────────────────────────────

/**
 * Body / footer cell — mirrors `TableCell` (`<td>`).
 *
 * @param weight Flex weight — must match the corresponding [TableHead] weight.
 * @param align  Text alignment override.
 */
@Composable
fun RowScope.TableCell(
    modifier: Modifier = Modifier,
    weight: Float = 1f,
    align: TextAlign = TextAlign.Start,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.weight(weight),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = when (align) {
            TextAlign.End    -> Arrangement.End
            TextAlign.Center -> Arrangement.Center
            else             -> Arrangement.Start
        },
        content = content
    )
}

// ─────────────────────────────────────────────
//  TableCaption
// ─────────────────────────────────────────────

/**
 * Caption rendered below the table — mirrors `TableCaption` (`<caption>`).
 */
@Composable
fun TableCaption(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier         = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        ProvideTextStyle(
            MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) { content() }
    }
}

// ─────────────────────────────────────────────
//  KTableColumn helper (for KDataTable preset)
// ─────────────────────────────────────────────

/**
 * Describes one column in a [KDataTable] preset.
 *
 * This is the data-driven convenience layer on top of the
 * primitive [Table] / [TableHead] / [TableCell] slots.
 */
data class KTableColumn<T>(
    val key: String,
    val header: String,
    val weight: Float = 1f,
    val sortable: Boolean = false,
    val align: TextAlign = TextAlign.Start,
    val cell: @Composable RowScope.(row: T) -> Unit
)

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
    var sortKey by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var sortDir by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(KSortDirection.None) }
    var page    by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(1) }

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