package dev.kindling.core.components.ui.datePicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.calendar.CalendarGrid
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Shadcn/ui-style DatePicker. Requires API 26+ (java.time).
 *
 * Uses [AnimatedVisibility] to show/hide an inline calendar card directly
 * below the trigger — no Popup or Skiko dependency.
 *
 * ```kotlin
 * var date by remember { mutableStateOf<LocalDate?>(null) }
 * KDatePicker(selected = date, onSelect = { date = it }, placeholder = "Pick a date")
 * ```
 */
@Composable
fun KDatePicker(
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Pick a date",
    enabled: Boolean = true,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    locale: Locale = Locale.getDefault()
) {
    val cs  = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    val fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy", locale)

    Column(modifier = modifier) {
        // ── Trigger ──────────────────────────────────────────────────────────
        Surface(
            onClick      = { if (enabled) expanded = !expanded },
            enabled      = enabled,
            shape        = RoundedCornerShape(6.dp),
            color        = Color.Transparent,
            contentColor = cs.onBackground,
            border       = BorderStroke(1.dp, if (expanded) cs.primary else cs.outline),
            modifier     = Modifier
                .fillMaxWidth()
                .height(36.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text     = selected?.format(fmt) ?: placeholder,
                    fontSize = 14.sp,
                    color    = if (selected != null) cs.onBackground
                    else cs.onSurface.copy(alpha = 0.5f)
                )
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint     = cs.onSurfaceVariant
                )
            }
        }

        // ── Inline calendar (no Popup) ────────────────────────────────────
        AnimatedVisibility(
            visible = expanded,
            enter   = expandVertically(tween(150)) + fadeIn(tween(150)),
            exit    = shrinkVertically(tween(150)) + fadeOut(tween(150))
        ) {
            CalendarGrid(
                selected = selected,
                minDate = minDate,
                maxDate = maxDate,
                locale = locale,
                onSelect = { date ->
                    onSelect(date)
                    expanded = false
                }
            )
        }
    }
}