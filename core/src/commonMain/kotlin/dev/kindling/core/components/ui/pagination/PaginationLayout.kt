package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun PaginationContent(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        content               = content
    )
}

@Composable
fun PaginationItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) { content() }
}

/**
 * A single page link — active page uses [KButtonVariant.Outline].
 */
@Composable
fun PaginationLink(
    page: Int,
    isActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    KButton(
        onClick  = onClick,
        modifier = modifier,
        variant  = if (isActive) KButtonVariant.Outline else KButtonVariant.Ghost,
        size     = KButtonSize.Icon
    ) {
        Text(page.toString())
    }
}

/**
 * Previous-page button.
 * Arrow direction is automatically mirrored in RTL via [LocalLayoutDirection].
 */
@Composable
fun PaginationPrevious(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "Previous"
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    KButton(
        onClick  = onClick,
        modifier = modifier,
        variant  = KButtonVariant.Ghost,
        size     = KButtonSize.Default,
        enabled  = enabled
    ) {
        Icon(
            imageVector        = if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowRight
            else     Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Previous",
            modifier           = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(text)
    }
}

/**
 * Next-page button.
 * Arrow direction is automatically mirrored in RTL.
 */
@Composable
fun PaginationNext(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "Next"
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    KButton(
        onClick  = onClick,
        modifier = modifier,
        variant  = KButtonVariant.Ghost,
        size     = KButtonSize.Default,
        enabled  = enabled
    ) {
        Text(text)
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector        = if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft
            else     Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Next",
            modifier           = Modifier.size(16.dp)
        )
    }
}

@Composable
fun PaginationEllipsis(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Default.MoreHoriz,
            contentDescription = "More pages",
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(16.dp)
        )
    }
}