package dev.kindling.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  KBadge
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Badge — mirrors `badge.tsx`.
 *
 * Respects [LocalLayoutDirection] automatically via Compose RTL support.
 *
 * ```kotlin
 * KBadge { Text("New") }
 * KBadge(variant = KBadgeVariant.Destructive) { Text("Error") }
 * KBadge(variant = KBadgeVariant.Outline) { Icon(…); Text("Label") }
 * ```
 */
@Composable
fun KBadge(
    modifier: Modifier = Modifier,
    variant: KBadgeVariant = KBadgeVariant.Default,
    content: @Composable RowScope.() -> Unit
) {
    val bg     = variant.bg()
    val fg     = variant.fg()
    val border = variant.border()

    Surface(
        modifier     = modifier,
        shape        = RoundedCornerShape(percent = 50),
        color        = bg,
        contentColor = fg,
        border       = if (border != null)
            androidx.compose.foundation.BorderStroke(1.dp, border)
        else null
    ) {
        Row(
            modifier = Modifier
                .height(20.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ProvideTextStyle(
                MaterialTheme.typography.labelSmall.copy(
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            ) { content() }
        }
    }
}

// Replace the existing KBadgeVariant enum/sealed class with this
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