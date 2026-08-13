package dev.kindling.core.components.ui.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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