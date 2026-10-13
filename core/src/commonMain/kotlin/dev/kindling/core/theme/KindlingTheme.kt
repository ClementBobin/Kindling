package dev.kindling.core.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  KindlingShapes
//
//  Mirrors the shadcn/ui CSS variable system:
//    --radius: 0.625rem   → 10.dp  (0.625 × 16 = 10 px)
//
//  Every component reads its shape from here instead of hardcoding a Dp value,
//  so callers can restyle the entire library by passing one KindlingShapes to
//  KindlingTheme — exactly like changing --radius in index.css.
//
//  Semantic slots
//  ──────────────
//  `base`        The root radius token (= --radius).  All other slots derive
//                from it by default, but each can be overridden independently.
//
//  Per-component slots keep the API close to shadcn token names and give
//  fine-grained override power when needed.
//
//  Border width
//  ────────────
//  `borderWidth` is the matching token for stroke thickness (shadcn's `border`
//  utility = 1px).  Components never hardcode a border Dp: they read
//  `borderWidth` for resting borders, `borderWidthThin` for subtle separators
//  (card/dialog footers) and `borderWidthThick` for focus / active / error
//  emphasis.  Thin and thick are derived from `borderWidth` with `calc`, so
//  one value restyles every outline in the library.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Immutable shape token bag consumed by every Kindling component.
 *
 * All slots default to a value derived from [base] so changing just [base]
 * reskins the entire library — the same effect as editing `--radius` in CSS.
 *
 * ```kotlin
 * // Sharp / square UI
 * KindlingTheme(shapes = KindlingShapes(base = 0.dp)) { … }
 *
 * // Pill-heavy / very round UI
 * KindlingTheme(shapes = KindlingShapes(base = 20.dp)) { … }
 *
 * // Heavier borders on every component (default is 1.dp)
 * KindlingTheme(shapes = KindlingShapes(borderWidth = 2.dp)) { … }
 *
 * // Flat, borderless UI: no border or divider is drawn at all
 * KindlingTheme(shapes = KindlingShapes(base = 12.dp, borderWidth = 0.dp)) { … }
 * ```
 *
 * `borderWidth` also drives two derived widths, so a single value keeps the
 * visual hierarchy intact: [borderWidthThin] (0.5×) for subtle separators and
 * [borderWidthThick] (2×) for focus, active and error states.
 */
@Immutable
data class KindlingShapes(
    /** The global base radius.  Defaults to 10 dp (= 0.625 rem × 16). */
    val base: Dp = 10.dp,
    /**
     * The global border width used by every component outline.  Defaults to 1 dp.
     * Use `0.dp` to turn borders (and border-like dividers) off entirely.
     */
    val borderWidth: Dp = 1.dp,
) {
    init {
        require(borderWidth >= 0.dp) { "borderWidth must be >= 0.dp, was $borderWidth" }
    }

    // Derived Shape helpers used internally by components.
    // Components call these to avoid repeating RoundedCornerShape() everywhere.
    val rounded    = base
    val roundedSm  get() = calc(base, 0.6f)
    val roundedMd  get() = calc(base, 0.8f)
    val roundedLg  get() = base
    val roundedXl  get() = calc(base, 1.4f)
    val rounded2xl get() = calc(base, 1.8f)
    val rounded3xl get() = calc(base, 2.2f)
    val rounded4xl get() = calc(base, 2.6f)
    val radius:    Shape get() = RoundedCornerShape(base)
    val radiusSm:  Shape get() = RoundedCornerShape(roundedSm)
    val radiusMd:  Shape get() = RoundedCornerShape(roundedMd)
    val radiusLg:  Shape get() = RoundedCornerShape(base)
    val radiusXl:  Shape get() = RoundedCornerShape(roundedXl)
    val radius2xl: Shape get() = RoundedCornerShape(rounded2xl)
    val radius3xl: Shape get() = RoundedCornerShape(rounded3xl)
    val radius4xl: Shape get() = RoundedCornerShape(rounded4xl)

    /** Subtle separators (card / dialog footer rules): [borderWidth] × 0.5. */
    val borderWidthThin  get() = calc(borderWidth, 0.5f)

    /** Emphasis for focus, active and error states: [borderWidth] × 2. */
    val borderWidthThick get() = calc(borderWidth, 2f)

    /**
     * Builds a [BorderStroke] for [color], or `null` when [width] is zero so that
     * `Surface(border = …)` draws nothing.  (A `0.dp` stroke would otherwise be
     * rendered as a one-pixel hairline.)
     */
    fun borderStroke(color: Color, width: Dp = borderWidth): BorderStroke? =
        if (width > 0.dp) BorderStroke(width, color) else null
}

