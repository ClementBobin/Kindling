package dev.kindling.core.components.ui.pagination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection

/**
 * Root pagination navigation wrapper — mirrors `Pagination` from `pagination.tsx`.
 *
 * Used to navigate through large collections of content that are split across multiple pages.
 * It provides a horizontal row for [PaginationLink], [PaginationPrevious], and [PaginationNext].
 *
 * Respects [LocalLayoutDirection] for RTL via Compose.
 *
 * ### Example usage:
 * ```kotlin
 * Pagination {
 *     PaginationContent {
 *         PaginationItem { 
 *             PaginationPrevious(onClick = { /* Previous */ }, enabled = currentPage > 1) 
 *         }
 *         
 *         repeat(totalPageCount) { page ->
 *             PaginationItem {
 *                 PaginationLink(
 *                     page = page + 1,
 *                     isActive = (page + 1) == currentPage,
 *                     onClick = { /* Go to page */ }
 *                 )
 *             }
 *         }
 *         
 *         PaginationItem { 
 *             PaginationNext(onClick = { /* Next */ }, enabled = currentPage < totalPageCount) 
 *         }
 *     }
 * }
 * ```
 * 
 * @param modifier The modifier to be applied to the pagination row.
 * @param content The structured pagination items.
 */
@Composable
fun Pagination(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
        content               = content
    )
}