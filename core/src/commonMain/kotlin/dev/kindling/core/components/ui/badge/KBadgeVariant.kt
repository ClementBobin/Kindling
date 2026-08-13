package dev.kindling.core.components.ui.badge

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Defines styling configurations and themes for [dev.kindling.core.components.ui.badge.KBadge].
 */
data class KBadgeVariant(
    val bg: @Composable () -> Color,
    val fg: @Composable () -> Color,
    val border: @Composable () -> Color? = { null }
) {
    companion object {
        val Default: KBadgeVariant
            @Composable get() = KBadgeVariant(
                bg = { MaterialTheme.colorScheme.primary },
                fg = { MaterialTheme.colorScheme.onPrimary }
            )
        val Secondary: KBadgeVariant
            @Composable get() = KBadgeVariant(
                bg = { MaterialTheme.colorScheme.secondaryContainer },
                fg = { MaterialTheme.colorScheme.onSecondaryContainer }
            )
        val Destructive: KBadgeVariant
            @Composable get() = KBadgeVariant(
                bg = { MaterialTheme.colorScheme.error.copy(.1f) },
                fg = { MaterialTheme.colorScheme.error }
            )
        val Outline: KBadgeVariant
            @Composable get() = KBadgeVariant(
                bg = { Color.Transparent },
                fg = { MaterialTheme.colorScheme.onSurface },
                border = { MaterialTheme.colorScheme.outline }
            )
        val Ghost: KBadgeVariant
            @Composable get() = KBadgeVariant(
                bg = { Color.Transparent },
                fg = { MaterialTheme.colorScheme.onSurface }
            )
        val Link: KBadgeVariant
            @Composable get() = KBadgeVariant(
                bg = { Color.Transparent },
                fg = { MaterialTheme.colorScheme.primary }
            )
    }
}