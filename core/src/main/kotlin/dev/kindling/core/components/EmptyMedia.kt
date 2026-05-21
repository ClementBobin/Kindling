package dev.kindling.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render media content used in empty states.
 *
 * @param modifier Applied to the outermost layout element.
 * @param variant Visual variant for the media container.
 * @param size Size of the media container.
 * @param iconBoxColor Background colour for the icon variant.
 * @param content Content shown inside the media container.
 */
@Composable
fun KEmptyMedia(
    modifier: Modifier = Modifier,
    variant: KEmptyMediaVariant = KEmptyMediaVariant.Icon,
    size: Dp = 56.dp,
    iconBoxColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable BoxScope.() -> Unit
) {
    val boxMod = when (variant) {
        KEmptyMediaVariant.Icon -> modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(iconBoxColor)
            .padding(12.dp)
        KEmptyMediaVariant.Avatar -> modifier.size(size).clip(CircleShape)
        KEmptyMediaVariant.Image -> modifier.size(size)
    }
    Box(modifier = boxMod, contentAlignment = Alignment.Center, content = content)
}

@Preview(name = "KEmptyMedia — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KEmptyMedia — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKEmptyMedia() {
    KindlingPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            KEmptyMedia(variant = KEmptyMediaVariant.Icon) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            KEmptyMedia(variant = KEmptyMediaVariant.Avatar) {
                Text("AB", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