// ─────────────────────────────────────────────────────────────────────────────
//  KindlingColors
//
//  Mirrors shadcn's --chart-1 … --chart-5 CSS variables.
//  Defined here alongside KindlingShapes so both token bags live in one file
//  and are passed together through KindlingTheme — exactly like a single
//  theme object in shadcn/ui.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Five-slot chart color palette consumed by every Kindling chart renderer.
 *
 * Pass a custom instance to [KindlingTheme] to brand the entire chart library
 * without touching individual composables:
 *
 * ```kotlin
 * KindlingTheme(
 *     colors = KindlingColors(
 *         chart1 = Color(0xFFFF6B35),
 *         chart2 = Color(0xFF004E89),
 *         chart3 = Color(0xFF1A936F),
 *         chart4 = Color(0xFFC6AC8F),
 *         chart5 = Color(0xFF5C4742),
 *     )
 * ) { … }
 * ```
 */
@Immutable
data class KindlingColors(
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
    /** Returns the color at [index] mod 5, cycling through chart1…chart5. */
    fun atIndex(index: Int): Color = when (index % 5) {
        0 -> chart1
        1 -> chart2
        2 -> chart3
        3 -> chart4
        else -> chart5
    }

    companion object {
        /**
         * Derives a palette from the current [MaterialTheme.colorScheme].
         * This is the default used by [KindlingTheme] so dark/light mode is
         * handled automatically when no explicit [KindlingColors] is supplied.
         */
        @Composable
        fun fromMaterial3(): KindlingColors {
            val cs = MaterialTheme.colorScheme
            return KindlingColors(
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
//  CompositionLocals
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Provides [KindlingShapes] down the composition tree.
 *
 * Read via `MaterialTheme.kindlingShapes` inside any composable.
 */
val LocalKindlingShapes: ProvidableCompositionLocal<KindlingShapes> =
    staticCompositionLocalOf { KindlingShapes() }

/**
 * Provides [KindlingColors] down the composition tree.
 *
 * The static default is a safe fallback for use outside [KindlingTheme].
 * [KindlingTheme] replaces it with M3-derived values at composition time.
 *
 * Read via `MaterialTheme.kindlingColors` inside any composable.
 */
val LocalKindlingColors: ProvidableCompositionLocal<KindlingColors> =
    staticCompositionLocalOf {
        KindlingColors(
            chart1 = Color(0xFF2563EB),
            chart2 = Color(0xFF16A34A),
            chart3 = Color(0xFFD97706),
            chart4 = Color(0xFFDC2626),
            chart5 = Color(0xFF7C3AED),
        )
    }

// ─────────────────────────────────────────────────────────────────────────────
//  MaterialTheme convenience accessors
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Returns the active [KindlingShapes] from the composition tree.
 *
 * ```kotlin
 * val shapes = MaterialTheme.kindlingShapes
 * Surface(shape = shapes.radius) { … }
 * ```
 */
val MaterialTheme.kindlingShapes: KindlingShapes
    @Composable @ReadOnlyComposable
    get() = LocalKindlingShapes.current

/**
 * Returns the active [KindlingColors] from the composition tree.
 *
 * ```kotlin
 * val colors = MaterialTheme.kindlingColors
 * Canvas { drawCircle(color = colors.chart1, …) }
 * ```
 */
val MaterialTheme.kindlingColors: KindlingColors
    @Composable @ReadOnlyComposable
    get() = LocalKindlingColors.current

// ─────────────────────────────────────────────────────────────────────────────
//  KindlingTheme
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Root theme wrapper for any app using Kindling.
 *
 * Wraps [MaterialTheme] and provides [KindlingShapes] and [KindlingColors]
 * via composition locals so every descendant composable can read them without
 * explicit parameter threading.
 *
 * **Placement**: call once at the top of your NavHost / Activity composition,
 * just like `MaterialTheme { … }`.
 *
 * ```kotlin
 * // Minimal — library defaults (base = 10.dp, M3-derived chart colors)
 * KindlingTheme {
 *     Scaffold { … }
 * }
 *
 * // Custom corner radius + border width + branded chart palette
 * KindlingTheme(
 *     colorScheme = myDarkColorScheme,
 *     shapes      = KindlingShapes(base = 4.dp, borderWidth = 2.dp),
 *     colors      = KindlingColors(
 *         chart1 = Color(0xFFFF6B35),
 *         chart2 = Color(0xFF004E89),
 *         chart3 = Color(0xFF1A936F),
 *         chart4 = Color(0xFFC6AC8F),
 *         chart5 = Color(0xFF5C4742),
 *     ),
 * ) { … }
 * ```
 *
 * @param colorScheme  Material3 color scheme.  Defaults to [MaterialTheme.colorScheme]
 *                     so you can nest [KindlingTheme] inside an existing [MaterialTheme].
 * @param typography   Material3 typography.
 * @param shapes       Kindling shape tokens (corner radius and border width).  Defaults to
 *                     [KindlingShapes] (base = 10 dp, borderWidth = 1 dp).
 * @param colors       Five-slot chart palette.  Defaults to [KindlingColors.fromMaterial3]
 *                     so dark/light mode is handled automatically when omitted.
 * @param content      The composition subtree.
 */
@Composable
fun KindlingTheme(
    colorScheme: ColorScheme    = MaterialTheme.colorScheme,
    typography:  Typography     = MaterialTheme.typography,
    shapes:      KindlingShapes = KindlingShapes(),
    colors:      KindlingColors = KindlingColors.fromMaterial3(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalKindlingShapes provides shapes,
        LocalKindlingColors provides colors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = typography,
            content     = content,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Modifier shadow helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Draws a border around the node, or nothing when the width is zero.
 *
 * Drop-in replacement for `Modifier.border` that treats `0.dp` as "no border"
 * instead of a hairline.  Pair it with `KindlingShapes.borderWidth`.
 *
 * ### Example usage:
 * ```kotlin
 * val shapes = MaterialTheme.kindlingShapes
 * Box(Modifier.kindlingBorder(shapes.borderWidth, MaterialTheme.colorScheme.outline, shapes.radius))
 * ```
 *
 * @param width Border thickness.  `0.dp` (or less) draws nothing.
 * @param color Border colour.
 * @param shape Shape the border follows.
 */
fun Modifier.kindlingBorder(width: Dp, color: Color, shape: Shape): Modifier =
    if (width > 0.dp) this.border(width, color, shape) else this

fun Modifier.kindlingShadowXs(shape: Shape): Modifier = this.shadow(
    elevation    = 1.dp,
    shape        = shape,
    ambientColor = Color.Black.copy(alpha = 0.05f),
    spotColor    = Color.Black.copy(alpha = 0.05f),
)

fun Modifier.kindlingShadowSm(shape: Shape): Modifier = this.shadow(
    elevation    = 2.dp,
    shape        = shape,
    ambientColor = Color.Black.copy(alpha = 0.07f),
    spotColor    = Color.Black.copy(alpha = 0.07f),
)

fun Modifier.kindlingShadowMd(shape: Shape): Modifier = this.shadow(
    elevation    = 4.dp,
    shape        = shape,
    ambientColor = Color.Black.copy(alpha = 0.10f),
    spotColor    = Color.Black.copy(alpha = 0.10f),
)

fun Modifier.kindlingShadowLg(shape: Shape): Modifier = this.shadow(
    elevation    = 8.dp,
    shape        = shape,
    ambientColor = Color.Black.copy(alpha = 0.12f),
    spotColor    = Color.Black.copy(alpha = 0.12f),
)

fun Modifier.kindlingShadowNone(shape: Shape = RoundedCornerShape(0.dp)): Modifier = this.shadow(
    elevation    = 0.dp,
    shape        = shape,
    ambientColor = Color.Transparent,
    spotColor    = Color.Transparent,
)

fun Modifier.kindlingClipNone(shape: Shape = RoundedCornerShape(0.dp)): Modifier = this.clip(shape)

private fun calc(base: Dp, factor: Float): Dp = (base.value * factor).dp