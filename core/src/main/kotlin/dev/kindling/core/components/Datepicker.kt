package dev.kindling.core.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.kindling.core.components.internal.KindlingPreviewSurface
import dev.kindling.core.components.internal.PreviewLabel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

/**
 * Render a shadcn/ui-style date picker.
 *
 * Requires API 26+ because it relies on java.time APIs.
 *
 * ```kotlin
 * var date by remember { mutableStateOf<LocalDate?>(null) }
 * KDatePicker(selected = date, onSelect = { date = it }, placeholder = "Pick a date")
 * ```
 *
 * @param selected Currently selected date, or `null` when empty.
 * @param onSelect Callback invoked when a date is selected.
 * @param modifier Applied to the outermost layout element.
 * @param placeholder Text shown when no date is selected.
 * @param enabled When `false`, the picker is non-interactive and dimmed.
 * @param minDate Minimum selectable date, inclusive.
 * @param maxDate Maximum selectable date, inclusive.
 * @param locale Locale used to format month and weekday labels.
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
    val cs = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    val fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy", locale)

    Column(modifier = modifier) {
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
                    text  = selected?.format(fmt) ?: placeholder,
                    fontSize = 14.sp,
                    color = if (selected != null) cs.onBackground else cs.onSurface.copy(alpha = 0.5f)
                )
                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp), tint = cs.onSurfaceVariant)
            }
        }

        if (expanded) {
            Popup(
                onDismissRequest = { expanded = false },
                properties       = PopupProperties(focusable = true)
            ) {
                CalendarView(selected, minDate, maxDate, locale) { onSelect(it); expanded = false }
            }
        }
    }
}

@Preview(name = "KDatePicker — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KDatePicker — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKDatePicker() {
    val selectedDate = LocalDate.of(2026, 5, 12)

    KindlingPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PreviewLabel("Empty")
            KDatePicker(selected = null, onSelect = { })

            PreviewLabel("With date selected")
            KDatePicker(selected = selectedDate, onSelect = { })

            PreviewLabel("Disabled")
            KDatePicker(selected = selectedDate, onSelect = { }, enabled = false)
        }
    }
}

@Composable
private fun CalendarView(
    selected: LocalDate?,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    locale: Locale,
    onSelect: (LocalDate) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var currentMonth by remember { mutableStateOf(YearMonth.from(selected ?: LocalDate.now())) }
    val today = LocalDate.now()
    val monthLabel = currentMonth.month.getDisplayName(JTextStyle.FULL, locale).replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .padding(top = 4.dp)
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(cs.surface)
            .border(1.dp, cs.outline, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .width(280.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, Modifier.size(18.dp))
            }
            Text("$monthLabel ${currentMonth.year}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            DayOfWeek.values().forEach { d ->
                Text(d.getDisplayName(JTextStyle.NARROW, locale), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = cs.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(4.dp))

        val firstDay    = currentMonth.atDay(1)
        val startOffset = firstDay.dayOfWeek.value - 1
        val daysInMonth = currentMonth.lengthOfMonth()
        val rows        = (startOffset + daysInMonth + 6) / 7

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(Modifier.weight(1f))
                    } else {
                        val date       = currentMonth.atDay(dayNum)
                        val isSelected = date == selected
                        val isToday    = date == today
                        val disabled   = (minDate != null && date < minDate) || (maxDate != null && date > maxDate)

                        Box(
                            modifier = Modifier
                                .weight(1f).aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) cs.primary else Color.Transparent)
                                .border(if (isToday && !isSelected) 1.dp else 0.dp, if (isToday && !isSelected) cs.primary else Color.Transparent, CircleShape)
                                .then(if (!disabled) Modifier.clickable { onSelect(date) } else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                dayNum.toString(),
                                fontSize   = 13.sp,
                                color      = when {
                                    isSelected -> cs.onPrimary
                                    disabled   -> cs.onSurface.copy(alpha = 0.3f)
                                    isToday    -> cs.primary
                                    else       -> cs.onSurface
                                },
                                fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
