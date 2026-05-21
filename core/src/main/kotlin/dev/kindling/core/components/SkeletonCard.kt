package dev.kindling.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render a skeleton card with an image placeholder and text lines.
 *
 * @param modifier Applied to the outermost layout element.
 * @param imageHeight Height of the image placeholder.
 */
@Composable
fun KSkeletonCard(
    modifier: Modifier = Modifier,
    imageHeight: Dp = 180.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KSkeleton(modifier = Modifier.fillMaxWidth().height(imageHeight), shape = RoundedCornerShape(8.dp))
        KSkeleton(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
        KSkeleton(modifier = Modifier.fillMaxWidth().height(12.dp))
        KSkeleton(modifier = Modifier.fillMaxWidth(0.85f).height(12.dp))
    }
}

@Preview(name = "KSkeletonCard — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KSkeletonCard — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKSkeletonCard() {
    KindlingPreviewSurface {
        KSkeletonCard()
    }
}
