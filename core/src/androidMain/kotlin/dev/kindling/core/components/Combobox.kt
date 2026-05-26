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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Data models
// ─────────────────────────────────────────────

/**
 * A single selectable item in [KCombobox].
 *
 * @param value  Unique key used for equality checks.
 * @param label  Primary display text.
 * @param group  Optional group key — matches [KComboboxGroup.key] to bucket this item.
 */
data class KComboboxItem(
    val value: String,
    val label: String,
    val group: String? = null
)

/**
 * A labelled group header rendered above its items in the dropdown.
 *
 * ```kotlin
 * val groups = listOf(
 *     KComboboxGroup("americas", "Americas"),
 *     KComboboxGroup("europe",   "Europe"),
 * )
 * val items = listOf(
 *     KComboboxItem("nyc",    "(GMT-5) New York",  group = "americas"),
 *     KComboboxItem("london", "(GMT+0) London",    group = "europe"),
 * )
 * ```
 */
data class KComboboxGroup(
    val key: String,
    val label: String
)

// ─────────────────────────────────────────────
//  KCombobox
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style searchable Combobox compatible with both Android Compose
 * and JetBrains Compose Multiplatform.
 *
 * Uses only cross-platform primitives — [Box], [Column], [Surface],
 * [DropdownMenu] — avoiding any Skiko-only APIs (`Popup_skikoKt`,
 * `ExposedDropdownMenu_skikoKt`, etc.) that crash on Android.
 *
 * ### Single selection
 * ```kotlin
 * var selected by remember { mutableStateOf<KComboboxItem?>(null) }
 * KCombobox(
 *     items       = frameworks,
 *     selected    = selected,
 *     onSelect    = { selected = it },
 *     placeholder = "Select a framework",
 *     showSearch  = false
 * )
 * ```
 *
 * ### Multiple selection (chips in trigger)
 * ```kotlin
 * var sel by remember { mutableStateOf(emptyList<KComboboxItem>()) }
 * KCombobox(
 *     items            = frameworks,
 *     multiple         = true,
 *     selectedMultiple = sel,
 *     onSelectMultiple = { sel = it }
 * )
 * ```
 *
 * ### Grouped items
 * ```kotlin
 * KCombobox(
 *     items  = timezoneItems,   // each item has group = "americas" / "europe"
 *     groups = timezoneGroups,
 *     …
 * )
 * ```
 *
 * ### Custom item rendering
 * ```kotlin
 * KCombobox(
 *     items      = countryItems,
 *     customItem = { item ->
 *         Column {
 *             Text(item.label, fontWeight = FontWeight.SemiBold)
 *             Text(item.value, fontSize = 11.sp, color = …onSurfaceVariant)
 *         }
 *     }, …
 * )
 * ```
 *
 * ### Input group addon (leading icon)
 * ```kotlin
 * KCombobox(
 *     items = timezoneItems,
 *     addon = { Icon(Icons.Default.Language, contentDescription = null) },
 *     …
 * )
 * ```
 */
