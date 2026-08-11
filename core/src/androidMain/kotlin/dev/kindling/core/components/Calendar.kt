package dev.kindling.core.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

// ─────────────────────────────────────────────
//  Calendar locale abstraction
// ─────────────────────────────────────────────

sealed interface KCalendarLocale {
    data class Standard(val locale: Locale = Locale.getDefault()) : KCalendarLocale
    data class Persian(val locale: Locale = Locale.forLanguageTag("fa-IR")) : KCalendarLocale
    data class Hijri(val locale: Locale = Locale.forLanguageTag("ar-SA"))   : KCalendarLocale
}

// ─────────────────────────────────────────────
//  Day content slot type
// ─────────────────────────────────────────────

typealias KCalendarDayContent = @Composable (date: LocalDate) -> Unit

// ─────────────────────────────────────────────
//  Preset
// ─────────────────────────────────────────────

data class KCalendarPreset(val label: String, val date: LocalDate)

// ─────────────────────────────────────────────
//  KCalendar
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Calendar — mirrors `calendar.tsx`.
 *
 * - Supports single / range selection
 * - Dropdown or label caption layout
 * - Week numbers, outside-days, booked/disabled dates
 * - Per-cell custom content slot (prices, dots, …)
 * - Preset buttons, multi-month
 * - Persian (Jalali) and Islamic (Hijri) calendar systems
 * - Full RTL support — reads [LocalLayoutDirection]; override via [layoutDirection]
 *
 * ```kotlin
 * var date by remember { mutableStateOf<LocalDate?>(null) }
 * KCalendar(selected = date, onSelectSingle = { date = it })
 *
 * // RTL override
 * KCalendar(selected = date, onSelectSingle = { date = it },
 *            layoutDirection = LayoutDirection.Rtl)
 * ```
 */
@Composable
fun KCalendar(
    modifier: Modifier = Modifier,
    // Selection
    mode: KCalendarMode = KCalendarMode.Single,
    selected: LocalDate? = null,
    onSelectSingle: ((LocalDate?) -> Unit)? = null,
    selectedRange: KDateRange = KDateRange(),
    onSelectRange: ((KDateRange) -> Unit)? = null,
    // Display
    captionLayout: KCalendarCaptionLayout = KCalendarCaptionLayout.Label,
    calendarLocale: KCalendarLocale = KCalendarLocale.Standard(),
    showOutsideDays: Boolean = true,
    showWeekNumber: Boolean = false,
    numberOfMonths: Int = 1,
    // Disabled / booked
    disabledDates: Set<LocalDate> = emptySet(),
    bookedDates: Set<LocalDate> = emptySet(),
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    // Extra UI
    presets: List<KCalendarPreset> = emptyList(),
    dayContent: KCalendarDayContent? = null,
    // RTL override (defaults to LocalLayoutDirection)
    layoutDirection: LayoutDirection? = null
) {
    val adapter   = rememberCalendarAdapter(calendarLocale)
    val rtl       = layoutDirection?.let { it == LayoutDirection.Rtl }
        ?: (LocalLayoutDirection.current == LayoutDirection.Rtl || adapter.isRtl())

    var currentMonth by remember {
        mutableStateOf(
            adapter.yearMonthOf(
                selected ?: selectedRange.from ?: LocalDate.now()
            )
        )
    }

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(numberOfMonths) { offset ->
                val month = adapter.plusMonths(currentMonth, offset)
                KCalendarMonth(
                    month           = month,
                    adapter         = adapter,
                    captionLayout   = captionLayout,
                    mode            = mode,
                    selected        = selected,
                    onSelectSingle  = onSelectSingle,
                    selectedRange   = selectedRange,
                    onSelectRange   = onSelectRange,
                    showOutsideDays = showOutsideDays,
                    showWeekNumber  = showWeekNumber,
                    disabledDates   = disabledDates,
                    bookedDates     = bookedDates,
                    minDate         = minDate,
                    maxDate         = maxDate,
                    dayContent      = dayContent,
                    showNavPrev     = offset == 0,
                    showNavNext     = offset == numberOfMonths - 1,
                    onPrev          = { currentMonth = adapter.plusMonths(currentMonth, -1) },
                    onNext          = { currentMonth = adapter.plusMonths(currentMonth, 1) },
                    onMonthChange   = { currentMonth = it },
                    rtl             = rtl,
                    modifier        = Modifier.weight(1f)
                )
            }
        }

        if (presets.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            CalendarPresetRow(
                presets  = presets,
                onSelect = { preset ->
                    onSelectSingle?.invoke(preset.date)
                    currentMonth = adapter.yearMonthOf(preset.date)
                }
            )
        }
    }
}

