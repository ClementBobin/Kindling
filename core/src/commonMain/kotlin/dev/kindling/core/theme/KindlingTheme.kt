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
 * KindlingTheme(shapes = KindlingShapes(base = 10.dp, card = 16.dp)) { … }
 * ```
 */
@Immutable
data class KindlingShapes(

    // ── Root token ────────────────────────────────────────────────────────
    /** The global base radius.  Defaults to 10 dp (= 0.625 rem × 16). */
    val base: Dp = 10.dp,
) {
    // Derived Shape helpers used internally by components.
    // Components call these to avoid repeating RoundedCornerShape() everywhere.
    val rounded = base
    val roundedSm get() = calc(base, 0.6f)
    val roundedMd get() = calc(base, 0.8f)
    val roundedLg get() = base
    val roundedXl get() = calc(base, 1.4f)
    val rounded2xl get() = calc(base, 1.8f)
    val rounded3xl get() = calc(base, 2.2f)
    val rounded4xl get() = calc(base, 2.6f)
    val radius:  Shape get() = RoundedCornerShape(base)
    val radiusSm:  Shape get() = RoundedCornerShape(roundedSm)
    val radiusMd:  Shape get() = RoundedCornerShape(roundedMd)
    val radiusLg:  Shape get() = RoundedCornerShape(base)
    val radiusXl:  Shape get() = RoundedCornerShape(roundedXl)
    val radius2xl: Shape get() = RoundedCornerShape(rounded2xl)
    val radius3xl: Shape get() = RoundedCornerShape(rounded3xl)
    val radius4xl: Shape get() = RoundedCornerShape(rounded4xl)
}

// ─────────────────────────────────────────────────────────────────────────────
//  CompositionLocal
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Provides [KindlingShapes] down the composition tree.
 *
 * Read via `LocalKindlingShapes.current` inside any composable, or use the
 * convenience extension [kindlingShapes].
 */
val LocalKindlingShapes: ProvidableCompositionLocal<KindlingShapes> =
    staticCompositionLocalOf { KindlingShapes() }

/**
 * Convenience accessor — mirrors `MaterialTheme.colorScheme`, `MaterialTheme.typography`.
 *
 * ```kotlin
 * val shapes = MaterialTheme.kindlingShapes
 * Surface(shape = shapes.buttonShape) { … }
 * ```
 */
val MaterialTheme.kindlingShapes: KindlingShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalKindlingShapes.current

// ─────────────────────────────────────────────────────────────────────────────
//  KindlingTheme
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Root theme wrapper for any app using Kindling.
 *
 * Wraps [MaterialTheme] and additionally provides [KindlingShapes] (and, in
 * the future, any other Kindling-specific tokens) via composition locals.
 *
 * **Placement**: call once at the top of your NavHost / Activity composition,
 * just like you would call `MaterialTheme { … }`.
 *
 * ```kotlin
 * // Minimal — uses library defaults (base = 10.dp)
 * KindlingTheme {
 *     Scaffold { … }
 * }
 *
 * // With a custom color scheme and sharp corners
 * KindlingTheme(
 *     colorScheme = myDarkColorScheme,
 *     shapes      = KindlingShapes(base = 4.dp)
 * ) {
 *     Scaffold { … }
 * }
 *
 * // Override only the card radius
 * KindlingTheme(shapes = KindlingShapes(card = 24.dp)) {
 *     Scaffold { … }
 * }
 * ```
 *
 * @param colorScheme  Material3 color scheme.  Defaults to [MaterialTheme.colorScheme]
 *                     so you can nest KindlingTheme inside an existing MaterialTheme.
 * @param typography   Material3 typography.
 * @param shapes       Kindling shape tokens.  Defaults to [KindlingShapes] (base = 10 dp).
 * @param content      The composition subtree.
 */
@Composable
fun KindlingTheme(
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    typography: Typography   = MaterialTheme.typography,
    shapes: KindlingShapes   = KindlingShapes(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalKindlingShapes provides shapes) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = typography,
            content     = content
        )
    }
}

fun Modifier.kindlingShadowXs(shape: Shape): Modifier = this.shadow(
    elevation    = 1.dp,
    shape        = shape,
    ambientColor = Color.Black.copy(alpha = 0.05f),
    spotColor    = Color.Black.copy(alpha = 0.05f)
)

fun Modifier.kindlingShadowSm(shape: Shape): Modifier = this.shadow(
    elevation    = 2.dp,
    shape        = shape,
    ambientColor = Color.Black.copy(alpha = 0.07f),
    spotColor    = Color.Black.copy(alpha = 0.07f)
)

fun Modifier.kindlingShadowMd(shape: Shape): Modifier = this.shadow(
    elevation    = 4.dp,
    shape        = shape,
    ambientColor = Color.Black.copy(alpha = 0.10f),
    spotColor    = Color.Black.copy(alpha = 0.10f)
)

fun Modifier.kindlingShadowLg(shape: Shape): Modifier = this.shadow(
    elevation    = 8.dp,
    shape        = shape,
    ambientColor = Color.Black.copy(alpha = 0.12f),
    spotColor    = Color.Black.copy(alpha = 0.12f)
)

fun Modifier.kindlingShadowNone(shape: Shape = RoundedCornerShape(0.dp)): Modifier = this.shadow(
    elevation    = 0.dp,
    shape        = shape,
    ambientColor = Color.Transparent,
    spotColor    = Color.Transparent
)

fun Modifier.kindlingClipNone(shape: Shape = RoundedCornerShape(0.dp)): Modifier = this.clip(shape)

private fun calc(base: Dp, factor: Float): Dp = (base.value * factor).dp