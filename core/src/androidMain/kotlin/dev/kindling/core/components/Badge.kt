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
    val cs = MaterialTheme.colorScheme

    data class BadgeColors(val bg: Color, val fg: Color, val border: Color?)

    val colors = when (variant) {
        KBadgeVariant.Default     -> BadgeColors(cs.primary, cs.onPrimary, null)
        KBadgeVariant.Secondary   -> BadgeColors(cs.secondaryContainer, cs.onSecondaryContainer, null)
        KBadgeVariant.Destructive -> BadgeColors(cs.error.copy(.1f), cs.error, null)
        KBadgeVariant.Outline     -> BadgeColors(Color.Transparent, cs.onSurface, cs.outline)
        KBadgeVariant.Ghost       -> BadgeColors(Color.Transparent, cs.onSurface, null)
        KBadgeVariant.Link        -> BadgeColors(Color.Transparent, cs.primary, null)
    }

    Surface(
        modifier     = modifier,
        shape        = RoundedCornerShape(percent = 50),
        color        = colors.bg,
        contentColor = colors.fg,
        border       = if (colors.border != null)
            androidx.compose.foundation.BorderStroke(1.dp, colors.border)
        else null
    ) {
        Row(
            modifier          = Modifier
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