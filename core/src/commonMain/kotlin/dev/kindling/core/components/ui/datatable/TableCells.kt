package dev.kindling.core.components.ui.datatable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Header cell — mirrors `TableHead` (`<th>`).
 *
 * @param weight  Flex weight of the column.
 * @param align    Text alignment override.
 * @param sortable Whether to show a sort indicator icon.
 * @param sortDirection Current sort direction when this column is sorted.
 * @param onSort  Called when the header is tapped (only when [sortable]).
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
        verticalAlignment    = Alignment.CenterVertically,
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
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = when (align) {
            TextAlign.End    -> Arrangement.End
            TextAlign.Center -> Arrangement.Center
            else             -> Arrangement.Start
        },
        content = content
    )
}

/**
 * Caption rendered below the table — mirrors `TableCaption` (`<caption>`).
 */
@Composable
fun TableCaption(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier        = modifier
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