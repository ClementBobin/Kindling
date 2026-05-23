package dev.kindling.core.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

// ─────────────────────────────────────────────
//  Selection mode
// ─────────────────────────────────────────────

/** Matches `DayPicker` mode prop from react-day-picker. */
sealed interface KCalendarMode {
    /** Single date selection. */
    object Single : KCalendarMode

    /** Date range selection (from / to). */
    object Range : KCalendarMode
}

/** Holds the selected range for [KCalendarMode.Range]. */
data class KDateRange(
    val from: LocalDate? = null,
    val to: LocalDate? = null
)

// ─────────────────────────────────────────────
//  KCalendar
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Calendar — a standalone month grid for date or range selection.
 *
 * Mirrors `calendar.tsx` (react-day-picker backed). Supports single and range
 * modes, optional outside-day display, and an optional week-number column.
 *
 * ```kotlin
 * // Single
 * var selected by remember { mutableStateOf<LocalDate?>(null) }
 * KCalendar(selected = selected, onSelectSingle = { selected = it })
 *
 * // Range
 * var range by remember { mutableStateOf(KDateRange()) }
 * KCalendar(
 *     mode           = KCalendarMode.Range,
 *     selectedRange  = range,
 *     onSelectRange  = { range = it }
 * )
 * ```
 *
 * @param mode              Selection mode — [KCalendarMode.Single] (default) or [KCalendarMode.Range].
 * @param selected          Currently selected date (single mode).
 * @param onSelectSingle    Callback when a date is selected in single mode.
 * @param selectedRange     Currently selected range (range mode).
 * @param onSelectRange     Callback when the range changes.
 * @param showOutsideDays   Whether to render days from adjacent months.
 * @param showWeekNumber    Whether to render a leading week-number column.
 * @param numberOfMonths    How many month grids to display side-by-side.
 * @param locale            Locale used for weekday/month names.
 * @param modifier          Applied to the outer container.
 */