// ─────────────────────────────────────────────
//  KCalendarDayButton — mirrors web CalendarDayButton
// ─────────────────────────────────────────────

/**
 * Individual day cell button — mirrors `CalendarDayButton` from `calendar.tsx`.
 *
 * Uses [KButton] with [KButtonVariant.Ghost] as its base.
 * Exposes selection/range/today/outside state via parameters.
 *
 * ```kotlin
 * KCalendarDayButton(
 *     date          = date,
 *     dayLabel      = "14",
 *     isSelected    = true,
 *     isToday       = false,
 *     isDisabled    = false,
 *     isOutside     = false,
 *     onClick       = { onSelectSingle(date) }
 * )
 * ```
 */
@Composable
fun KCalendarDayButton(
    date: LocalDate,
    dayLabel: String,
    isSelected: Boolean = false,
    isRangeStart: Boolean = false,
    isRangeEnd: Boolean = false,
    isRangeMiddle: Boolean = false,
    isToday: Boolean = false,
    isDisabled: Boolean = false,
    isBooked: Boolean = false,
    isOutside: Boolean = false,
    modifier: Modifier = Modifier,
    dayContent: KCalendarDayContent? = null,
    onClick: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val highlighted = isSelected || isRangeStart || isRangeEnd

    val bgColor = when {
        highlighted    -> cs.primary
        isRangeMiddle  -> cs.primary.copy(alpha = .12f)
        else           -> Color.Transparent
    }
    val shape = when {
        isRangeStart  -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
        isRangeEnd    -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
        isRangeMiddle -> RoundedCornerShape(0.dp)
        else          -> CircleShape
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(shape)
            .background(bgColor)
            .then(
                if (isToday && !highlighted)
                    Modifier.border(1.dp, cs.primary, CircleShape)
                else Modifier
            )
            .then(
                if (!isDisabled && !isBooked)
                    Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text           = dayLabel,
                fontSize       = if (dayContent != null) 11.sp else 13.sp,
                color          = when {
                    highlighted              -> cs.onPrimary
                    isOutside || isDisabled  -> cs.onSurface.copy(if (isBooked) .35f else .3f)
                    isToday                  -> cs.primary
                    else                     -> cs.onSurface
                },
                fontWeight     = if (highlighted || isToday) FontWeight.SemiBold else FontWeight.Normal,
                textDecoration = if (isBooked) TextDecoration.LineThrough else null,
                textAlign      = TextAlign.Center
            )
            if (dayContent != null && !isOutside) dayContent(date)
        }
    }
}

// ─────────────────────────────────────────────
//  Internal month grid
// ─────────────────────────────────────────────

private val DaySize = 36.dp

