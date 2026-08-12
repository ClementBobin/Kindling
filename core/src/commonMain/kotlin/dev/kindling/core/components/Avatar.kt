package dev.kindling.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Size resolution
// ─────────────────────────────────────────────

private val KAvatarSize.sizeDp: Dp get() = when (this) {
    KAvatarSize.Sm      -> 24.dp
    KAvatarSize.Default -> 32.dp
    KAvatarSize.Lg      -> 40.dp
}

private val KAvatarSize.fontSizeSp: Float get() = when (this) {
    KAvatarSize.Sm      -> 10f
    KAvatarSize.Default -> 12f
    KAvatarSize.Lg      -> 14f
}

private val KAvatarSize.badgeSizeDp: Dp get() = when (this) {
    KAvatarSize.Sm      -> 8.dp
    KAvatarSize.Default -> 10.dp
    KAvatarSize.Lg      -> 12.dp
}

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

// ─────────────────────────────────────────────
//  KAvatarImage
// ─────────────────────────────────────────────

/**
 * Image layer of an [KAvatar].
 *
 * Pass `null` to [painter] to hide and let [KAvatarFallback] show instead.
 */
@Composable
fun BoxScope.KAvatarImage(
    modifier: Modifier = Modifier,
    painter: Painter?,
    contentDescription: String? = null
) {
    if (painter == null) return
    androidx.compose.foundation.Image(
        painter            = painter,
        contentDescription = contentDescription,
        contentScale       = ContentScale.Crop,
        modifier           = modifier.matchParentSize().clip(CircleShape)
    )
}

// ─────────────────────────────────────────────
//  KAvatarFallback
// ─────────────────────────────────────────────

/**
 * Fallback layer — shown when [KAvatarImage] has no painter.
 *
 * ```kotlin
 * KAvatarFallback(size = KAvatarSize.Default) { Text("CN") }
 * // or convenience:
 * KAvatarFallback(initials = "CN", size = KAvatarSize.Default)
 * ```
 */
@Composable
fun BoxScope.KAvatarFallback(
    modifier: Modifier = Modifier,
    size: KAvatarSize = KAvatarSize.Default,
    initials: String? = null,
    content: (@Composable () -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier         = modifier
            .matchParentSize()
            .background(cs.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            content != null  -> content()
            initials != null -> Text(
                text       = initials.take(2).uppercase(),
                fontSize   = size.fontSizeSp.sp,
                fontWeight = FontWeight.Medium,
                color      = cs.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────
//  KAvatarBadge
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  KAvatarGroup
// ─────────────────────────────────────────────

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
    //size: KAvatarSize = KAvatarSize.Default,
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

// ─────────────────────────────────────────────
//  KAvatarGroupCount
// ─────────────────────────────────────────────

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