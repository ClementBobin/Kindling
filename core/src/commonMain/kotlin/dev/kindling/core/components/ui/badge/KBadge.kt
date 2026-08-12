package dev.kindling.core.components.ui.badge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes

/**
 * Shadcn/ui-style Badge — mirrors `badge.tsx`.
 *
 * Respects [androidx.compose.ui.platform.LocalLayoutDirection] automatically via Compose RTL support.
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
    val shape  = LocalKindlingShapes.current.radius4xl

    Surface(
        modifier     = modifier,
        shape        = shape,
        color        = bg,
        contentColor = fg,
        border       = if (border != null)
            BorderStroke(1.dp, border)
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