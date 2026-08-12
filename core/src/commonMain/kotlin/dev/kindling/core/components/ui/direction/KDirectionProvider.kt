package dev.kindling.core.components.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

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

/**
 * Returns the current [LayoutDirection] as a boolean.
 * Useful for conditional layout logic inside composables.
 */
@Composable
fun isRtl(): Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl