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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Size preset
// ─────────────────────────────────────────────

/**
 * Avatar size presets matching the shadcn/ui `size` data attribute:
 * `sm` = 24 dp, `default` = 32 dp, `lg` = 40 dp.
 */
enum class KAvatarSize(val dp: Dp, val fontSize: Float, val badgeSize: Dp) {
    Sm(24.dp, 10f, 8.dp),
    Default(32.dp, 12f, 10.dp),
    Lg(40.dp, 14f, 12.dp)
}

// ─────────────────────────────────────────────
//  KAvatar
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Avatar.
 *
 * Shows [painter] when provided; falls back to [fallbackText] initials,
 * then to a coloured circle when both are absent.
 *
 * An optional [badge] slot (bottom-right) mirrors `AvatarBadge`.
 *
 * ```kotlin
 * // Image avatar
 * KAvatar(painter = rememberAsyncImagePainter(url), contentDescription = "Alice")
 *
 * // Fallback initials
 * KAvatar(fallbackText = "CN")
 *
 * // With badge
 * KAvatar(fallbackText = "AB", size = KAvatarSize.Lg) {
 *     Box(Modifier.fillMaxSize().background(Color.Green))
 * }
 * ```
 *
 * @param painter           Optional image painter.
 * @param contentDescription Accessibility description for the image.
 * @param fallbackText       1–2 character initials shown when no image is available.
 * @param size               Size preset.
 * @param modifier           Applied to the outermost container.
 * @param badge              Optional composable rendered in the bottom-right corner.
 */
@Composable
fun KAvatar(
    painter: Painter? = null,
    contentDescription: String? = null,
    fallbackText: String? = null,
    size: KAvatarSize = KAvatarSize.Default,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(1.dp, cs.outline.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            painter != null -> {
                androidx.compose.foundation.Image(
                    painter            = painter,
                    contentDescription = contentDescription,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            }
            fallbackText != null -> {
                Box(
                    modifier           = Modifier.fillMaxSize().background(cs.muted),
                    contentAlignment   = Alignment.Center
                ) {
                    Text(
                        text       = fallbackText.take(2).uppercase(),
                        fontSize   = size.fontSize.sp,
                        fontWeight = FontWeight.Medium,
                        color      = cs.onSurface
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(cs.muted)
                )
            }
        }

        // Badge (bottom-right)
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size.badgeSize)
                    .clip(CircleShape)
                    .background(cs.primary)
                    .border(2.dp, cs.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                badge()
            }
        }
    }
}

// ─────────────────────────────────────────────
//  KAvatarGroup
// ─────────────────────────────────────────────

/**
 * Renders a list of [KAvatar] composables in a horizontal stack,
 * each overlapping by [overlap].
 *
 * Mirrors `AvatarGroup` + `AvatarGroupCount` from shadcn/ui.
 *
 * ```kotlin
 * KAvatarGroup(
 *     avatars = listOf(
 *         KAvatarData(fallbackText = "CN"),
 *         KAvatarData(fallbackText = "AB"),
 *         KAvatarData(fallbackText = "MX"),
 *     ),
 *     maxVisible = 3,
 *     size = KAvatarSize.Default
 * )
 * ```
 */
@Composable
fun KAvatarGroup(
    avatars: List<KAvatarData>,
    modifier: Modifier = Modifier,
    maxVisible: Int = 4,
    size: KAvatarSize = KAvatarSize.Default,
    overlap: Dp = 8.dp
) {
    val cs = MaterialTheme.colorScheme
    val visible  = avatars.take(maxVisible)
    val overflow = avatars.size - visible.size

    Row(modifier = modifier) {
        visible.forEachIndexed { index, data ->
            Box(
                modifier = Modifier
                    .offset(x = if (index == 0) 0.dp else -(overlap * index))
                    .border(2.dp, cs.background, CircleShape)
                    .clip(CircleShape)
            ) {
                KAvatar(
                    painter            = data.painter,
                    contentDescription = data.contentDescription,
                    fallbackText       = data.fallbackText,
                    size               = size
                )
            }
        }

        if (overflow > 0) {
            Box(
                modifier = Modifier
                    .offset(x = -(overlap * visible.size))
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(cs.muted)
                    .border(2.dp, cs.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = "+$overflow",
                    fontSize = size.fontSize.sp,
                    color    = cs.onSurface
                )
            }
        }
    }
}

/**
 * Data holder for a single avatar in [KAvatarGroup].
 */
data class KAvatarData(
    val painter: Painter? = null,
    val contentDescription: String? = null,
    val fallbackText: String? = null
)

// ─────────────────────────────────────────────
//  Convenience — Color.muted extension shim
// ─────────────────────────────────────────────
// MaterialTheme.colorScheme does not expose "muted" directly;
// we use surfaceVariant which is the closest semantic equivalent.
private val androidx.compose.material3.ColorScheme.muted: Color
    get() = surfaceVariant