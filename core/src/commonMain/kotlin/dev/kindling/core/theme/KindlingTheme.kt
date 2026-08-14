package dev.kindling.core.theme

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
 * // Override one slot while keeping everything else default
 * KindlingTheme(shapes = KindlingShapes(base = 10.dp)) { … }
 * ```
 */
@Immutable
data class KindlingShapes(
    /** The global base radius.  Defaults to 10 dp (= 0.625 rem × 16). */
    val base: Dp = 10.dp,
) {
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
 * // Custom corner radius + branded chart palette
 * KindlingTheme(
 *     colorScheme = myDarkColorScheme,
 *     shapes      = KindlingShapes(base = 4.dp),
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
 * @param shapes       Kindling shape tokens.  Defaults to [KindlingShapes] (base = 10 dp).
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