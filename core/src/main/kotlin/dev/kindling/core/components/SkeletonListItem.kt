package dev.kindling.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render a skeleton list item with an avatar and text lines.
 *
 * @param modifier Applied to the outermost layout element.
 * @param avatarSize Size of the circular avatar placeholder.
 */
@Composable
fun KSkeletonListItem(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 40.dp
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        KSkeleton(modifier = Modifier.size(avatarSize), shape = CircleShape)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KSkeleton(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp))
            KSkeleton(modifier = Modifier.fillMaxWidth(0.9f).height(12.dp))
        }
    }
}

@Preview(name = "KSkeletonListItem — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KSkeletonListItem — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKSkeletonListItem() {
    KindlingPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KSkeletonListItem()
            KSkeletonListItem()
        }
    }
}
