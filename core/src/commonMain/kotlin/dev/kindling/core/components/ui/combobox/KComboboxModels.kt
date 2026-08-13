package dev.kindling.core.components.ui.combobox

/**
 * A single selectable item in a [Combobox].
 *
 * @param value  Unique key used for equality checks and [ComboboxValue].
 * @param label  Display text shown in the list and trigger.
 * @param group  Optional key matching a [KComboboxGroup] to bucket this item.
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