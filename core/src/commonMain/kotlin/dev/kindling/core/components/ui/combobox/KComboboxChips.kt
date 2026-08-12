package dev.kindling.core.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes

/**
 * Chip container shown inside the trigger area for multiple-selection mode.
 */
@Composable
fun ComboboxChips(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = LocalKindlingShapes.current.radiusLg
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cs.outline, shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content               = content
    )
}

/**
 * Individual chip rendered inside [ComboboxChips] for each selected item.
 */
@Composable
fun ComboboxChip(
    state: ComboboxState,
    item: KComboboxItem,
    modifier: Modifier = Modifier,
    showRemove: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    val shape = LocalKindlingShapes.current.radiusSm
    Row(
        modifier = modifier
            .clip(shape)
            .background(cs.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(item.label, fontSize = 12.sp, color = cs.onSurfaceVariant)
        if (showRemove) {
            KButton(
                onClick = { state.removeChip(item) },
                variant = KButtonVariant.Ghost,
                size    = KButtonSize.IconXs,
                modifier = Modifier.size(14.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove ${item.label}",
                    modifier           = Modifier.size(10.dp)
                )
            }
        }
    }
}

/**
 * Inline search input rendered alongside chips inside [ComboboxChips].
 */
@Composable
fun ComboboxChipsInput(
    state: ComboboxState,
    modifier: Modifier = Modifier,
    placeholder: String = "Search…",
    enabled: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    androidx.compose.foundation.text.BasicTextField(
        value         = state.query,
        onValueChange = { state.query = it; state.expanded = true },
        enabled       = enabled,
        singleLine    = true,
        textStyle     = LocalTextStyle.current.copy(fontSize = 13.sp, color = cs.onBackground),
        modifier      = modifier
            .widthIn(min = 64.dp),
        decorationBox = { inner ->
            if (state.query.isEmpty()) {
                Text(placeholder, fontSize = 13.sp, color = cs.onSurface.copy(.5f))
            }
            inner()
        }
    )
}