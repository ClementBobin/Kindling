package dev.kindling.core.components.ui.combobox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes
import dev.kindling.core.theme.kindlingShadowMd

/**
 * The dropdown panel — mirrors `ComboboxContent` (Positioner + Popup).
 */
@Composable
fun ComboboxContent(
    state: ComboboxState,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 240.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = LocalKindlingShapes.current.radiusLg
    DropdownMenu(
        expanded        = state.expanded,
        onDismissRequest = { state.close() },
        modifier        = modifier
            .background(MaterialTheme.colorScheme.surface)
            .kindlingShadowMd(shape)
            .clip(shape)
            .widthIn(min = 180.dp)
    ) {
        Column(modifier = Modifier.heightIn(max = maxHeight), content = content)
    }
}

/**
 * Scrollable list container inside [ComboboxContent].
 */
@Composable
fun ComboboxList(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(4.dp), content = content)
}

/**
 * Empty-state slot shown when no items match the query.
 */
@Composable
fun ComboboxEmpty(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier        = modifier.fillMaxWidth().padding(12.dp),
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

/**
 * Groups items under a labelled section.
 */
@Composable
fun ComboboxGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, content = content)
}

/** Group label — mirrors `ComboboxLabel` (GroupLabel). */
@Composable
fun ComboboxLabel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
        ProvideTextStyle(
            MaterialTheme.typography.labelSmall.copy(
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) { content() }
    }
}

/** Thin horizontal rule between groups. */
@Composable
fun ComboboxSeparator(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier  = modifier.padding(vertical = 4.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant
    )
}

/**
 * A single selectable row inside [ComboboxList].
 */
@Composable
fun ComboboxItem(
    state: ComboboxState,
    item: KComboboxItem,
    modifier: Modifier = Modifier,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val cs         = MaterialTheme.colorScheme
    val isSelected = state.isSelected(item)
    val shape = LocalKindlingShapes.current.radiusMd

    DropdownMenuItem(
        onClick  = { state.toggle(item) },
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) cs.primary.copy(.08f) else Color.Transparent),
        text     = {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (content != null) {
                        Row(content = content)
                    } else {
                        Text(
                            text       = item.label,
                            fontSize   = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color      = cs.onSurface
                        )
                    }
                }
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier           = Modifier.padding(start = 8.dp).size(16.dp),
                        tint               = cs.primary
                    )
                }
            }
        }
    )
}

/**
 * Renders all [items] as [ComboboxItem]s, filtered by [state.query].
 */
@Composable
fun ComboboxCollection(
    state: ComboboxState,
    items: List<KComboboxItem>,
    modifier: Modifier = Modifier,
    groups: List<KComboboxGroup> = emptyList(),
    customItem: (@Composable RowScope.(KComboboxItem) -> Unit)? = null
) {
    val filtered = remember(state.query, items) {
        if (state.query.isBlank()) items
        else items.filter { it.label.contains(state.query, ignoreCase = true) }
    }

    Column(modifier = modifier) {
        if (groups.isNotEmpty()) {
            groups.forEachIndexed { i, group ->
                val groupItems = filtered.filter { it.group == group.key }
                if (groupItems.isEmpty()) return@forEachIndexed
                if (i > 0) ComboboxSeparator()
                ComboboxGroup {
                    ComboboxLabel { Text(group.label) }
                    groupItems.forEach { item ->
                        ComboboxItem(state = state, item = item) {
                            customItem?.invoke(this, item)
                        }
                    }
                }
            }
            val ungrouped = filtered.filter { it.group == null }
            if (ungrouped.isNotEmpty()) {
                if (groups.isNotEmpty()) ComboboxSeparator()
                ungrouped.forEach { item ->
                    ComboboxItem(state = state, item = item) {
                        customItem?.invoke(this, item)
                    }
                }
            }
        } else {
            filtered.forEach { item ->
                ComboboxItem(state = state, item = item) {
                    customItem?.invoke(this, item)
                }
            }
        }
    }
}