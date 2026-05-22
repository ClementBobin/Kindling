package dev.kindling.core.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * Shadcn/ui-style searchable Combobox.
 *
 * ```kotlin
 * var selected by remember { mutableStateOf<KComboboxItem?>(null) }
 * KCombobox(
 *     items       = frameworks,
 *     selected    = selected,
 *     onSelect    = { selected = it },
 *     placeholder = "Select framework…"
 * )
 * ```
 */
@Composable
fun KCombobox(
    items: List<KComboboxItem>,
    selected: KComboboxItem?,
    onSelect: (KComboboxItem?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select…",
    searchPlaceholder: String = "Search…",
    enabled: Boolean = true,
    maxDropdownHeight: Dp = 240.dp,
    emptyLabel: String = "No results found."
) {
    val cs = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    var query    by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val filtered = remember(query, items) {
        if (query.isBlank()) items else items.filter { it.label.contains(query, ignoreCase = true) }
    }

    Column(modifier = modifier) {
        // ── Trigger ───────────────────────────────────
        Surface(
            onClick      = { if (enabled) expanded = !expanded },
            enabled      = enabled,
            shape        = RoundedCornerShape(6.dp),
            color        = Color.Transparent,
            contentColor = cs.onBackground,
            border       = BorderStroke(1.dp, if (expanded) cs.primary else cs.outline),
            modifier     = Modifier.fillMaxWidth().height(36.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text      = selected?.label ?: placeholder,
                    fontSize  = 14.sp,
                    color     = if (selected != null) cs.onBackground else cs.onSurface.copy(alpha = 0.5f)
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint     = cs.onSurfaceVariant
                )
            }
        }

        // ── Dropdown popup ───────────────────────────
        if (expanded) {
            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            Popup(
                onDismissRequest = { expanded = false; query = "" },
                properties       = PopupProperties(focusable = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(cs.surface)
                        .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    OutlinedTextField(
                        value         = query,
                        onValueChange = { query = it },
                        placeholder   = { Text(searchPlaceholder, fontSize = 13.sp, color = cs.onSurface.copy(alpha = 0.5f)) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth().focusRequester(focusRequester).padding(bottom = 4.dp),
                        textStyle     = LocalTextStyle.current.copy(fontSize = 13.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors        = OutlinedTextFieldDefaults.colors(
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

                    LazyColumn(modifier = Modifier.heightIn(max = maxDropdownHeight)) {
                        if (filtered.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                    Text(emptyLabel, fontSize = 13.sp, color = cs.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(filtered) { item ->
                                val isSel = item.value == selected?.value
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { onSelect(if (isSel) null else item); expanded = false; query = "" }
                                        .background(if (isSel) cs.primary.copy(alpha = 0.08f) else Color.Transparent)
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.label, fontSize = 14.sp, fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal, color = cs.onSurface)
                                    if (isSel) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = cs.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
