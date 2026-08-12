package dev.kindling.core.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

@Composable
internal fun CalendarGrid(
    selected: LocalDate?,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    locale: Locale,
    onSelect: (LocalDate) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var currentMonth by remember {
        mutableStateOf(YearMonth.from(selected ?: LocalDate.now()))
    }
    val today      = LocalDate.now()
    val monthLabel = currentMonth.month
        .getDisplayName(JTextStyle.FULL, locale)
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
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
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
                fontSize   = 14.sp
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
            DayOfWeek.values().forEach { d ->
                Text(
                    text       = d.getDisplayName(JTextStyle.NARROW, locale),
                    modifier   = Modifier.weight(1f),
                    textAlign  = TextAlign.Center,
                    fontSize   = 12.sp,
                    color      = cs.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Day cells
        val firstDay    = currentMonth.atDay(1)
        val startOffset = firstDay.dayOfWeek.value - 1
        val daysInMonth = currentMonth.lengthOfMonth()
        val rowCount    = (startOffset + daysInMonth + 6) / 7

        for (row in 0 until rowCount) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date       = currentMonth.atDay(dayNum)
                        val isSelected = date == selected
                        val isToday    = date == today
                        val disabled   = (minDate != null && date < minDate) ||
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
                                    if (!disabled) Modifier.clickable { onSelect(date) }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = dayNum.toString(),
                                fontSize   = 13.sp,
                                color      = when {
                                    isSelected -> cs.onPrimary
                                    disabled   -> cs.onSurface.copy(alpha = 0.3f)
                                    isToday    -> cs.primary
                                    else       -> cs.onSurface
                                },
                                fontWeight = if (isSelected || isToday) FontWeight.SemiBold
                                else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}