@Composable
private fun KCalendarMonth(
    month: CalMonth,
    adapter: CalendarAdapter,
    captionLayout: KCalendarCaptionLayout,
    mode: KCalendarMode,
    selected: LocalDate?,
    onSelectSingle: ((LocalDate?) -> Unit)?,
    selectedRange: KDateRange,
    onSelectRange: ((KDateRange) -> Unit)?,
    showOutsideDays: Boolean,
    showWeekNumber: Boolean,
    disabledDates: Set<LocalDate>,
    bookedDates: Set<LocalDate>,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    dayContent: KCalendarDayContent?,
    showNavPrev: Boolean,
    showNavNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onMonthChange: (CalMonth) -> Unit,
    rtl: Boolean,
    modifier: Modifier = Modifier
) {
    val today        = LocalDate.now()
    val daysInMonth  = adapter.daysInMonth(month)
    val startOffset  = adapter.firstDayOfWeekOffset(month)

    Column(modifier = modifier.width(IntrinsicSize.Min)) {
        when (captionLayout) {
            KCalendarCaptionLayout.Label    -> CalendarCaptionLabel(
                label = adapter.monthLabel(month),
                showPrev = showNavPrev, showNext = showNavNext,
                onPrev = onPrev, onNext = onNext, rtl = rtl
            )
            KCalendarCaptionLayout.Dropdown -> CalendarCaptionDropdown(
                month = month, adapter = adapter,
                showPrev = showNavPrev, showNext = showNavNext,
                onPrev = onPrev, onNext = onNext,
                onMonthChange = onMonthChange, rtl = rtl
            )
        }

        Spacer(Modifier.height(4.dp))

        // Weekday header
        Row(Modifier.fillMaxWidth()) {
            if (showWeekNumber) CalendarWeekCell(text = "")
            repeat(7) { i -> CalendarWeekCell(text = adapter.weekdayNarrow(i)) }
        }

        // Day grid
        val rowCount = (startOffset + daysInMonth + 6) / 7
        for (row in 0 until rowCount) {
            Row(Modifier.fillMaxWidth()) {
                if (showWeekNumber) {
                    val dayNum  = (row * 7 + 1 - startOffset).coerceIn(1, daysInMonth)
                    val refDate = adapter.toLocalDate(month, dayNum)
                    CalendarWeekCell(text = adapter.weekNumber(refDate).toString(), muted = true)
                }
                repeat(7) { col ->
                    val dayNum = row * 7 + col - startOffset + 1
                    when {
                        dayNum < 1 || dayNum > daysInMonth -> {
                            if (showOutsideDays) {
                                val outsideDate = if (dayNum < 1) {
                                    val prev = adapter.plusMonths(month, -1)
                                    adapter.toLocalDate(prev, adapter.daysInMonth(prev) + dayNum)
                                } else {
                                    adapter.toLocalDate(adapter.plusMonths(month, 1), dayNum - daysInMonth)
                                }
                                CalendarDayCellInternal(
                                    date = outsideDate, mode = mode, selected = selected,
                                    selectedRange = selectedRange, today = today,
                                    outside = true, disabled = true, booked = false,
                                    dayContent = dayContent, adapter = adapter,
                                    onSelectSingle = onSelectSingle, onSelectRange = onSelectRange
                                )
                            } else {
                                Spacer(Modifier.size(DaySize))
                            }
                        }
                        else -> {
                            val date     = adapter.toLocalDate(month, dayNum)
                            val disabled = date in disabledDates ||
                                    (minDate != null && date < minDate) ||
                                    (maxDate != null && date > maxDate)
                            CalendarDayCellInternal(
                                date = date, mode = mode, selected = selected,
                                selectedRange = selectedRange, today = today,
                                outside = false, disabled = disabled,
                                booked = date in bookedDates,
                                dayContent = dayContent, adapter = adapter,
                                onSelectSingle = onSelectSingle, onSelectRange = onSelectRange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCellInternal(
    date: LocalDate,
    mode: KCalendarMode,
    selected: LocalDate?,
    selectedRange: KDateRange,
    today: LocalDate,
    outside: Boolean,
    disabled: Boolean,
    booked: Boolean,
    dayContent: KCalendarDayContent?,
    adapter: CalendarAdapter,
    onSelectSingle: ((LocalDate?) -> Unit)?,
    onSelectRange: ((KDateRange) -> Unit)?
) {
    val (_, _, dayNum) = adapter.fromLocalDate(date)
    val isSelectedSingle = mode is KCalendarMode.Single && date == selected
    val isRangeStart     = mode is KCalendarMode.Range  && date == selectedRange.from
    val isRangeEnd       = mode is KCalendarMode.Range  && date == selectedRange.to
    val isRangeMiddle    = mode is KCalendarMode.Range
            && selectedRange.from != null && selectedRange.to != null
            && date > selectedRange.from && date < selectedRange.to

    KCalendarDayButton(
        date          = date,
        dayLabel      = dayNum.toString(),
        isSelected    = isSelectedSingle,
        isRangeStart  = isRangeStart,
        isRangeEnd    = isRangeEnd,
        isRangeMiddle = isRangeMiddle,
        isToday       = date == today,
        isDisabled    = disabled,
        isBooked      = booked,
        isOutside     = outside,
        dayContent    = dayContent,
        onClick       = {
            when (mode) {
                is KCalendarMode.Single ->
                    onSelectSingle?.invoke(if (isSelectedSingle) null else date)
                is KCalendarMode.Range -> {
                    val from = selectedRange.from; val to = selectedRange.to
                    when {
                        from == null || to != null ->
                            onSelectRange?.invoke(KDateRange(from = date))
                        date < from ->
                            onSelectRange?.invoke(KDateRange(from = date, to = from))
                        else ->
                            onSelectRange?.invoke(KDateRange(from = from, to = date))
                    }
                }
            }
        }
    )
}

// ─────────────────────────────────────────────
//  Caption helpers — use KButton for nav arrows
// ─────────────────────────────────────────────

@Composable
private fun CalendarCaptionLabel(
    label: String,
    showPrev: Boolean,
    showNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    rtl: Boolean
) {
    Box(Modifier.fillMaxWidth().height(28.dp)) {
        if (showPrev) {
            KButton(
                onClick  = onPrev,
                variant  = KButtonVariant.Ghost,
                size     = KButtonSize.Icon,
                modifier = Modifier.align(Alignment.CenterStart).size(28.dp)
            ) {
                Icon(
                    if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowRight
                    else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text       = label,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.align(Alignment.Center)
        )
        if (showNext) {
            KButton(
                onClick  = onNext,
                variant  = KButtonVariant.Ghost,
                size     = KButtonSize.Icon,
                modifier = Modifier.align(Alignment.CenterEnd).size(28.dp)
            ) {
                Icon(
                    if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CalendarCaptionDropdown(
    month: CalMonth,
    adapter: CalendarAdapter,
    showPrev: Boolean,
    showNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onMonthChange: (CalMonth) -> Unit,
    rtl: Boolean
) {
    var monthMenu by remember { mutableStateOf(false) }
    var yearMenu  by remember { mutableStateOf(false) }

    Row(
        modifier          = Modifier.fillMaxWidth().height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showPrev) {
            KButton(onClick = onPrev, variant = KButtonVariant.Ghost, size = KButtonSize.Icon,
                modifier = Modifier.size(28.dp)) {
                Icon(
                    if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowRight
                    else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous", modifier = Modifier.size(16.dp)
                )
            }
        } else Spacer(Modifier.size(28.dp))

        Spacer(Modifier.weight(1f))

        // Month dropdown
        Box {
            TextButton(onClick = { monthMenu = true }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(
                    (adapter as? GregorianAdapter)?.let {
                        java.time.Month.of(month.month)
                            .getDisplayName(JTextStyle.SHORT, it.locale)
                            .replaceFirstChar { c -> c.uppercase() }
                    } ?: month.month.toString(),
                    fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(14.dp))
            }
            DropdownMenu(expanded = monthMenu, onDismissRequest = { monthMenu = false }) {
                (1..12).forEach { m ->
                    DropdownMenuItem(
                        text    = { Text((adapter as? GregorianAdapter)?.let {
                            java.time.Month.of(m).getDisplayName(JTextStyle.SHORT, it.locale)
                                .replaceFirstChar { c -> c.uppercase() }
                        } ?: m.toString(), fontSize = 13.sp) },
                        onClick = { onMonthChange(month.copy(month = m)); monthMenu = false }
                    )
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        // Year dropdown
        Box {
            TextButton(onClick = { yearMenu = true }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(month.year.toString(), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(14.dp))
            }
            DropdownMenu(expanded = yearMenu, onDismissRequest = { yearMenu = false },
                modifier = Modifier.height(200.dp)) {
                val curYear = adapter.yearMonthOf(LocalDate.now()).year
                ((curYear - 10)..(curYear + 10)).forEach { y ->
                    DropdownMenuItem(
                        text    = { Text(y.toString(), fontSize = 13.sp) },
                        onClick = { onMonthChange(month.copy(year = y)); yearMenu = false }
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (showNext) {
            KButton(onClick = onNext, variant = KButtonVariant.Ghost, size = KButtonSize.Icon,
                modifier = Modifier.size(28.dp)) {
                Icon(
                    if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next", modifier = Modifier.size(16.dp)
                )
            }
        } else Spacer(Modifier.size(28.dp))
    }
}

@Composable
private fun CalendarWeekCell(text: String, muted: Boolean = false) {
    Box(Modifier.size(DaySize), contentAlignment = Alignment.Center) {
        Text(
            text      = text,
            fontSize  = 11.sp,
            color     = if (muted) MaterialTheme.colorScheme.onSurface.copy(.4f)
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalendarPresetRow(
    presets: List<KCalendarPreset>,
    onSelect: (KCalendarPreset) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        presets.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { preset ->
                    KButton(
                        onClick  = { onSelect(preset) },
                        variant  = KButtonVariant.Outline,
                        modifier = Modifier.weight(1f)
                    ) { Text(preset.label, fontSize = 12.sp, maxLines = 1) }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Calendar adapter (updated signatures)
// ─────────────────────────────────────────────

data class CalMonth(val year: Int, val month: Int)

internal interface CalendarAdapter {
    fun yearMonthOf(date: LocalDate): CalMonth
    fun plusMonths(m: CalMonth, delta: Int): CalMonth
    fun daysInMonth(m: CalMonth): Int
    fun firstDayOfWeekOffset(m: CalMonth): Int
    fun toLocalDate(m: CalMonth, day: Int): LocalDate
    fun fromLocalDate(date: LocalDate): Triple<Int, Int, Int>
    fun monthLabel(m: CalMonth): String
    fun weekdayNarrow(dowIndex: Int): String
    fun weekNumber(date: LocalDate): Int
    fun isRtl(): Boolean
}

@Composable
private fun rememberCalendarAdapter(locale: KCalendarLocale): CalendarAdapter =
    remember(locale) {
        when (locale) {
            is KCalendarLocale.Standard -> GregorianAdapter(locale.locale)
            is KCalendarLocale.Persian  -> PersianAdapter(locale.locale)
            is KCalendarLocale.Hijri    -> HijriAdapter(locale.locale)
        }
    }

internal class GregorianAdapter(val locale: Locale) : CalendarAdapter {
    override fun yearMonthOf(date: LocalDate) = CalMonth(date.year, date.monthValue)
    override fun plusMonths(m: CalMonth, delta: Int): CalMonth {
        val ym = YearMonth.of(m.year, m.month).plusMonths(delta.toLong())
        return CalMonth(ym.year, ym.monthValue)
    }
    override fun daysInMonth(m: CalMonth) = YearMonth.of(m.year, m.month).lengthOfMonth()
    override fun firstDayOfWeekOffset(m: CalMonth) =
        LocalDate.of(m.year, m.month, 1).dayOfWeek.value - 1
    override fun toLocalDate(m: CalMonth, day: Int) = LocalDate.of(m.year, m.month, day)
    override fun fromLocalDate(date: LocalDate) =
        Triple(date.year, date.monthValue, date.dayOfMonth)
    override fun monthLabel(m: CalMonth): String {
        val name = YearMonth.of(m.year, m.month).month
            .getDisplayName(JTextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
        return "$name ${m.year}"
    }
    override fun weekdayNarrow(dowIndex: Int) =
        DayOfWeek.of(dowIndex + 1).getDisplayName(JTextStyle.NARROW, locale)
    override fun weekNumber(date: LocalDate) =
        date.get(java.time.temporal.WeekFields.of(locale).weekOfWeekBasedYear())
    override fun isRtl() = locale.language in setOf("ar", "he", "fa", "ur", "ps")
}

internal class PersianAdapter(val locale: Locale) : CalendarAdapter {
    override fun isRtl() = true
    private fun toJalali(d: LocalDate): Triple<Int,Int,Int> =
        jdnToJalali(gregorianToJdn(d.year, d.monthValue, d.dayOfMonth))
    private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): LocalDate {
        val (gy,gm,gd) = jdnToGregorian(jalaliToJdn(jy,jm,jd)); return LocalDate.of(gy,gm,gd)
    }
    override fun yearMonthOf(date: LocalDate) = toJalali(date).let { CalMonth(it.first, it.second) }
    override fun plusMonths(m: CalMonth, delta: Int): CalMonth {
        var y = m.year; var mo = m.month + delta
        while (mo > 12) { mo -= 12; y++ }; while (mo < 1) { mo += 12; y-- }
        return CalMonth(y, mo)
    }
    override fun daysInMonth(m: CalMonth) =
        if (m.month <= 6) 31 else if (m.month <= 11) 30
        else if (isLeap(m.year)) 30 else 29
    override fun firstDayOfWeekOffset(m: CalMonth): Int {
        val iso = jalaliToGregorian(m.year, m.month, 1).dayOfWeek.value
        return when (iso) { 6 -> 0; 7 -> 1; else -> iso + 1 }
    }
    override fun toLocalDate(m: CalMonth, day: Int) = jalaliToGregorian(m.year, m.month, day)
    override fun fromLocalDate(date: LocalDate) = toJalali(date)
    override fun monthLabel(m: CalMonth): String {
        val names = persianMonthNames(locale)
        return "${names[m.month - 1]} ${toFarsi(m.year, locale)}"
    }
    override fun weekdayNarrow(dowIndex: Int) = if (locale.language == "fa")
        arrayOf("ش","ی","د","س","چ","پ","ج")[dowIndex]
    else arrayOf("Sa","Su","Mo","Tu","We","Th","Fr")[dowIndex]
    override fun weekNumber(date: LocalDate): Int {
        val (_,jm,jd) = toJalali(date); return ((jm-1)*30+jd)/7+1
    }
    private fun gregorianToJdn(y:Int,m:Int,d:Int):Long{val a=(14-m)/12;val yr=y+4800-a;val mo=m+12*a-3;return d+(153*mo+2)/5+365L*yr+yr/4-yr/100+yr/400-32045}
    private fun jdnToGregorian(jdn:Long):Triple<Int,Int,Int>{val a=jdn+32044;val b=(4*a+3)/146097;val c=a-(146097*b)/4;val d=(4*c+3)/1461;val e=c-(1461*d)/4;val mo=(5*e+2)/153;val day=(e-(153*mo+2)/5+1).toInt();val month=(mo+3-12*(mo/10)).toInt();val year=(100*b+d-4800+mo/10).toInt();return Triple(year,month,day)}
    private fun jalaliToJdn(jy:Int,jm:Int,jd:Int):Long{val ep=jy-if(jy>=0)474 else 473;val year=474+ep.mod(2820);return jd+(if(jm<=6)(jm-1)*31 else(jm-1)*30+6).toLong()+(year*682-110)/2816+(year-1)*365L+ep/2820*1029983+1948319L}
    private fun jdnToJalali(jdn:Long):Triple<Int,Int,Int>{val depoch=jdn-jalaliToJdn(475,1,1);val(cycle,cyear0)=depoch.divmod(1029983);val ycycle:Long=if(cyear0==1029982L)2819 else{val a1=cyear0/366;val a2=cyear0.mod(366);(2134*a1+2816*a2+2815)/1028522+a1+1};var year=(ycycle+2820*cycle+474).toInt();if(year<=0)year--;val yday=(jdn-jalaliToJdn(year,1,1)+1).toInt();val month=if(yday<=186)(yday-1)/31+1 else(yday-7)/30+1;val day=(jdn-jalaliToJdn(year,month,1)+1).toInt();return Triple(year,month,day)}
    private fun isLeap(year:Int):Boolean{val y=if(year>0)year else year+1;return((y-474).mod(2820)+474+38)*682%2816<682}
    private fun Long.divmod(d:Long)=Pair(this/d,this.mod(d))
}

internal class HijriAdapter(val locale: Locale) : CalendarAdapter {
    override fun isRtl() = true
    private fun toHijri(d: LocalDate) = jdnToHijri(gregorianToJdn(d.year, d.monthValue, d.dayOfMonth))
    private fun gregorianToJdn(y:Int,m:Int,d:Int):Long{val a=(14-m)/12;val yr=y+4800-a;val mo=m+12*a-3;return d+(153*mo+2)/5+365L*yr+yr/4-yr/100+yr/400-32045}
    private fun jdnToHijri(jdn:Long):Triple<Int,Int,Int>{val l=jdn-1948440+10632;val n=(l-1)/10631;val l2=l-10631*n+354;val j=(10985-l2)/5316*(50*l2)/17719+(l2/5670)*(43*l2)/15238;val l3=l2-(30-j)/15*(17719*j)/50-(j/16)*(15238*j)/43+29;val month=(24*l3)/709;val day=l3-(709*month)/24;val year=30*n+j-30;return Triple(year.toInt(),month.toInt(),day.toInt())}
    private fun hijriToJdn(hy:Int,hm:Int,hd:Int):Long=(11*hy+3).toLong()/30+354*hy+30*hm-(hm-1)/2+hd+1948440-385
    private fun hijriToGregorian(hy:Int,hm:Int,hd:Int):LocalDate{val jdn=hijriToJdn(hy,hm,hd);val l=jdn+68569;val n=4*l/146097;val l2=l-(146097*n+3)/4;val i=4000*(l2+1)/1461001;val l3=l2-1461*i/4+31;val j=80*l3/2447;val day=(l3-2447*j/80).toInt();val l4=j/11;val month=(j+2-12*l4).toInt();val year=(100*(n-49)+i+l4).toInt();return LocalDate.of(year,month,day)}
    override fun yearMonthOf(date: LocalDate) = toHijri(date).let { CalMonth(it.first, it.second) }
    override fun plusMonths(m: CalMonth, delta: Int): CalMonth {
        var y = m.year; var mo = m.month + delta
        while (mo > 12) { mo -= 12; y++ }; while (mo < 1) { mo += 12; y-- }
        return CalMonth(y, mo)
    }
    override fun daysInMonth(m: CalMonth): Int {
        val leap = (11 * m.year + 14).mod(30) < 11
        return when { m.month%2==1->30; m.month==12&&leap->30; else->29 }
    }
    override fun firstDayOfWeekOffset(m: CalMonth): Int {
        val iso = hijriToGregorian(m.year,m.month,1).dayOfWeek.value
        return if(iso==7) 0 else iso
    }
    override fun toLocalDate(m: CalMonth, day: Int) = hijriToGregorian(m.year,m.month,day)
    override fun fromLocalDate(date: LocalDate) = toHijri(date)
    override fun monthLabel(m: CalMonth) = "${hijriMonthNames(locale)[m.month-1]} ${m.year}"
    override fun weekdayNarrow(dowIndex: Int) = if(locale.language=="ar")
        arrayOf("ح","ن","ث","ر","خ","ج","س")[dowIndex]
    else arrayOf("Su","Mo","Tu","We","Th","Fr","Sa")[dowIndex]
    override fun weekNumber(date: LocalDate): Int { val(_,hm,hd)=toHijri(date); return((hm-1)*30+hd)/7+1 }
}

private fun persianMonthNames(locale: Locale) = if (locale.language == "fa")
    arrayOf("فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند")
else arrayOf("Farvardin","Ordibehesht","Khordad","Tir","Mordad","Shahrivar","Mehr","Aban","Azar","Dey","Bahman","Esfand")

private fun hijriMonthNames(locale: Locale) = if (locale.language == "ar")
    arrayOf("محرم","صفر","ربيع الأول","ربيع الثاني","جمادى الأولى","جمادى الآخرة","رجب","شعبان","رمضان","شوال","ذو القعدة","ذو الحجة")
else arrayOf("Muharram","Safar","Rabi al-Awwal","Rabi al-Thani","Jumada al-Awwal","Jumada al-Thani","Rajab","Sha'ban","Ramadan","Shawwal","Dhu al-Qi'dah","Dhu al-Hijjah")

private fun toFarsi(n: Int, locale: Locale): String {
    if (locale.language != "fa") return n.toString()
    return n.toString().map { if (it.isDigit()) '۰' + (it - '0') else it }.joinToString("")
}
