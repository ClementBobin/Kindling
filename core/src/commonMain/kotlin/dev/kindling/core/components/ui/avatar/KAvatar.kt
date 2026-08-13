package dev.kindling.core.components.ui.avatar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────
//  KAvatar (root container)
// ─────────────────────────────────────────────

/**
 * Root avatar container — mirrors shadcn/ui `Avatar`.
 *
 * Compose the child slots [KAvatarImage], [KAvatarFallback], [KAvatarBadge]
 * inside a [Box] to reproduce the layered structure of the web component.
 *
 * ```kotlin
 * KAvatar(size = KAvatarSize.Lg) {
 *     KAvatarImage(painter = rememberAsyncImagePainter(url), contentDescription = "Alice")
 *     KAvatarFallback { Text("AL") }
 *     KAvatarBadge()
 * }
 * ```
 */
@Composable
fun KAvatar(
    modifier: Modifier = Modifier,
    size: KAvatarSize = KAvatarSize.Default,
    content: @Composable BoxScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(size.sizeDp)
            .clip(CircleShape)
            .border(1.dp, cs.outline.copy(alpha = .25f), CircleShape),
        contentAlignment = Alignment.Center,
        content = content
    )
}