package dev.kindling.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shadcn/ui-style Pagination.
 *
 * ```kotlin
 * var page by remember { mutableStateOf(1) }
 * KPagination(currentPage = page, totalPages = 20, onPageChange = { page = it })
 * ```
 */
@Composable
fun KPagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    siblingCount: Int = 1,
    showEdges: Boolean = true
) {
    if (totalPages <= 1) return

    val pages = buildPageList(currentPage, totalPages, siblingCount, showEdges)

    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        PaginationArrow(
            label   = "Previous",
            icon    = { Icon(Icons.Default.KeyboardArrowLeft, "Previous", Modifier.size(16.dp)) },
            enabled = currentPage > 1,
            onClick = { onPageChange(currentPage - 1) }
        )

        pages.forEach { item ->
            when (item) {
                is PageItem.Number  -> PaginationItem(item.page, item.page == currentPage) { onPageChange(item.page) }
                is PageItem.Ellipsis -> PaginationEllipsis()
            }
        }

        PaginationArrow(
            label   = "Next",
            icon    = { Icon(Icons.Default.KeyboardArrowRight, "Next", Modifier.size(16.dp)) },
            enabled = currentPage < totalPages,
            onClick = { onPageChange(currentPage + 1) }
        )
    }
}

@Composable
private fun PaginationItem(page: Int, isActive: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick      = onClick,
        shape        = RoundedCornerShape(6.dp),
        color        = if (isActive) cs.primary else Color.Transparent,
        contentColor = if (isActive) cs.onPrimary else cs.onBackground,
        modifier     = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(page.toString(), fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal)
        }
    }
}

@Composable
private fun PaginationArrow(label: String, icon: @Composable () -> Unit, enabled: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick      = onClick,
        enabled      = enabled,
        shape        = RoundedCornerShape(6.dp),
        color        = Color.Transparent,
        contentColor = if (enabled) cs.onBackground else cs.onSurface.copy(alpha = 0.38f),
        modifier     = Modifier.height(36.dp).padding(horizontal = 2.dp)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (label == "Previous") { icon(); Text(label, fontSize = 14.sp) }
            else                     { Text(label, fontSize = 14.sp); icon() }
        }
    }
}

@Composable
private fun PaginationEllipsis() {
    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
        Text("…", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── page list builder ───────────────────────

private sealed interface PageItem {
    data class Number(val page: Int) : PageItem
    object Ellipsis : PageItem
}

private fun buildPageList(current: Int, total: Int, siblings: Int, showEdges: Boolean): List<PageItem> {
    val result       = mutableListOf<PageItem>()
    val leftSibling  = maxOf(1, current - siblings)
    val rightSibling = minOf(total, current + siblings)
    val showLeft     = showEdges && leftSibling > 2
    val showRight    = showEdges && rightSibling < total - 1

    if (showEdges) {
        result += PageItem.Number(1)
        if (showLeft) result += PageItem.Ellipsis
    }
    for (p in leftSibling..rightSibling) {
        if (showEdges && (p == 1 || p == total)) continue
        result += PageItem.Number(p)
    }
    if (showEdges) {
        if (showRight) result += PageItem.Ellipsis
        if (total > 1) result += PageItem.Number(total)
    }
    return result
}
