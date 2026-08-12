package dev.kindling.core.components.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

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

    fun setFromLocale(locale: Locale) {
        direction = if (isRtlLocale(locale)) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    val isRtl: Boolean get() = direction == LayoutDirection.Rtl
    val isLtr: Boolean get() = direction == LayoutDirection.Ltr
}

fun isRtlLocale(locale: Locale = Locale.getDefault()): Boolean =
    locale.language.lowercase() in setOf("ar", "he", "iw", "fa", "ur", "ps", "sd", "ku", "yi", "dv")