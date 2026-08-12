package dev.kindling.core.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shadcn/ui-style Combobox root — mirrors `Combobox` (Base UI primitive).
 */
@Composable
fun Combobox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, content = content)
}

/**
 * Renders the current selected label (or [placeholder] if nothing selected).
 */
@Composable
fun ComboboxValue(
    modifier: Modifier = Modifier,
    state: ComboboxState,
    placeholder: String = "Select…"
) {
    val cs = MaterialTheme.colorScheme
    val label = when {
        state.multiple && state.selectedMultiple.isNotEmpty() ->
            state.selectedMultiple.joinToString(", ") { it.label }
        !state.multiple && state.selected != null -> state.selected!!.label
        else -> null
    }
    Text(
        text     = label ?: placeholder,
        fontSize = 14.sp,
        color    = if (label != null) cs.onBackground else cs.onSurface.copy(.5f),
        modifier = modifier
    )
}

/**
 * Button-style trigger that opens/closes the dropdown.
 */
@Composable
fun ComboboxTrigger(
    state: ComboboxState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    content: @Composable RowScope.() -> Unit = {
        ComboboxValue(state = state)
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector         = if (state.expanded) Icons.Default.KeyboardArrowUp
            else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier           = Modifier.size(16.dp),
            tint               = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick      = { if (enabled) state.expanded = !state.expanded },
        enabled      = enabled,
        shape        = RoundedCornerShape(8.dp),
        color        = Color.Transparent,
        contentColor = if (enabled) cs.onBackground else cs.onBackground.copy(.38f),
        border       = BorderStroke(
            if (isError) 2.dp else 1.dp,
            when {
                !enabled -> cs.outline.copy(.38f)
                isError  -> cs.error
                state.expanded -> cs.primary
                else     -> cs.outline
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp)
    ) {
        Row(
            modifier           = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content           = content
        )
    }
}

/**
 * Search-field trigger — mirrors `ComboboxInput`.
 */
@Composable
fun ComboboxInput(
    state: ComboboxState,
    modifier: Modifier = Modifier,
    placeholder: String = "Search…",
    showTrigger: Boolean = true,
    showClear: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape  = RoundedCornerShape(8.dp),
        color  = Color.Transparent,
        border = BorderStroke(
            if (isError) 2.dp else 1.dp,
            when {
                !enabled    -> cs.outline.copy(.38f)
                isError     -> cs.error
                state.expanded -> cs.primary
                else        -> cs.outline
            }
        ),
        modifier = modifier.fillMaxWidth().heightIn(min = 32.dp)
    ) {
        Row(
            modifier           = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.multiple && state.selectedMultiple.isNotEmpty()) {
                Row(
                    modifier              = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    state.selectedMultiple.forEach { item ->
                        ComboboxChip(state = state, item = item)
                    }
                    ComboboxChipsInput(state = state, placeholder = placeholder, enabled = enabled)
                }
            } else {
                BasicTextField(
                    value         = state.query,
                    onValueChange = { state.query = it; state.expanded = true },
                    enabled       = enabled,
                    singleLine    = true,
                    textStyle     = LocalTextStyle.current.copy(fontSize = 14.sp, color = cs.onBackground),
                    modifier      = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (state.query.isEmpty()) {
                            Text(placeholder, fontSize = 14.sp, color = cs.onSurface.copy(.5f))
                        }
                        inner()
                    }
                )
            }

            if (showClear && (state.selected != null || state.selectedMultiple.isNotEmpty())) {
                KButton(
                    onClick = {
                        state.selected = null; state.selectedMultiple = emptyList()
                        state.onSelect(null); state.onSelectMultiple(emptyList())
                    },
                    variant = KButtonVariant.Ghost, size = KButtonSize.IconXs
                ) { Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp)) }
            } else if (showTrigger) {
                KButton(
                    onClick = { state.expanded = !state.expanded },
                    variant = KButtonVariant.Ghost, size = KButtonSize.IconXs, enabled = enabled
                ) {
                    Icon(
                        if (state.expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null, modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    singleLine: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit
) {
    androidx.compose.foundation.text.BasicTextField(
        value         = value,
        onValueChange = onValueChange,
        enabled       = enabled,
        singleLine    = singleLine,
        textStyle     = textStyle,
        modifier      = modifier,
        decorationBox = decorationBox
    )
}