package dev.kindling.core.components.charts

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  ChartDefaults
//
//  Stroke / fill constants shared across all chart renderers.
//  Color tokens live in KindlingColors (dev.kindling.core.theme) and are read
//  via MaterialTheme.kindlingColors inside each composable.
// ─────────────────────────────────────────────────────────────────────────────

object ChartDefaults {
    val strokeWidth: Dp = 2.dp
    val dotRadius:   Dp = 3.dp
    val barCorner:   Dp = 4.dp
    /** Alpha applied to area fills. */
    const val areaFillAlpha:  Float = 0.4f
    /** Alpha applied to radar fills. */
    const val radarFillAlpha: Float = 0.6f
    /** Tick label abbreviation length (e.g. "January" → "Jan"). */
    const val tickLabelLength: Int = 3
}

// ─────────────────────────────────────────────────────────────────────────────
//  Extension helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Returns the abbreviated tick label (first [ChartDefaults.tickLabelLength] chars). */
fun String.toTickLabel(): String =
    if (length > ChartDefaults.tickLabelLength) take(ChartDefaults.tickLabelLength) else this

/**
 * Computes a safe max value across all series, returning 1f when all values
 * are 0 (prevents division-by-zero in normalisation).
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