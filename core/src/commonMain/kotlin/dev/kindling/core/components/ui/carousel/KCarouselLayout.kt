package dev.kindling.core.components.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Wraps the pager slides — place [KCarouselItem]s inside.
 *
 * ```kotlin
 * KCarouselContent {
 *     items.forEach { KCarouselItem { MySlide(it) } }
 * }
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KCarouselContent(
    api: CarouselApi,
    modifier: Modifier = Modifier,
    pageSpacing: Dp = 8.dp,
    content: @Composable (page: Int) -> Unit
) {
    HorizontalPager(
        state       = api.pagerState,
        pageSpacing = pageSpacing,
        modifier    = modifier.fillMaxWidth()
    ) { page ->
        Box(modifier = Modifier.fillMaxWidth()) { content(page) }
    }
}

/**
 * A single slide container.
 *
 * ```kotlin
 * KCarouselContent(api = api) { page ->
 *     KCarouselItem { MySlide(items[page]) }
 * }
 * ```
 */
@Composable
fun KCarouselItem(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        content  = content
    )
}

/**
 * Previous-slide button.
 * Arrow is mirrored automatically in RTL via [LocalLayoutDirection].
 */
@Composable
fun KCarouselPrevious(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: KButtonVariant = KButtonVariant.Outline,
    size: KButtonSize = KButtonSize.IconSm
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    KButton(
        onClick  = onClick,
        modifier = modifier.clip(CircleShape),
        variant  = variant,
        size     = size,
        enabled  = enabled
    ) {
        Icon(
            imageVector        = if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowRight
            else     Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Previous slide",
            modifier           = Modifier.size(16.dp)
        )
    }
}

/**
 * Next-slide button.
 * Arrow is mirrored automatically in RTL.
 */
@Composable
fun KCarouselNext(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: KButtonVariant = KButtonVariant.Outline,
    size: KButtonSize = KButtonSize.IconSm
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    KButton(
        onClick  = onClick,
        modifier = modifier.clip(CircleShape),
        variant  = variant,
        size     = size,
        enabled  = enabled
    ) {
        Icon(
            imageVector        = if (rtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft
            else     Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Next slide",
            modifier           = Modifier.size(16.dp)
        )
    }
}