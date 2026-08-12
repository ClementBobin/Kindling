package dev.kindling.core.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Horizontal stack of overlapping avatars — mirrors `AvatarGroup`.
 *
 * ```kotlin
 * KAvatarGroup(size = KAvatarSize.Default) {
 *     KAvatar { KAvatarFallback(initials = "AL") }
 *     KAvatar { KAvatarFallback(initials = "BX") }
 *     KAvatar { KAvatarFallback(initials = "CY") }
 *     KAvatarGroupCount(count = 4)
 * }
 * ```
 */
@Composable
fun KAvatarGroup(
    modifier: Modifier = Modifier,
    overlap: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(-overlap),
        verticalAlignment     = Alignment.CenterVertically,
        content               = content
    )
}

/**
 * Overflow count pill used inside [KAvatarGroup] to show "+ N more" avatars.
 *
 * ```kotlin
 * KAvatarGroupCount(count = 5, size = KAvatarSize.Default)
 * ```
 */
@Composable
fun KAvatarGroupCount(
    count: Int,
    modifier: Modifier = Modifier,
    size: KAvatarSize = KAvatarSize.Default
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(size.sizeDp)
            .clip(CircleShape)
            .background(cs.surfaceVariant)
            .border(2.dp, cs.background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text     = "+$count",
            fontSize = size.fontSizeSp.sp,
            color    = cs.onSurface
        )
    }
}