@Composable
fun KCombobox(
    items: List<KComboboxItem>,
    modifier: Modifier = Modifier,

    // ── Single selection ─────────────────────────────────────────────────
    selected: KComboboxItem? = null,
    onSelect: (KComboboxItem?) -> Unit = {},

    // ── Multiple selection ───────────────────────────────────────────────
    multiple: Boolean = false,
    selectedMultiple: List<KComboboxItem> = emptyList(),
    onSelectMultiple: (List<KComboboxItem>) -> Unit = {},

    // ── Display ──────────────────────────────────────────────────────────
    placeholder: String = "Select…",
    searchPlaceholder: String = "Search…",
    /** Show a search field at the top of the dropdown. */
    showSearch: Boolean = true,

    // ── Clear button (single mode only) ──────────────────────────────────
    /** Show an × button inside the trigger when a value is selected. */
    showClear: Boolean = false,

    // ── Groups ────────────────────────────────────────────────────────────
    /** Optional group definitions. Items are bucketed by [KComboboxItem.group]. */
    groups: List<KComboboxGroup> = emptyList(),

    // ── Custom item ───────────────────────────────────────────────────────
    /** Override the default label row with a fully custom composable per item. */
    customItem: (@Composable (KComboboxItem) -> Unit)? = null,

    // ── Input group addon ─────────────────────────────────────────────────
    /** Leading slot rendered left of the trigger label (icon, flag, etc.). */
    addon: (@Composable () -> Unit)? = null,

    // ── State ─────────────────────────────────────────────────────────────
    enabled: Boolean = true,
    isInvalid: Boolean = false,

    // ── Auto-highlight ────────────────────────────────────────────────────
    /** Automatically highlight the first filtered item when query changes. */
    autoHighlight: Boolean = false,

    // ── Layout ────────────────────────────────────────────────────────────
    maxDropdownHeight: Dp = 240.dp,
    emptyLabel: String = "No results found."
) {
    val cs = MaterialTheme.colorScheme

    var expanded       by remember { mutableStateOf(false) }
    var query          by remember { mutableStateOf("") }
    var highlightIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(expanded) { if (!expanded) query = "" }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter { it.label.contains(query, ignoreCase = true) }
    }

    LaunchedEffect(filtered) {
        highlightIndex = if (autoHighlight && filtered.isNotEmpty()) 0 else -1
    }

    val hasValue = if (multiple) selectedMultiple.isNotEmpty() else selected != null

    fun isSelected(item: KComboboxItem) =
        if (multiple) selectedMultiple.any { it.value == item.value }
        else selected?.value == item.value

    fun toggle(item: KComboboxItem) {
        if (multiple) {
            val next = if (selectedMultiple.any { it.value == item.value })
                selectedMultiple.filter { it.value != item.value }
            else selectedMultiple + item
            onSelectMultiple(next)
        } else {
            onSelect(if (selected?.value == item.value) null else item)
            expanded = false
        }
    }

    val borderColor = when {
        isInvalid -> cs.error
        expanded  -> cs.primary
        else      -> cs.outline
    }

    // Box is the anchor for DropdownMenu — it positions the menu below itself
    // using its own layout bounds, no Skiko/Popup needed.
    Box(modifier = modifier) {

        // ── Trigger ───────────────────────────────────────────────────────
        Surface(
            onClick      = { if (enabled) expanded = !expanded },
            enabled      = enabled,
            shape        = RoundedCornerShape(6.dp),
            color        = Color.Transparent,
            contentColor = if (enabled) cs.onBackground
            else cs.onBackground.copy(alpha = 0.38f),
            border       = BorderStroke(
                width = if (isInvalid) 2.dp else 1.dp,
                color = if (!enabled) cs.outline.copy(alpha = 0.38f) else borderColor
            ),
            modifier     = Modifier
                .fillMaxWidth()
                .heightIn(min = 36.dp)
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading addon
                if (addon != null) {
                    Box(
                        modifier         = Modifier.padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(
                            LocalContentColor provides cs.onSurfaceVariant
                        ) { addon() }
                    }
                }

                // Label / chips
                if (multiple && selectedMultiple.isNotEmpty()) {
                    Row(
                        modifier              = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        selectedMultiple.forEach { item ->
                            KComboboxChip(
                                label    = item.label,
                                onRemove = {
                                    onSelectMultiple(
                                        selectedMultiple.filter { it.value != item.value }
                                    )
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text     = selected?.label ?: placeholder,
                        fontSize = 14.sp,
                        color    = if (hasValue && !multiple) cs.onBackground
                        else cs.onSurface.copy(
                            alpha = if (enabled) 0.5f else 0.38f
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Clear × or chevron
                if (showClear && hasValue && !multiple && enabled) {
                    Box(
                        modifier         = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .clickable { onSelect(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier           = Modifier.size(12.dp),
                            tint               = cs.onSurfaceVariant
                        )
                    }
                } else {
                    Icon(
                        imageVector        = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = if (enabled) cs.onSurfaceVariant
                        else cs.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
            }
        }

        // ── Dropdown ─────────────────────────────────────────────────────
        // DropdownMenu is cross-platform in both Google and JetBrains M3.
        // It anchors to the parent Box automatically.
        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
            modifier         = Modifier
                .background(cs.surface)
                .widthIn(min = 180.dp)
        ) {
            // Optional search field
            if (showSearch) {
                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    placeholder   = {
                        Text(
                            searchPlaceholder,
                            fontSize = 13.sp,
                            color    = cs.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    singleLine      = true,
                    modifier        = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    textStyle       = LocalTextStyle.current.copy(fontSize = 13.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = cs.primary,
                        unfocusedBorderColor    = cs.outline,
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor        = cs.onBackground,
                        unfocusedTextColor      = cs.onBackground,
                        cursorColor             = cs.primary
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }

            // Items — the DropdownMenu itself is already scrollable,
            // Column + heightIn caps the visible area.
            Column(modifier = Modifier.heightIn(max = maxDropdownHeight)) {
                if (filtered.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emptyLabel, fontSize = 13.sp, color = cs.onSurfaceVariant)
                    }
                } else if (groups.isNotEmpty()) {
                    // ── Grouped ───────────────────────────────────────────
                    groups.forEachIndexed { groupIndex, group ->
                        val groupItems = filtered.filter { it.group == group.key }
                        if (groupItems.isEmpty()) return@forEachIndexed

                        if (groupIndex > 0) KComboboxSeparator()

                        Text(
                            text       = group.label,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color      = cs.onSurfaceVariant,
                            modifier   = Modifier.padding(
                                horizontal = 12.dp,
                                vertical   = 6.dp
                            )
                        )

                        groupItems.forEach { item ->
                            KComboboxDropdownItem(
                                item          = item,
                                isSelected    = isSelected(item),
                                isHighlighted = false,
                                customItem    = customItem,
                                onClick       = { toggle(item) }
                            )
                        }
                    }

                    val ungrouped = filtered.filter { it.group == null }
                    if (ungrouped.isNotEmpty()) {
                        KComboboxSeparator()
                        ungrouped.forEach { item ->
                            KComboboxDropdownItem(
                                item          = item,
                                isSelected    = isSelected(item),
                                isHighlighted = false,
                                customItem    = customItem,
                                onClick       = { toggle(item) }
                            )
                        }
                    }
                } else {
                    // ── Flat ──────────────────────────────────────────────
                    filtered.forEachIndexed { index, item ->
                        KComboboxDropdownItem(
                            item          = item,
                            isSelected    = isSelected(item),
                            isHighlighted = autoHighlight && index == highlightIndex,
                            customItem    = customItem,
                            onClick       = { toggle(item) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  KComboboxChip
// ─────────────────────────────────────────────

/**
 * Pill chip rendered inside the trigger for each selected item in multiple mode.
 * Automatically used by [KCombobox] when `multiple = true`.
 */
@Composable
fun KComboboxChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier              = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(cs.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 12.sp, color = cs.onSurfaceVariant)
        Box(
            modifier         = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove $label",
                modifier           = Modifier.size(10.dp),
                tint               = cs.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────
//  KComboboxSeparator
// ─────────────────────────────────────────────

/**
 * Thin horizontal rule between groups.
 * Inserted automatically by [KCombobox] between groups.
 */
@Composable
fun KComboboxSeparator(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier  = modifier.padding(vertical = 4.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant
    )
}

// ─────────────────────────────────────────────
//  Internal — dropdown item row
// ─────────────────────────────────────────────

@Composable
private fun KComboboxDropdownItem(
    item: KComboboxItem,
    isSelected: Boolean,
    isHighlighted: Boolean,
    customItem: (@Composable (KComboboxItem) -> Unit)?,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val bg = when {
        isHighlighted -> cs.primary.copy(alpha = 0.12f)
        isSelected    -> cs.primary.copy(alpha = 0.08f)
        else          -> Color.Transparent
    }

    DropdownMenuItem(
        onClick  = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg),
        text     = {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (customItem != null) {
                        customItem(item)
                    } else {
                        Text(
                            text       = item.label,
                            fontSize   = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Medium
                            else FontWeight.Normal,
                            color      = cs.onSurface
                        )
                    }
                }
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier           = Modifier
                            .padding(start = 8.dp)
                            .size(16.dp),
                        tint               = cs.primary
                    )
                }
            }
        }
    )
}