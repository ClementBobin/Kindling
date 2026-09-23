package dev.kindling.core.components.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import kotlinx.datetime.*

data class YearMonth(val year: Int, val month: Month) {
    fun plusMonths(months: Int): YearMonth {
        val totalMonths = (year * 12) + (month.ordinal) + months
        val newYear = totalMonths / 12
        val newMonthIndex = totalMonths % 12
        val normalizedMonthIndex = if (newMonthIndex < 0) newMonthIndex + 12 else newMonthIndex
        val normalizedYear = if (newMonthIndex < 0) newYear - 1 else newYear
        return YearMonth(normalizedYear, Month.entries[normalizedMonthIndex])
    }
    fun minusMonths(months: Int): YearMonth = plusMonths(-months)
    val lengthOfMonth: Int get() = lengthOfMonth(year, month)
    fun atDay(day: Int): LocalDate = LocalDate(year, month, day)
}

private fun lengthOfMonth(year: Int, month: Month): Int {
    return when (month) {
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)
}

@Composable
internal fun KCalendarGrid(
    selected: LocalDate?,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    locale: String = "en",
    onSelect: (LocalDate) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var currentMonth by remember {
        mutableStateOf(
            selected?.let { YearMonth(it.year, it.month) }
                ?: YearMonth(today.year, today.month)
        )
    }
    val monthLabel = currentMonth.month.name.lowercase()
        .replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .padding(top = 4.dp)
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(cs.surface)
            .border(1.dp, cs.outline, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                "$monthLabel ${currentMonth.year}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Weekday headers
        Row(Modifier.fillMaxWidth()) {
            val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
            weekdays.forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = cs.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Day cells
        val firstDay = currentMonth.atDay(1)
        val startOffset = firstDay.dayOfWeek.isoDayNumber - 1
        val daysInMonth = currentMonth.lengthOfMonth
        val rowCount = (startOffset + daysInMonth + 6) / 7

        for (row in 0 until rowCount) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = currentMonth.atDay(dayNum)
                        val isSelected = date == selected
                        val isToday = date == today
                        val disabled = (minDate != null && date < minDate) ||
                                (maxDate != null && date > maxDate)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) cs.primary else Color.Transparent
                                )
                                .border(
                                    width = if (isToday && !isSelected) 1.dp else 0.dp,
                                    color = if (isToday && !isSelected) cs.primary
                                    else Color.Transparent,
                                    shape = CircleShape
                                )
                                .then(
                                    if (!disabled) {
                                        Modifier.clickable { onSelect(date) }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                fontSize = 13.sp,
                                color = when {
                                    isSelected -> cs.onPrimary
                                    disabled -> cs.onSurface.copy(alpha = 0.3f)
                                    else -> cs.onSurface
                                },
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
