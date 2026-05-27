package dev.kindling.core.components

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

// ─────────────────────────────────────────────
//  Direction provider
// ─────────────────────────────────────────────

/**
 * Overrides [LocalLayoutDirection] for the entire subtree.
 *
 * ```kotlin
 * KDirectionProvider(LayoutDirection.Rtl) { MyScreen() }
 * ```
 */
@Composable
fun KDirectionProvider(
    direction: LayoutDirection = LayoutDirection.Ltr,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
}

/** Reads the current [LayoutDirection] from composition. */
@Composable
fun useKDirection(): LayoutDirection = LocalLayoutDirection.current

// ─────────────────────────────────────────────
//  Runtime singleton for app-level toggling
// ─────────────────────────────────────────────

/**
 * Singleton for runtime LTR ↔ RTL toggling.
 *
 * ```kotlin
 * // In your root composable:
 * KDirectionProvider(direction = KDirectionManager.direction) { NavHost(…) }
 *
 * // Anywhere:
 * KButton("Toggle RTL", onClick = { KDirectionManager.toggle() })
 * ```
 */
object KDirectionManager {
    var direction by mutableStateOf<LayoutDirection>(LayoutDirection.Ltr)
        private set

    fun toggle() {
        direction = if (direction == LayoutDirection.Ltr) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    fun set(dir: LayoutDirection) { direction = dir }

    fun setFromLocale(locale: java.util.Locale) {
        direction = if (isRtlLocale(locale)) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    val isRtl: Boolean get() = direction == LayoutDirection.Rtl
    val isLtr: Boolean get() = direction == LayoutDirection.Ltr
}

// ─────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────

fun isRtlLocale(locale: java.util.Locale = java.util.Locale.getDefault()): Boolean =
    locale.language.lowercase() in setOf("ar", "he", "iw", "fa", "ur", "ps", "sd", "ku", "yi", "dv")

/**
 * Returns the current [LayoutDirection] as a boolean.
 * Useful for conditional layout logic inside composables.
 */
@Composable
fun isRtl(): Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl