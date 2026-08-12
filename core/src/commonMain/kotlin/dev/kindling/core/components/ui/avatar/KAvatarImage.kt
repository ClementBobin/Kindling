package dev.kindling.core.components.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

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
    Image(
        painter            = painter,
        contentDescription = contentDescription,
        contentScale       = ContentScale.Crop,
        modifier           = modifier.matchParentSize().clip(CircleShape)
    )
}