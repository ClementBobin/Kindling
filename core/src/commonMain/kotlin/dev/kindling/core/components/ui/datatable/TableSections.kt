package dev.kindling.core.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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