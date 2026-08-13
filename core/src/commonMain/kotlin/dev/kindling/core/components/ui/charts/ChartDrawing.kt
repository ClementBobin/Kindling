package dev.kindling.core.components.ui.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  KindlingChartColors
//
//  Mirrors shadcn's --chart-1 … --chart-5 CSS variables.
//  Consumed by every chart renderer so the 5-color palette is consistent.
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class KindlingChartColors(
    /** Equivalent of --chart-1 */
    val chart1: Color,
    /** Equivalent of --chart-2 */
    val chart2: Color,
    /** Equivalent of --chart-3 */
    val chart3: Color,
    /** Equivalent of --chart-4 */
    val chart4: Color,
    /** Equivalent of --chart-5 */
    val chart5: Color,
) {
    fun atIndex(index: Int): Color = when (index % 5) {
        0 -> chart1
        1 -> chart2
        2 -> chart3
        3 -> chart4
        else -> chart5
    }

    companion object {
        /**
         * Default palette derived from Material3 colour roles.
         * Swap with a branded set by passing custom [KindlingChartColors] to
         * [rememberChartColors].
         */
        @Composable
        fun fromMaterial3(): KindlingChartColors {
            val cs = MaterialTheme.colorScheme
            return KindlingChartColors(
                chart1 = cs.primary,
                chart2 = cs.secondary,
                chart3 = cs.tertiary,
                chart4 = cs.primaryContainer,
                chart5 = cs.secondaryContainer,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Stroke / fill constants shared across renderers
// ─────────────────────────────────────────────────────────────────────────────

object ChartDefaults {
    val strokeWidth: Dp = 2.dp
    val dotRadius: Dp   = 3.dp
    val barCorner: Dp   = 4.dp
    /** Alpha applied to area fills. */
    const val areaFillAlpha: Float = 0.4f
    /** Alpha applied to radar fills. */
    const val radarFillAlpha: Float = 0.6f
    /** Tick label abbreviation length (e.g. "January" → "Jan"). */
    const val tickLabelLength: Int = 3
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helper extensions
// ─────────────────────────────────────────────────────────────────────────────

/** Returns the abbreviated tick label (first [ChartDefaults.tickLabelLength] chars). */
fun String.toTickLabel(): String =
    if (length > ChartDefaults.tickLabelLength) take(ChartDefaults.tickLabelLength) else this

/**
 * Computes a safe max value across all series, returning 1f when all values are 0
 * (prevents division-by-zero in normalisation).
 */
fun List<ChartSeriesData>.safeMaxValue(): Float =
    flatMap { it.values }.maxOrNull()?.takeIf { it != 0f } ?: 1f

/**
 * Computes the stacked max: for each data-point index, sum the values across
 * series, then return the maximum sum.
 */
fun List<ChartSeriesData>.safeStackedMaxValue(): Float {
    if (isEmpty()) return 1f
    val size = first().values.size
    return (0 until size)
        .map { i -> sumOf { it.values.getOrElse(i) { 0f }.toDouble() }.toFloat() }
        .maxOrNull()?.takeIf { it != 0f } ?: 1f
}