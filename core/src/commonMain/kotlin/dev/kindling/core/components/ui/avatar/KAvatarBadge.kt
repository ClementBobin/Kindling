package dev.kindling.core.components.ui

import androidx.compose.foundation.background
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

/**
 * Status badge shown in the bottom-right corner of an [KAvatar].
 *
 * ```kotlin
 * KAvatarBadge(size = KAvatarSize.Default) // solid primary circle
 * KAvatarBadge(size = KAvatarSize.Lg) {
 *     Icon(Icons.Default.Check, null, modifier = Modifier.size(6.dp))
 * }
 * ```
 */
@Composable
fun BoxScope.KAvatarBadge(
    modifier: Modifier = Modifier,
    size: KAvatarSize = KAvatarSize.Default,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .align(Alignment.BottomEnd)
            .size(size.badgeSizeDp)
            .clip(CircleShape)
            .background(cs.primary)
            .border(2.dp, cs.background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        content?.invoke(this)
    }
}