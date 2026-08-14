package dev.kindling.core.components.ui.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import dev.kindling.core.theme.KindlingColors
import dev.kindling.core.theme.kindlingColors

// ─────────────────────────────────────────────────────────────────────────────
//  Shared Chart Helpers & Extensions
// ─────────────────────────────────────────────────────────────────────────────

fun List<ChartSeries>.safeMaxValue(): Float {
    val max = flatMap { it.values }.maxOrNull() ?: 1f
    return if (max == 0f) 1f else max
}

fun List<ChartSeries>.safeStackedMaxValue(): Float {
    if (isEmpty()) return 1f
    val dataCount = first().values.size
    var maxStack = 0f
    for (i in 0 until dataCount) {
        val sum = sumOf { it.values.getOrElse(i) { 0f }.toDouble() }.toFloat()
        if (sum > maxStack) maxStack = sum
    }
    return if (maxStack == 0f) 1f else maxStack
}