package dev.kindling.core.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Data models
// ─────────────────────────────────────────────

/**
 * A single selectable item in a [Combobox].
 *
 * @param value  Unique key used for equality checks and [ComboboxValue].
 * @param label  Display text shown in the list and trigger.
 * @param group  Optional key matching a [ComboboxGroup] to bucket this item.
 */
data class KComboboxItem(
    val value: String,
    val label: String,
    val group: String? = null
)

/**
 * A labelled group header rendered above its items.
 */
data class KComboboxGroup(
    val key: String,
    val label: String
)

// ─────────────────────────────────────────────
//  Internal state
// ─────────────────────────────────────────────

/**
 * State holder created by [rememberComboboxState] and passed down to all
 * Combobox slot composables.
 *
 * Mirrors the controlled / uncontrolled pattern of the web component.
 */
@Stable
class ComboboxState internal constructor(
    // single selection
    initialSelected: KComboboxItem?,
    val onSelect: (KComboboxItem?) -> Unit,
    // multiple selection
    initialSelectedMultiple: List<KComboboxItem>,
    val onSelectMultiple: (List<KComboboxItem>) -> Unit,
    val multiple: Boolean
) {
    var expanded by mutableStateOf(false)
    var query    by mutableStateOf("")

    var selected         by mutableStateOf(initialSelected)
    var selectedMultiple by mutableStateOf(initialSelectedMultiple)

    fun isSelected(item: KComboboxItem) =
        if (multiple) selectedMultiple.any { it.value == item.value }
        else selected?.value == item.value

    fun toggle(item: KComboboxItem) {
        if (multiple) {
            val next = if (selectedMultiple.any { it.value == item.value })
                selectedMultiple.filter { it.value != item.value }
            else selectedMultiple + item
            selectedMultiple = next
            onSelectMultiple(next)
        } else {
            val next = if (selected?.value == item.value) null else item
            selected = next
            onSelect(next)
            expanded = false
        }
    }

    fun removeChip(item: KComboboxItem) {
        val next = selectedMultiple.filter { it.value != item.value }
        selectedMultiple = next
        onSelectMultiple(next)
    }

    fun close() { expanded = false; query = "" }
}

/**
 * Creates and remembers a [ComboboxState].
 *
 * ```kotlin
 * val state = rememberComboboxState(
 *     selected = current,
 *     onSelect = { current = it }
 * )
 * Combobox(state = state) { … }
 * ```
 */
@Composable
fun rememberComboboxState(
    selected: KComboboxItem? = null,
    onSelect: (KComboboxItem?) -> Unit = {},
    multiple: Boolean = false,
    selectedMultiple: List<KComboboxItem> = emptyList(),
    onSelectMultiple: (List<KComboboxItem>) -> Unit = {}
): ComboboxState = remember(multiple) {
    ComboboxState(selected, onSelect, selectedMultiple, onSelectMultiple, multiple)
}.also {
    // Keep state in sync with external controlled values
    LaunchedEffect(selected)         { it.selected = selected ?: it.selected }
    LaunchedEffect(selectedMultiple) { it.selectedMultiple = selectedMultiple }
}

// Convenience hook mirroring useComboboxAnchor from the web version
@Composable
fun useComboboxAnchor(): MutableState<Any?> = remember { mutableStateOf<Any?>(null) }

