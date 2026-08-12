package dev.kindling.core.components.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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

    var selected        by mutableStateOf(initialSelected)
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