@Composable
fun KCalendar(
    mode: KCalendarMode = KCalendarMode.Single,
    selected: LocalDate? = null,
    onSelectSingle: ((LocalDate?) -> Unit)? = null,
    selectedRange: KDateRange = KDateRange(),
    onSelectRange: ((KDateRange) -> Unit)? = null,
    showOutsideDays: Boolean = true,
    showWeekNumber: Boolean = false,
    numberOfMonths: Int = 1,
    locale: Locale = Locale.getDefault(),
    modifier: Modifier = Modifier
) {
    var currentMonth by remember {
        mutableStateOf(
            YearMonth.from(
                when {
                    selected != null     -> selected
                    selectedRange.from != null -> selectedRange.from
                    else                 -> LocalDate.now()
                }
            )
        )
    }

    Row(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(numberOfMonths) { monthOffset ->
            val month = currentMonth.plusMonths(monthOffset.toLong())
            KCalendarMonth(
                month           = month,
                mode            = mode,
                selected        = selected,
                onSelectSingle  = onSelectSingle,
                selectedRange   = selectedRange,
                onSelectRange   = onSelectRange,
                showOutsideDays = showOutsideDays,
                showWeekNumber  = showWeekNumber,
                locale          = locale,
                showNav         = monthOffset == 0,
                totalMonths     = numberOfMonths,
                onPrev          = { currentMonth = currentMonth.minusMonths(1) },
                onNext          = { currentMonth = currentMonth.plusMonths(1) },
                modifier        = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Internal — single month grid
// ─────────────────────────────────────────────

@Composable
private fun KCalendarMonth(
    month: YearMonth,
    mode: KCalendarMode,
    selected: LocalDate?,
    onSelectSingle: ((LocalDate?) -> Unit)?,
    selectedRange: KDateRange,
    onSelectRange: ((KDateRange) -> Unit)?,
    showOutsideDays: Boolean,
    showWeekNumber: Boolean,
    locale: Locale,
    showNav: Boolean,
    totalMonths: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs    = MaterialTheme.colorScheme
    val today = LocalDate.now()

    val monthLabel = month.month.getDisplayName(JTextStyle.FULL, locale)
        .replaceFirstChar { it.uppercase() } + " ${month.year}"

    Column(modifier = modifier.width(IntrinsicSize.Min)) {

        // ── Caption / navigation ──────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        ) {
            if (showNav) {
                IconButton(
                    onClick  = onPrev,
                    modifier = Modifier.align(Alignment.CenterStart).size(28.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text      = monthLabel,
                fontSize  = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier  = Modifier.align(Alignment.Center)
            )
            if (showNav) {
                IconButton(
                    onClick  = onNext,
                    modifier = Modifier.align(Alignment.CenterEnd).size(28.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next month",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── Weekday header ────────────────────────────
        Row {
            if (showWeekNumber) {
                WeekCell(text = "", cs = cs)
            }
            DayOfWeek.values().forEach { dow ->
                WeekCell(
                    text = dow.getDisplayName(JTextStyle.NARROW, locale),
                    cs   = cs
                )
            }
        }

        // ── Day grid ──────────────────────────────────
        val firstDay    = month.atDay(1)
        val startOffset = (firstDay.dayOfWeek.value - 1) // Mon=0
        val daysInMonth = month.lengthOfMonth()
        val rows        = (startOffset + daysInMonth + 6) / 7

        for (row in 0 until rows) {
            Row {
                if (showWeekNumber) {
                    val weekDayNum = row * 7 + 1 - startOffset
                    val weekDate   = if (weekDayNum in 1..daysInMonth) month.atDay(weekDayNum) else month.atDay(1)
                    val weekNum    = weekDate.get(java.time.temporal.WeekFields.of(locale).weekOfWeekBasedYear())
                    WeekCell(text = weekNum.toString(), cs = cs, muted = true)
                }

                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1

                    when {
                        dayNum < 1 || dayNum > daysInMonth -> {
                            // Outside day
                            val outsideDate: LocalDate? = when {
                                dayNum < 1          -> month.minusMonths(1).atDay(month.minusMonths(1).lengthOfMonth() + dayNum)
                                else                -> month.plusMonths(1).atDay(dayNum - daysInMonth)
                            }
                            if (showOutsideDays && outsideDate != null) {
                                CalendarDayButton(
                                    date          = outsideDate,
                                    mode          = mode,
                                    selected      = selected,
                                    selectedRange = selectedRange,
                                    today         = today,
                                    outside       = true,
                                    onSelectSingle = onSelectSingle,
                                    onSelectRange  = onSelectRange
                                )
                            } else {
                                Spacer(Modifier.size(28.dp))
                            }
                        }
                        else -> {
                            CalendarDayButton(
                                date          = month.atDay(dayNum),
                                mode          = mode,
                                selected      = selected,
                                selectedRange = selectedRange,
                                today         = today,
                                outside       = false,
                                onSelectSingle = onSelectSingle,
                                onSelectRange  = onSelectRange
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Internal — day button
// ─────────────────────────────────────────────

@Composable
private fun CalendarDayButton(
    date: LocalDate,
    mode: KCalendarMode,
    selected: LocalDate?,
    selectedRange: KDateRange,
    today: LocalDate,
    outside: Boolean,
    onSelectSingle: ((LocalDate?) -> Unit)?,
    onSelectRange: ((KDateRange) -> Unit)?
) {
    val cs = MaterialTheme.colorScheme

    val isSelectedSingle = mode is KCalendarMode.Single && date == selected
    val isRangeStart     = mode is KCalendarMode.Range && date == selectedRange.from
    val isRangeEnd       = mode is KCalendarMode.Range && date == selectedRange.to
    val isRangeMiddle    = mode is KCalendarMode.Range
            && selectedRange.from != null && selectedRange.to != null
            && date.isAfter(selectedRange.from) && date.isBefore(selectedRange.to)
    val isToday          = date == today

    val bgColor = when {
        isSelectedSingle || isRangeStart || isRangeEnd -> cs.primary
        isRangeMiddle                                  -> cs.primary.copy(alpha = 0.12f)
        isToday                                        -> cs.muted
        else                                           -> Color.Transparent
    }

    val textColor = when {
        isSelectedSingle || isRangeStart || isRangeEnd -> cs.onPrimary
        outside                                        -> cs.onSurface.copy(alpha = 0.4f)
        isToday                                        -> cs.onSurface
        else                                           -> cs.onSurface
    }

    val shape = when {
        isRangeStart  -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
        isRangeEnd    -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
        isRangeMiddle -> RoundedCornerShape(0.dp)
        else          -> CircleShape
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(shape)
            .background(bgColor)
            .then(
                if (isToday && !isSelectedSingle && !isRangeStart && !isRangeEnd)
                    Modifier.border(1.dp, cs.primary, CircleShape)
                else Modifier
            )
            .clickable {
                when (mode) {
                    is KCalendarMode.Single -> {
                        onSelectSingle?.invoke(if (isSelectedSingle) null else date)
                    }
                    is KCalendarMode.Range -> {
                        val from = selectedRange.from
                        val to   = selectedRange.to
                        when {
                            from == null || to != null -> onSelectRange?.invoke(KDateRange(from = date))
                            date.isBefore(from)        -> onSelectRange?.invoke(KDateRange(from = date, to = from))
                            else                       -> onSelectRange?.invoke(KDateRange(from = from, to = date))
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = date.dayOfMonth.toString(),
            fontSize   = 13.sp,
            color      = textColor,
            fontWeight = if (isSelectedSingle || isRangeStart || isRangeEnd || isToday) FontWeight.SemiBold else FontWeight.Normal,
            textAlign  = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────
//  Internal helpers
// ─────────────────────────────────────────────

@Composable
private fun WeekCell(
    text: String,
    cs: androidx.compose.material3.ColorScheme,
    muted: Boolean = false
) {
    Box(
        modifier         = Modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = text,
            fontSize  = 12.sp,
            color     = if (muted) cs.onSurfaceVariant else cs.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            textAlign  = TextAlign.Center
        )
    }
}

private val androidx.compose.material3.ColorScheme.muted: Color
    get() = surfaceVariant