// ─────────────────────────────────────────────
//  Combobox (root)
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Combobox root — mirrors `Combobox` (Base UI primitive).
 *
 * Wraps its slots in a [Box] so [ComboboxContent] (the dropdown) can be
 * positioned relative to [ComboboxTrigger] / [ComboboxInput].
 *
 * ```kotlin
 * val state = rememberComboboxState(selected = sel, onSelect = { sel = it })
 *
 * Combobox(state = state) {
 *     ComboboxInput(state = state, placeholder = "Select a framework…")
 *     ComboboxContent(state = state, items = frameworkItems) {
 *         ComboboxList {
 *             ComboboxEmpty { Text("No results.") }
 *             ComboboxCollection(state = state, items = frameworkItems)
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun Combobox(
    state: ComboboxState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, content = content)
}

// ─────────────────────────────────────────────
//  ComboboxValue
// ─────────────────────────────────────────────

/**
 * Renders the current selected label (or [placeholder] if nothing selected).
 * Mirrors `ComboboxValue` from the web component.
 */
@Composable
fun ComboboxValue(
    state: ComboboxState,
    placeholder: String = "Select…",
    modifier: Modifier = Modifier
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

// ─────────────────────────────────────────────
//  ComboboxTrigger
// ─────────────────────────────────────────────

/**
 * Button-style trigger that opens/closes the dropdown.
 * Mirrors `ComboboxTrigger`.
 *
 * Accepts arbitrary [content] — typically [ComboboxValue] + a chevron.
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
            imageVector        = if (state.expanded) Icons.Default.KeyboardArrowUp
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
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content           = content
        )
    }
}

// ─────────────────────────────────────────────
//  ComboboxInput
// ─────────────────────────────────────────────

/**
 * Search-field trigger — mirrors `ComboboxInput`.
 *
 * Shows chips when [state.multiple] = true. The search query is written
 * into [state.query] so [ComboboxContent] can filter automatically.
 *
 * ```kotlin
 * ComboboxInput(state = state, placeholder = "Search…", showClear = true)
 * ```
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
            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chips (multiple mode)
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
                // Plain text search input
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

// Bare BasicTextField alias used inside ComboboxInput
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

// ─────────────────────────────────────────────
//  ComboboxContent (dropdown panel)
// ─────────────────────────────────────────────

/**
 * The dropdown panel — mirrors `ComboboxContent` (Positioner + Popup).
 *
 * Wraps a [DropdownMenu] anchored to the parent [Combobox] [Box].
 *
 * ```kotlin
 * ComboboxContent(state = state, items = items) {
 *     ComboboxList {
 *         ComboboxEmpty { Text("No results.") }
 *         ComboboxCollection(state = state, items = items)
 *     }
 * }
 * ```
 */
@Composable
fun ComboboxContent(
    state: ComboboxState,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 240.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded         = state.expanded,
        onDismissRequest = { state.close() },
        modifier         = modifier
            .background(MaterialTheme.colorScheme.surface)
            .widthIn(min = 180.dp)
    ) {
        Column(modifier = Modifier.heightIn(max = maxHeight), content = content)
    }
}

// ─────────────────────────────────────────────
//  ComboboxList
// ─────────────────────────────────────────────

/**
 * Scrollable list container inside [ComboboxContent].
 * Mirrors `ComboboxList`.
 */
@Composable
fun ComboboxList(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(4.dp), content = content)
}

// ─────────────────────────────────────────────
//  ComboboxEmpty
// ─────────────────────────────────────────────

/**
 * Empty-state slot shown when no items match the query.
 * Visibility is controlled by the caller — wrap in an `if` check.
 *
 * ```kotlin
 * if (filtered.isEmpty()) ComboboxEmpty { Text("No results found.") }
 * ```
 */
@Composable
fun ComboboxEmpty(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier         = modifier.fillMaxWidth().padding(12.dp),
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
//  ComboboxGroup + ComboboxLabel
// ─────────────────────────────────────────────

/**
 * Groups items under a labelled section.
 * Mirrors `ComboboxGroup`.
 *
 * ```kotlin
 * ComboboxGroup {
 *     ComboboxLabel { Text("Frameworks") }
 *     ComboboxItem(state = state, item = reactItem)
 * }
 * ```
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

// ─────────────────────────────────────────────
//  ComboboxSeparator
// ─────────────────────────────────────────────

/** Thin horizontal rule between groups. */
@Composable
fun ComboboxSeparator(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier  = modifier.padding(vertical = 4.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant
    )
}

// ─────────────────────────────────────────────
//  ComboboxItem
// ─────────────────────────────────────────────

/**
 * A single selectable row inside [ComboboxList].
 * Mirrors `ComboboxItem`.
 *
 * ```kotlin
 * ComboboxItem(state = state, item = item) {
 *     Text(item.label)          // optional custom content
 * }
 * ```
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

    DropdownMenuItem(
        onClick  = { state.toggle(item) },
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) cs.primary.copy(.08f) else Color.Transparent),
        text     = {
            Row(
                modifier          = Modifier.fillMaxWidth(),
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

// ─────────────────────────────────────────────
//  ComboboxCollection
// ─────────────────────────────────────────────

/**
 * Renders all [items] as [ComboboxItem]s, filtered by [state.query].
 *
 * Optionally groups them via [groups] — items are bucketed by
 * [KComboboxItem.group] matching [KComboboxGroup.key].
 *
 * Mirrors `ComboboxCollection` from the web component.
 *
 * ```kotlin
 * ComboboxCollection(
 *     state  = state,
 *     items  = frameworkItems,
 *     groups = frameworkGroups   // optional
 * )
 * ```
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

// ─────────────────────────────────────────────
//  ComboboxChips
// ─────────────────────────────────────────────

/**
 * Chip container shown inside the trigger area for multiple-selection mode.
 * Mirrors `ComboboxChips`.
 *
 * ```kotlin
 * ComboboxChips(state = state) {
 *     state.selectedMultiple.forEach { ComboboxChip(state, it) }
 *     ComboboxChipsInput(state, placeholder = "Search…")
 * }
 * ```
 */
@Composable
fun ComboboxChips(
    state: ComboboxState,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content               = content
    )
}

// ─────────────────────────────────────────────
//  ComboboxChip
// ─────────────────────────────────────────────

/**
 * Individual chip rendered inside [ComboboxChips] for each selected item.
 * Mirrors `ComboboxChip`.
 */
@Composable
fun ComboboxChip(
    state: ComboboxState,
    item: KComboboxItem,
    modifier: Modifier = Modifier,
    showRemove: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
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

// ─────────────────────────────────────────────
//  ComboboxChipsInput
// ─────────────────────────────────────────────

/**
 * Inline search input rendered alongside chips inside [ComboboxChips].
 * Mirrors `ComboboxChipsInput`.
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
            .widthIn(min = 64.dp)
            .weight(1f, fill = false),
        decorationBox = { inner ->
            if (state.query.isEmpty()) {
                Text(placeholder, fontSize = 13.sp, color = cs.onSurface.copy(.5f))
            }
            inner()
        }
    )
}