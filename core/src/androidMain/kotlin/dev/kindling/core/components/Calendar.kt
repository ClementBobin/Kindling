package dev.kindling.core.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

// ─────────────────────────────────────────────
//  Calendar locale abstraction
// ─────────────────────────────────────────────

/**
 * Locale system used by [KCalendar].
 *
 * [Standard] wraps a [java.util.Locale] — covers all Gregorian-based locales
 * and locale-aware display (e.g. Arabic months in Gregorian).
 *
 * [Persian] uses the Solar Hijri (Shamsi / Jalali) calendar system.
 * [Hijri]   uses the Islamic (Lunar Hijri) calendar system.
 *
 * Both non-Gregorian systems are approximated via [java.util.Calendar]
 * with the appropriate calendar type so the library stays dependency-free.
 */
sealed interface KCalendarLocale {
    /** Standard Gregorian locale. [locale] drives all display strings. */
    data class Standard(val locale: Locale = Locale.getDefault()) : KCalendarLocale

    /** Solar Hijri / Jalali / Shamsi calendar (used in Iran/Afghanistan). */
    data class Persian(val locale: Locale = Locale("fa", "IR")) : KCalendarLocale

    /** Islamic Lunar Hijri calendar. */
    data class Hijri(val locale: Locale = Locale("ar", "SA")) : KCalendarLocale
}

// ─────────────────────────────────────────────
//  Caption layout
// ─────────────────────────────────────────────

/**
 * Controls how the month/year header is rendered.
 *
 * [Label]    — plain text, navigation arrows on each side (default).
 * [Dropdown] — month and year are tappable dropdowns (image 1 & 7 style).
 */
enum class KCalendarCaptionLayout { Label, Dropdown }

// ─────────────────────────────────────────────
//  Selection modes (re-exported for back-compat)
// ─────────────────────────────────────────────

sealed interface KCalendarMode {
    object Single : KCalendarMode
    object Range  : KCalendarMode
}

data class KDateRange(
    val from: LocalDate? = null,
    val to:   LocalDate? = null
)

// ─────────────────────────────────────────────
//  Time picker state
// ─────────────────────────────────────────────

/** Holds start/end time for the time-picker variant (image 5). */
data class KCalendarTimeRange(
    val startTime: LocalTime = LocalTime.of(10, 30),
    val endTime:   LocalTime = LocalTime.of(12, 30)
)

// ─────────────────────────────────────────────
//  Preset
// ─────────────────────────────────────────────

/** A named preset button shown below the calendar grid (image 4). */
data class KCalendarPreset(
    val label: String,
    val date:  LocalDate
)

// ─────────────────────────────────────────────
//  Day content slot
// ─────────────────────────────────────────────

/** Custom content rendered below the day number in each cell (image 7). */
typealias KCalendarDayContent = @Composable (date: LocalDate) -> Unit

// ─────────────────────────────────────────────
//  Main KCalendar
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Calendar component.
 *
 * Supports:
 * - Single and range date selection
 * - Dropdown or label caption layout
 * - Week numbers column (image 8)
 * - Outside days display
 * - Booked / disabled dates with strikethrough (image 6)
 * - Per-cell custom content slot (image 7 — prices etc.)
 * - Preset buttons row (image 4)
 * - Start/End time pickers (image 5)
 * - Multi-month display (image 3)
 * - Persian (Jalali/Shamsi), Hijri, and standard Gregorian locales
 *
 * ```kotlin
 * // Basic single selection
 * var date by remember { mutableStateOf<LocalDate?>(null) }
 * KCalendar(selected = date, onSelectSingle = { date = it })
 *
 * // Range + dropdown caption
 * var range by remember { mutableStateOf(KDateRange()) }
 * KCalendar(
 *     mode          = KCalendarMode.Range,
 *     selectedRange = range,
 *     onSelectRange = { range = it },
 *     captionLayout = KCalendarCaptionLayout.Dropdown
 * )
 *
 * // Persian locale
 * KCalendar(
 *     selected      = date,
 *     onSelectSingle = { date = it },
 *     calendarLocale = KCalendarLocale.Persian()
 * )
 *
 * // With presets
 * KCalendar(
 *     selected = date,
 *     onSelectSingle = { date = it },
 *     presets = listOf(
 *         KCalendarPreset("Today",     LocalDate.now()),
 *         KCalendarPreset("Tomorrow",  LocalDate.now().plusDays(1)),
 *         KCalendarPreset("In 3 days", LocalDate.now().plusDays(3)),
 *     )
 * )
 *
 * // With time pickers
 * var timeRange by remember { mutableStateOf(KCalendarTimeRange()) }
 * KCalendar(
 *     selected      = date,
 *     onSelectSingle = { date = it },
 *     timeRange     = timeRange,
 *     onTimeRangeChange = { timeRange = it }
 * )
 *
 * // Per-cell price content
 * KCalendar(
 *     selected = date,
 *     onSelectSingle = { date = it },
 *     dayContent = { d -> Text("\$100", fontSize = 9.sp) }
 * )
 * ```
 */
