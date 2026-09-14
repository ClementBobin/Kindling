package dev.kindling.core.components.ui.animated.logosCarousel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest

@Composable
fun KDefaultLogoItem(
    logo: KLogo,
    logoHeight: Dp,
    colorFilter: ColorFilter?
) {
    val context = LocalPlatformContext.current
    val model = logo.url ?: logo.resId

    Box(
        modifier = Modifier.height(logoHeight),
        contentAlignment = Alignment.Center
    ) {
        if (model != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(model)
                    .build(),
                contentDescription = logo.alt.ifEmpty { null },
                colorFilter = colorFilter,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}