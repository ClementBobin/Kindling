package dev.kindling.core.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import dev.kindling.core.components.internal.KindlingPreviewSurface
import dev.kindling.core.components.internal.PreviewLabel

/**
 * Describe a column in a data table.
 *
 * @property key Unique key used for sorting.
 * @property header Column header label.
 * @property weight Flex weight for the column width.
 * @property sortable Whether the header shows a sort toggle.
 * @property cell Composable that renders a single cell.
 */
data class KTableColumn<T>(
    val key: String,
    val header: String,
    val weight: Float = 1f,
    val sortable: Boolean = false,
    val cell: @Composable RowScope.(row: T) -> Unit
)

/**
 * Render a shadcn/ui-style data table with optional sorting and pagination.
 *
 * ```kotlin
 * val columns = listOf(
 *     KTableColumn<Payment>("email", "Email", sortable = true) { Text(it.email) },
 *     KTableColumn<Payment>("amount","Amount") { Text("$${it.amount}") },
 * )
 * KDataTable(columns = columns, data = payments, onSort = { key, dir -> /* re-sort */ })
 * ```
 *
 * @param columns Column descriptors for the table.
 * @param data Row data displayed in the table.
 * @param modifier Applied to the outermost layout element.
 * @param pageSize Number of rows per page, or [Int.MAX_VALUE] to disable pagination.
 * @param striped When `true`, alternates row background colours.
 * @param onSort Optional callback invoked when a column sort is toggled.
 * @param emptyContent Composable shown when [data] is empty.
 */
@Composable
fun <T> KDataTable(
    columns: List<KTableColumn<T>>,
    data: List<T>,
    modifier: Modifier = Modifier,
    pageSize: Int = Int.MAX_VALUE,
    striped: Boolean = false,
    onSort: ((key: String, direction: KSortDirection) -> Unit)? = null,
    emptyContent: @Composable () -> Unit = {
        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No results.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
) {
    val cs = MaterialTheme.colorScheme
    var sortKey by remember { mutableStateOf<String?>(null) }
    var sortDir by remember { mutableStateOf(KSortDirection.None) }
    var page    by remember { mutableStateOf(1) }

    val totalPages  = if (pageSize == Int.MAX_VALUE) 1 else maxOf(1, (data.size + pageSize - 1) / pageSize)
    val displayData = if (pageSize == Int.MAX_VALUE) data else data.drop((page - 1) * pageSize).take(pageSize)

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(cs.surface).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                columns.forEach { col ->
                    Row(
                        modifier = Modifier.weight(col.weight).then(
                            if (col.sortable && onSort != null) Modifier.clickable {
                                val newDir = when {
                                    sortKey != col.key          -> KSortDirection.Asc
                                    sortDir == KSortDirection.Asc -> KSortDirection.Desc
                                    else                        -> KSortDirection.None
                                }
                                sortKey = if (newDir == KSortDirection.None) null else col.key
                                sortDir = newDir
                                onSort(col.key, newDir)
                            } else Modifier
                        ),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(col.header, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (col.sortable && onSort != null) {
                            Icon(
                                imageVector = when {
                                    sortKey == col.key && sortDir == KSortDirection.Asc  -> Icons.Default.KeyboardArrowUp
                                    sortKey == col.key && sortDir == KSortDirection.Desc -> Icons.Default.KeyboardArrowDown
                                    else -> Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint     = if (sortKey == col.key) cs.primary else cs.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = cs.outline, thickness = 1.dp)

            if (displayData.isEmpty()) {
                emptyContent()
            } else {
                LazyColumn {
                    itemsIndexed(displayData) { i, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (striped && i % 2 == 1) cs.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            columns.forEach { col ->
                                Row(modifier = Modifier.weight(col.weight)) { col.cell(this, row) }
                            }
                        }
                        if (i < displayData.lastIndex)
                            HorizontalDivider(color = cs.outline.copy(alpha = 0.5f), thickness = 1.dp)
                    }
                }
            }
        }

        if (totalPages > 1) {
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Page $page of $totalPages", fontSize = 13.sp, color = cs.onSurfaceVariant)
                KPagination(currentPage = page, totalPages = totalPages, onPageChange = { page = it }, siblingCount = 1)
            }
        }
    }
}

private data class PreviewPayment(
    val email: String,
    val amount: String
)

@Preview(name = "KDataTable — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KDataTable — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKDataTable() {
    val columns = listOf(
        KTableColumn<PreviewPayment>("email", "Email", sortable = true) { Text(it.email) },
        KTableColumn<PreviewPayment>("amount", "Amount") { Text(it.amount) }
    )
    val data = listOf(
        PreviewPayment("arya@kindling.dev", "$120.00"),
        PreviewPayment("jon@kindling.dev", "$75.50")
    )

    KindlingPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PreviewLabel("Default")
            KDataTable(columns = columns, data = data, onSort = { _, _ -> })

            PreviewLabel("Striped")
            KDataTable(columns = columns, data = data, striped = true, onSort = { _, _ -> })

            PreviewLabel("Empty state")
            KDataTable(columns = columns, data = emptyList(), onSort = { _, _ -> })
        }
    }
}