@Composable
fun KCalendar(
    modifier: Modifier = Modifier,
    // ── Selection ─────────────────────────────
    mode: KCalendarMode = KCalendarMode.Single,
    selected: LocalDate? = null,
    onSelectSingle: ((LocalDate?) -> Unit)? = null,
    selectedRange: KDateRange = KDateRange(),
    onSelectRange: ((KDateRange) -> Unit)? = null,
    // ── Display ───────────────────────────────
    captionLayout: KCalendarCaptionLayout = KCalendarCaptionLayout.Label,
    calendarLocale: KCalendarLocale = KCalendarLocale.Standard(),
    showOutsideDays: Boolean = true,
    showWeekNumber: Boolean = false,
    numberOfMonths: Int = 1,
    // ── Disabled / booked dates ───────────────
    disabledDates: Set<LocalDate> = emptySet(),
    bookedDates: Set<LocalDate> = emptySet(),
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    // ── Extra UI ──────────────────────────────
    presets: List<KCalendarPreset> = emptyList(),
    timeRange: KCalendarTimeRange? = null,
    onTimeRangeChange: ((KCalendarTimeRange) -> Unit)? = null,
    dayContent: KCalendarDayContent? = null
) {
    val adapter = rememberCalendarAdapter(calendarLocale)

    var currentMonth by remember {
        mutableStateOf(
            adapter.yearMonthOf(
                when {
                    selected != null        -> selected
                    selectedRange.from != null -> selectedRange.from
                    else                    -> LocalDate.now()
                }
            )
        )
    }

    Column(modifier = modifier) {
        // ── Month grids ───────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(numberOfMonths) { offset ->
                val month = adapter.plusMonths(currentMonth, offset)
                KCalendarMonth(
                    month          = month,
                    adapter        = adapter,
                    captionLayout  = captionLayout,
                    mode           = mode,
                    selected       = selected,
                    onSelectSingle = onSelectSingle,
                    selectedRange  = selectedRange,
                    onSelectRange  = onSelectRange,
                    showOutsideDays = showOutsideDays,
                    showWeekNumber = showWeekNumber,
                    disabledDates  = disabledDates,
                    bookedDates    = bookedDates,
                    minDate        = minDate,
                    maxDate        = maxDate,
                    dayContent     = dayContent,
                    showNavPrev    = offset == 0,
                    showNavNext    = offset == numberOfMonths - 1,
                    onPrev         = { currentMonth = adapter.plusMonths(currentMonth, -1) },
                    onNext         = { currentMonth = adapter.plusMonths(currentMonth, 1) },
                    onMonthChange  = { currentMonth = it },
                    modifier       = Modifier.weight(1f)
                )
            }
        }

        // ── Presets ───────────────────────────────────────────────────────
        if (presets.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PresetRow(
                presets = presets,
                onSelect = { preset ->
                    onSelectSingle?.invoke(preset.date)
                    currentMonth = adapter.yearMonthOf(preset.date)
                }
            )
        }

        // ── Time pickers ──────────────────────────────────────────────────
        if (timeRange != null && onTimeRangeChange != null) {
            Spacer(Modifier.height(12.dp))
            TimeRangePicker(
                timeRange = timeRange,
                onChange  = onTimeRangeChange
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Calendar adapter — calendar-system abstraction
// ─────────────────────────────────────────────

/** Opaque month token (year + month in the calendar system). */
data class CalMonth(val year: Int, val month: Int)

private interface CalendarAdapter {
    fun yearMonthOf(date: LocalDate): CalMonth
    fun plusMonths(m: CalMonth, delta: Int): CalMonth
    fun daysInMonth(m: CalMonth): Int
    fun firstDayOfWeekOffset(m: CalMonth): Int   // 0 = Mon … 6 = Sun
    fun toLocalDate(m: CalMonth, day: Int): LocalDate
    fun fromLocalDate(date: LocalDate): Triple<Int, Int, Int> // year, month, day
    fun monthLabel(m: CalMonth): String
    fun weekdayNarrow(dowIndex: Int): String      // 0=Mon…6=Sun
    fun weekNumber(date: LocalDate): Int
    fun isRtl(): Boolean
}

@Composable
private fun rememberCalendarAdapter(locale: KCalendarLocale): CalendarAdapter {
    return remember(locale) {
        when (locale) {
            is KCalendarLocale.Standard -> GregorianAdapter(locale.locale)
            is KCalendarLocale.Persian  -> PersianAdapter(locale.locale)
            is KCalendarLocale.Hijri    -> HijriAdapter(locale.locale)
        }
    }
}

// ── Gregorian adapter ─────────────────────────

private class GregorianAdapter(val locale: Locale) : CalendarAdapter {
    override fun yearMonthOf(date: LocalDate) = CalMonth(date.year, date.monthValue)
    override fun plusMonths(m: CalMonth, delta: Int): CalMonth {
        val ym = YearMonth.of(m.year, m.month).plusMonths(delta.toLong())
        return CalMonth(ym.year, ym.monthValue)
    }
    override fun daysInMonth(m: CalMonth) = YearMonth.of(m.year, m.month).lengthOfMonth()
    override fun firstDayOfWeekOffset(m: CalMonth): Int {
        val first = LocalDate.of(m.year, m.month, 1)
        return (first.dayOfWeek.value - 1) // Mon=0
    }
    override fun toLocalDate(m: CalMonth, day: Int) = LocalDate.of(m.year, m.month, day)
    override fun fromLocalDate(date: LocalDate) = Triple(date.year, date.monthValue, date.dayOfMonth)
    override fun monthLabel(m: CalMonth): String {
        val ym = YearMonth.of(m.year, m.month)
        val monthName = ym.month.getDisplayName(JTextStyle.FULL, locale)
            .replaceFirstChar { it.uppercase() }
        return "$monthName ${m.year}"
    }
    override fun weekdayNarrow(dowIndex: Int): String =
        DayOfWeek.of(dowIndex + 1).getDisplayName(JTextStyle.NARROW, locale)
    override fun weekNumber(date: LocalDate): Int =
        date.get(java.time.temporal.WeekFields.of(locale).weekOfWeekBasedYear())
    override fun isRtl() = locale.language in setOf("ar", "he", "fa", "ur", "ps")
}

// ── Persian (Solar Hijri / Jalali) adapter ────
// Uses java.util.Calendar with type "persian" via ULocale trick-free approach:
// We compute Jalali dates algorithmically (no extra dependency).

private class PersianAdapter(val locale: Locale) : CalendarAdapter {
    override fun isRtl() = true

    // Jalali conversion algorithm
    private fun toJalali(date: LocalDate): Triple<Int, Int, Int> {
        val jd = gregorianToJdn(date.year, date.monthValue, date.dayOfMonth)
        return jdnToJalali(jd)
    }
    private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): LocalDate {
        val jdn = jalaliToJdn(jy, jm, jd)
        val (gy, gm, gdd) = jdnToGregorian(jdn)
        return LocalDate.of(gy, gm, gdd)
    }

    override fun yearMonthOf(date: LocalDate): CalMonth {
        val (jy, jm, _) = toJalali(date)
        return CalMonth(jy, jm)
    }
    override fun plusMonths(m: CalMonth, delta: Int): CalMonth {
        var y = m.year; var mo = m.month + delta
        while (mo > 12) { mo -= 12; y++ }
        while (mo < 1)  { mo += 12; y-- }
        return CalMonth(y, mo)
    }
    override fun daysInMonth(m: CalMonth): Int {
        return if (m.month <= 6) 31
        else if (m.month <= 11) 30
        else if (isJalaliLeap(m.year)) 30 else 29
    }
    override fun firstDayOfWeekOffset(m: CalMonth): Int {
        val greg = jalaliToGregorian(m.year, m.month, 1)
        // In Persian calendar week starts Saturday (=6 in ISO), map to 0-based Sat=0
        val iso = greg.dayOfWeek.value // Mon=1…Sun=7
        // Sat=6→0, Sun=7→1, Mon=1→2, Tue=2→3, Wed=3→4, Thu=4→5, Fri=5→6
        return when (iso) { 6 -> 0; 7 -> 1; else -> iso + 1 }
    }
    override fun toLocalDate(m: CalMonth, day: Int) = jalaliToGregorian(m.year, m.month, day)
    override fun fromLocalDate(date: LocalDate): Triple<Int, Int, Int> = toJalali(date)
    override fun monthLabel(m: CalMonth): String {
        val names = persianMonthNames(locale)
        return "${names[m.month - 1]} ${toFarsiDigits(m.year, locale)}"
    }
    override fun weekdayNarrow(dowIndex: Int): String {
        // dowIndex 0=Sat…6=Fri in Persian week
        return if (locale.language == "fa") {
            arrayOf("ش", "ی", "د", "س", "چ", "پ", "ج")[dowIndex]
        } else {
            arrayOf("Sa", "Su", "Mo", "Tu", "We", "Th", "Fr")[dowIndex]
        }
    }
    override fun weekNumber(date: LocalDate): Int {
        val (jy, jm, jd) = toJalali(date)
        return ((jm - 1) * 30 + jd) / 7 + 1
    }

    // ── Jalali math ───────────────────────────────────────────────────────

    private fun gregorianToJdn(y: Int, m: Int, d: Int): Long {
        val a = (14 - m) / 12
        val yr = y + 4800 - a
        val mo = m + 12 * a - 3
        return d + (153 * mo + 2) / 5 + 365L * yr + yr / 4 - yr / 100 + yr / 400 - 32045
    }
    private fun jdnToGregorian(jdn: Long): Triple<Int, Int, Int> {
        val a = jdn + 32044
        val b = (4 * a + 3) / 146097
        val c = a - (146097 * b) / 4
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day   = (e - (153 * m + 2) / 5 + 1).toInt()
        val month = (m + 3 - 12 * (m / 10)).toInt()
        val year  = (100 * b + d - 4800 + m / 10).toInt()
        return Triple(year, month, day)
    }
    private fun jalaliToJdn(jy: Int, jm: Int, jd: Int): Long {
        val ep   = jy - if (jy >= 0) 474 else 473
        val year = 474 + ep.mod(2820)
        return jd +
                (if (jm <= 6) (jm - 1) * 31 else (jm - 1) * 30 + 6) +
                (year * 682 - 110) / 2816 +
                (year - 1) * 365 +
                ep / 2820 * 1029983 +
                (1948319).toLong()
    }
    private fun jdnToJalali(jdn: Long): Triple<Int, Int, Int> {
        val depoch = jdn - jalaliToJdn(475, 1, 1)
        val (cycle, cyear0) = depoch.divmod(1029983)
        val ycycle: Long
        if (cyear0 == 1029982L) {
            ycycle = 2819
        } else {
            val aux1 = cyear0 / 366
            val aux2 = cyear0.mod(366)
            ycycle = (2134 * aux1 + 2816 * aux2 + 2815) / 1028522 + aux1 + 1
        }
        var year = (ycycle + 2820 * cycle + 474).toInt()
        if (year <= 0) year--
        val yday = (jdn - jalaliToJdn(year, 1, 1) + 1).toInt()
        val month = if (yday <= 186) ((yday - 1) / 31 + 1) else ((yday - 7) / 30 + 1)
        val day = (jdn - jalaliToJdn(year, month, 1) + 1).toInt()
        return Triple(year, month, day)
    }
    private fun isJalaliLeap(year: Int): Boolean {
        val y = if (year > 0) year else year + 1
        return ((y - 474).mod(2820) + 474 + 38) * 682 % 2816 < 682
    }
    private fun Long.divmod(d: Long) = Pair(this / d, this.mod(d))
}

// ── Hijri (Islamic Lunar) adapter ─────────────

private class HijriAdapter(val locale: Locale) : CalendarAdapter {
    override fun isRtl() = true

    // Tabular Islamic calendar (civil epoch)
    private fun toHijri(date: LocalDate): Triple<Int, Int, Int> {
        val jdn = gregorianToJdn(date.year, date.monthValue, date.dayOfMonth)
        return jdnToHijri(jdn)
    }
    private fun gregorianToJdn(y: Int, m: Int, d: Int): Long {
        val a = (14 - m) / 12
        val yr = y + 4800 - a
        val mo = m + 12 * a - 3
        return d + (153 * mo + 2) / 5 + 365L * yr + yr / 4 - yr / 100 + yr / 400 - 32045
    }
    private fun jdnToHijri(jdn: Long): Triple<Int, Int, Int> {
        val l = jdn - 1948440 + 10632
        val n = (l - 1) / 10631
        val l2 = l - 10631 * n + 354
        val j = (10985 - l2) / 5316 * (50 * l2) / 17719 + (l2 / 5670) * (43 * l2) / 15238
        val l3 = l2 - (30 - j) / 15 * (17719 * j) / 50 - (j / 16) * (15238 * j) / 43 + 29
        val month = (24 * l3) / 709
        val day = l3 - (709 * month) / 24
        val year = 30 * n + j - 30
        return Triple(year.toInt(), month.toInt(), day.toInt())
    }
    private fun hijriToJdn(hy: Int, hm: Int, hd: Int): Long {
        return (11 * hy + 3).toLong() / 30 + 354 * hy + 30 * hm -
                (hm - 1) / 2 + hd + 1948440 - 385
    }
    private fun hijriToGregorian(hy: Int, hm: Int, hd: Int): LocalDate {
        val jdn = hijriToJdn(hy, hm, hd)
        val l = jdn + 68569
        val n = 4 * l / 146097
        val l2 = l - (146097 * n + 3) / 4
        val i = 4000 * (l2 + 1) / 1461001
        val l3 = l2 - 1461 * i / 4 + 31
        val j = 80 * l3 / 2447
        val day = (l3 - 2447 * j / 80).toInt()
        val l4 = j / 11
        val month = (j + 2 - 12 * l4).toInt()
        val year = (100 * (n - 49) + i + l4).toInt()
        return LocalDate.of(year, month, day)
    }

    override fun yearMonthOf(date: LocalDate): CalMonth {
        val (hy, hm, _) = toHijri(date); return CalMonth(hy, hm)
    }
    override fun plusMonths(m: CalMonth, delta: Int): CalMonth {
        var y = m.year; var mo = m.month + delta
        while (mo > 12) { mo -= 12; y++ }
        while (mo < 1)  { mo += 12; y-- }
        return CalMonth(y, mo)
    }
    override fun daysInMonth(m: CalMonth): Int {
        // Odd months 30 days, even months 29 days; last month of leap year 30
        val isLeap = (11 * m.year + 14).mod(30) < 11
        return when {
            m.month % 2 == 1           -> 30
            m.month == 12 && isLeap    -> 30
            else                       -> 29
        }
    }
    override fun firstDayOfWeekOffset(m: CalMonth): Int {
        val greg = hijriToGregorian(m.year, m.month, 1)
        // Arabic week starts Sunday
        val iso = greg.dayOfWeek.value // Mon=1…Sun=7
        return if (iso == 7) 0 else iso
    }
    override fun toLocalDate(m: CalMonth, day: Int) = hijriToGregorian(m.year, m.month, day)
    override fun fromLocalDate(date: LocalDate) = toHijri(date)
    override fun monthLabel(m: CalMonth): String {
        val names = hijriMonthNames(locale)
        return "${names[m.month - 1]} ${m.year}"
    }
    override fun weekdayNarrow(dowIndex: Int): String {
        return if (locale.language == "ar") {
            arrayOf("ح", "ن", "ث", "ر", "خ", "ج", "س")[dowIndex]
        } else {
            arrayOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")[dowIndex]
        }
    }
    override fun weekNumber(date: LocalDate): Int {
        val (_, hm, hd) = toHijri(date)
        return ((hm - 1) * 30 + hd) / 7 + 1
    }
}

// ─────────────────────────────────────────────
//  Single month grid
// ─────────────────────────────────────────────

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
    modifier: Modifier = Modifier
) {
    val cs    = MaterialTheme.colorScheme
    val today = LocalDate.now()
    val daysInMonth   = adapter.daysInMonth(month)
    val startOffset   = adapter.firstDayOfWeekOffset(month)
    val rtl           = adapter.isRtl()

    Column(modifier = modifier.width(IntrinsicSize.Min)) {

        // ── Caption ───────────────────────────────────────────────────────
        when (captionLayout) {
            KCalendarCaptionLayout.Label ->
                CaptionLabel(
                    label       = adapter.monthLabel(month),
                    showPrev    = showNavPrev,
                    showNext    = showNavNext,
                    onPrev      = onPrev,
                    onNext      = onNext,
                    rtl         = rtl
                )
            KCalendarCaptionLayout.Dropdown ->
                CaptionDropdown(
                    month       = month,
                    adapter     = adapter,
                    showPrev    = showNavPrev,
                    showNext    = showNavNext,
                    onPrev      = onPrev,
                    onNext      = onNext,
                    onMonthChange = onMonthChange,
                    rtl         = rtl
                )
        }

        Spacer(Modifier.height(4.dp))

        // ── Weekday header ────────────────────────────────────────────────
        val headerRow: @Composable () -> Unit = {
            if (showWeekNumber) WeekCell(text = "", cs = cs)
            repeat(7) { i -> WeekCell(text = adapter.weekdayNarrow(i), cs = cs) }
        }
        if (rtl) Row(Modifier.fillMaxWidth()) { /* mirror */ headerRow() }
        else     Row(Modifier.fillMaxWidth()) { headerRow() }

        // ── Day grid ──────────────────────────────────────────────────────
        val rowCount = (startOffset + daysInMonth + 6) / 7

        for (row in 0 until rowCount) {
            Row(Modifier.fillMaxWidth()) {
                // Week number column
                if (showWeekNumber) {
                    val dayNum = row * 7 + 1 - startOffset
                    val refDay = dayNum.coerceIn(1, daysInMonth)
                    val refDate = adapter.toLocalDate(month, refDay)
                    WeekCell(
                        text  = adapter.weekNumber(refDate).toString(),
                        cs    = cs,
                        muted = true
                    )
                }

                repeat(7) { col ->
                    val dayNum = row * 7 + col - startOffset + 1

                    if (dayNum < 1 || dayNum > daysInMonth) {
                        // Outside day
                        if (showOutsideDays) {
                            val outsideDate = when {
                                dayNum < 1 -> {
                                    val prev = adapter.plusMonths(month, -1)
                                    val prevDays = adapter.daysInMonth(prev)
                                    adapter.toLocalDate(prev, prevDays + dayNum)
                                }
                                else -> {
                                    val next = adapter.plusMonths(month, 1)
                                    adapter.toLocalDate(next, dayNum - daysInMonth)
                                }
                            }
                            CalendarDayCell(
                                date          = outsideDate,
                                mode          = mode,
                                selected      = selected,
                                selectedRange = selectedRange,
                                today         = today,
                                outside       = true,
                                disabled      = true,
                                booked        = false,
                                dayContent    = dayContent,
                                onSelectSingle = onSelectSingle,
                                onSelectRange  = onSelectRange,
                                adapter        = adapter
                            )
                        } else {
                            Spacer(Modifier.size(DaySize))
                        }
                    } else {
                        val date     = adapter.toLocalDate(month, dayNum)
                        val disabled = date in disabledDates ||
                                (minDate != null && date < minDate) ||
                                (maxDate != null && date > maxDate)
                        val booked   = date in bookedDates

                        CalendarDayCell(
                            date          = date,
                            mode          = mode,
                            selected      = selected,
                            selectedRange = selectedRange,
                            today         = today,
                            outside       = false,
                            disabled      = disabled,
                            booked        = booked,
                            dayContent    = dayContent,
                            onSelectSingle = onSelectSingle,
                            onSelectRange  = onSelectRange,
                            adapter        = adapter
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Caption helpers
// ─────────────────────────────────────────────

@Composable
private fun CaptionLabel(
    label: String,
    showPrev: Boolean,
    showNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    rtl: Boolean
) {
    Box(Modifier.fillMaxWidth().height(28.dp)) {
        if (showPrev) {
            val prevIcon = if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowRight
            else     Icons.AutoMirrored.Filled.KeyboardArrowLeft
            IconButton(onClick = onPrev, modifier = Modifier.align(Alignment.CenterStart).size(28.dp)) {
                Icon(prevIcon, contentDescription = "Previous", modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text       = label,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.align(Alignment.Center)
        )
        if (showNext) {
            val nextIcon = if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft
            else     Icons.AutoMirrored.Filled.KeyboardArrowRight
            IconButton(onClick = onNext, modifier = Modifier.align(Alignment.CenterEnd).size(28.dp)) {
                Icon(nextIcon, contentDescription = "Next", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun CaptionDropdown(
    month: CalMonth,
    adapter: CalendarAdapter,
    showPrev: Boolean,
    showNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onMonthChange: (CalMonth) -> Unit,
    rtl: Boolean
) {
    var monthMenuOpen by remember { mutableStateOf(false) }
    var yearMenuOpen  by remember { mutableStateOf(false) }

    Row(
        modifier          = Modifier.fillMaxWidth().height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showPrev) {
            val icon = if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowRight
            else     Icons.AutoMirrored.Filled.KeyboardArrowLeft
            IconButton(onClick = onPrev, modifier = Modifier.size(28.dp)) {
                Icon(icon, contentDescription = "Previous", modifier = Modifier.size(16.dp))
            }
        } else {
            Spacer(Modifier.size(28.dp))
        }

        Spacer(Modifier.weight(1f))

        // Month dropdown
        Box {
            TextButton(onClick = { monthMenuOpen = true }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                val monthName = if (adapter is GregorianAdapter) {
                    java.time.Month.of(month.month).getDisplayName(JTextStyle.SHORT, adapter.locale)
                        .replaceFirstChar { it.uppercase() }
                } else {
                    when (adapter) {
                        is PersianAdapter -> persianMonthNames(adapter.locale)[month.month - 1]
                        is HijriAdapter   -> hijriMonthNames(adapter.locale)[month.month - 1]
                        else -> month.month.toString()
                    }
                }
                Text(monthName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(14.dp))
            }
            DropdownMenu(expanded = monthMenuOpen, onDismissRequest = { monthMenuOpen = false }) {
                (1..12).forEach { m ->
                    val name = if (adapter is GregorianAdapter) {
                        java.time.Month.of(m).getDisplayName(JTextStyle.SHORT, adapter.locale)
                            .replaceFirstChar { it.uppercase() }
                    } else {
                        when (adapter) {
                            is PersianAdapter -> persianMonthNames(adapter.locale)[m - 1]
                            is HijriAdapter   -> hijriMonthNames(adapter.locale)[m - 1]
                            else -> m.toString()
                        }
                    }
                    DropdownMenuItem(
                        text    = { Text(name, fontSize = 13.sp) },
                        onClick = { onMonthChange(month.copy(month = m)); monthMenuOpen = false }
                    )
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        // Year dropdown
        Box {
            TextButton(onClick = { yearMenuOpen = true }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(month.year.toString(), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(14.dp))
            }
            DropdownMenu(expanded = yearMenuOpen, onDismissRequest = { yearMenuOpen = false },
                modifier = Modifier.height(200.dp)) {
                val currentYear = if (adapter is GregorianAdapter) LocalDate.now().year
                else adapter.yearMonthOf(LocalDate.now()).year
                ((currentYear - 10)..(currentYear + 10)).forEach { y ->
                    DropdownMenuItem(
                        text    = { Text(y.toString(), fontSize = 13.sp) },
                        onClick = { onMonthChange(month.copy(year = y)); yearMenuOpen = false }
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (showNext) {
            val icon = if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft
            else     Icons.AutoMirrored.Filled.KeyboardArrowRight
            IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
                Icon(icon, contentDescription = "Next", modifier = Modifier.size(16.dp))
            }
        } else {
            Spacer(Modifier.size(28.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  Day cell
// ─────────────────────────────────────────────

private val DaySize = 36.dp

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    mode: KCalendarMode,
    selected: LocalDate?,
    selectedRange: KDateRange,
    today: LocalDate,
    outside: Boolean,
    disabled: Boolean,
    booked: Boolean,
    dayContent: KCalendarDayContent?,
    onSelectSingle: ((LocalDate?) -> Unit)?,
    onSelectRange: ((KDateRange) -> Unit)?,
    adapter: CalendarAdapter
) {
    val cs = MaterialTheme.colorScheme

    val isSelectedSingle = mode is KCalendarMode.Single && date == selected
    val isRangeStart     = mode is KCalendarMode.Range && date == selectedRange.from
    val isRangeEnd       = mode is KCalendarMode.Range && date == selectedRange.to
    val isRangeMiddle    = mode is KCalendarMode.Range
            && selectedRange.from != null && selectedRange.to != null
            && date > selectedRange.from && date < selectedRange.to
    val isToday          = date == today
    val interactive      = !disabled && !booked

    val bgColor = when {
        isSelectedSingle || isRangeStart || isRangeEnd -> cs.primary
        isRangeMiddle                                   -> cs.primary.copy(alpha = 0.12f)
        else                                            -> Color.Transparent
    }
    val cellShape = when {
        isRangeStart  -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
        isRangeEnd    -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
        isRangeMiddle -> RoundedCornerShape(0.dp)
        else          -> CircleShape
    }

    val (_, _, dayNum) = adapter.fromLocalDate(date)

    Box(
        modifier = Modifier
            .size(DaySize)
            .clip(cellShape)
            .background(bgColor)
            .then(
                if (isToday && !isSelectedSingle && !isRangeStart && !isRangeEnd)
                    Modifier.border(1.dp, cs.primary, CircleShape)
                else Modifier
            )
            .then(
                if (interactive) Modifier.clickable {
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
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = dayNum.toString(),
                fontSize   = if (dayContent != null) 11.sp else 13.sp,
                color      = when {
                    isSelectedSingle || isRangeStart || isRangeEnd ->
                        cs.onPrimary
                    outside || disabled ->
                        cs.onSurface.copy(alpha = if (booked) 0.35f else 0.3f)
                    isToday -> cs.primary
                    else    -> cs.onSurface
                },
                fontWeight  = if (isSelectedSingle || isRangeStart || isRangeEnd || isToday)
                    FontWeight.SemiBold else FontWeight.Normal,
                textDecoration = if (booked) TextDecoration.LineThrough else null,
                textAlign   = TextAlign.Center
            )
            if (dayContent != null && !outside) {
                dayContent(date)
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Preset row
// ─────────────────────────────────────────────

@Composable
private fun PresetRow(
    presets: List<KCalendarPreset>,
    onSelect: (KCalendarPreset) -> Unit
) {
    val chunked = presets.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chunked.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { preset ->
                    OutlinedButton(
                        onClick  = { onSelect(preset) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(preset.label, fontSize = 12.sp, maxLines = 1)
                    }
                }
                // Fill remaining cells in last row
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Time range picker
// ─────────────────────────────────────────────

@Composable
private fun TimeRangePicker(
    timeRange: KCalendarTimeRange,
    onChange: (KCalendarTimeRange) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            "Start Time" to timeRange.startTime,
            "End Time"   to timeRange.endTime
        ).forEachIndexed { index, (label, time) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cs.onSurface)
                Surface(
                    shape  = RoundedCornerShape(6.dp),
                    color  = cs.surfaceVariant,
                    border = BorderStroke(1.dp, cs.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = androidx.compose.material.icons.Icons.Default.DateRange,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp),
                            tint               = cs.onSurfaceVariant
                        )
                        // Scrollable hour
                        TimeWheel(
                            value   = time.hour,
                            range   = 0..23,
                            format  = { "%02d".format(it) },
                            onChange = { h ->
                                if (index == 0) onChange(timeRange.copy(startTime = time.withHour(h)))
                                else            onChange(timeRange.copy(endTime   = time.withHour(h)))
                            }
                        )
                        Text(":", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        TimeWheel(
                            value   = time.minute,
                            range   = 0..59,
                            format  = { "%02d".format(it) },
                            onChange = { m ->
                                if (index == 0) onChange(timeRange.copy(startTime = time.withMinute(m)))
                                else            onChange(timeRange.copy(endTime   = time.withMinute(m)))
                            }
                        )
                        Text(":", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        TimeWheel(
                            value   = time.second,
                            range   = 0..59,
                            format  = { "%02d".format(it) },
                            onChange = { s ->
                                if (index == 0) onChange(timeRange.copy(startTime = time.withSecond(s)))
                                else            onChange(timeRange.copy(endTime   = time.withSecond(s)))
                            }
                        )
                        Spacer(Modifier.weight(1f))
                        // AM/PM
                        val isAm = time.hour < 12
                        Text(
                            text     = if (isAm) "AM" else "PM",
                            fontSize = 12.sp,
                            color    = cs.primary,
                            modifier = Modifier.clickable {
                                val newHour = if (isAm) time.hour + 12 else time.hour - 12
                                if (index == 0) onChange(timeRange.copy(startTime = time.withHour(newHour.coerceIn(0, 23))))
                                else            onChange(timeRange.copy(endTime   = time.withHour(newHour.coerceIn(0, 23))))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeWheel(
    value: Int,
    range: IntRange,
    format: (Int) -> String,
    onChange: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text     = format(value),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color    = cs.onSurface,
            modifier = Modifier
                .clickable { onChange(if (value < range.last) value + 1 else range.first) }
        )
    }
}

// ─────────────────────────────────────────────
//  Week / day header cell
// ─────────────────────────────────────────────

@Composable
private fun WeekCell(
    text: String,
    cs: ColorScheme,
    muted: Boolean = false
) {
    Box(Modifier.size(DaySize), contentAlignment = Alignment.Center) {
        Text(
            text      = text,
            fontSize  = 11.sp,
            color     = if (muted) cs.onSurface.copy(alpha = 0.4f) else cs.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            textAlign  = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────
//  Locale string helpers
// ─────────────────────────────────────────────

private fun persianMonthNames(locale: Locale): Array<String> =
    if (locale.language == "fa") arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    ) else arrayOf(
        "Farvardin","Ordibehesht","Khordad","Tir","Mordad","Shahrivar",
        "Mehr","Aban","Azar","Dey","Bahman","Esfand"
    )

private fun hijriMonthNames(locale: Locale): Array<String> =
    if (locale.language == "ar") arrayOf(
        "محرم","صفر","ربيع الأول","ربيع الثاني","جمادى الأولى","جمادى الآخرة",
        "رجب","شعبان","رمضان","شوال","ذو القعدة","ذو الحجة"
    ) else arrayOf(
        "Muharram","Safar","Rabi al-Awwal","Rabi al-Thani",
        "Jumada al-Awwal","Jumada al-Thani","Rajab","Sha'ban",
        "Ramadan","Shawwal","Dhu al-Qi'dah","Dhu al-Hijjah"
    )

private fun toFarsiDigits(n: Int, locale: Locale): String {
    if (locale.language != "fa") return n.toString()
    return n.toString().map { c ->
        if (c.isDigit()) ('۰' + (c - '0')) else c
    }.joinToString("")
}