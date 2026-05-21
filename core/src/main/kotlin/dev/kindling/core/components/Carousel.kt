package dev.kindling.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shadcn/ui-style Carousel backed by Compose Foundation's [HorizontalPager] / [VerticalPager].
 *
 * ```kotlin
 * KCarousel(pageCount = items.size) { page ->
 *     Card(modifier = Modifier.fillMaxWidth().height(200.dp)) { Text(items[page]) }
 * }
 * // Auto-play
 * KCarousel(pageCount = items.size, autoPlayMs = 3_000L) { page -> Image(…) }
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KCarousel(
    pageCount: Int,
    modifier: Modifier = Modifier,
    orientation: KCarouselOrientation = KCarouselOrientation.Horizontal,
    showArrows: Boolean = true,
    showDots: Boolean = true,
    autoPlayMs: Long? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 0.dp),
    pageSpacing: Dp = 8.dp,
    state: PagerState = rememberPagerState { pageCount },
    content: @Composable BoxScope.(page: Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    if (autoPlayMs != null) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(autoPlayMs)
                state.animateScrollToPage((state.currentPage + 1) % pageCount)
            }
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box {
            if (orientation == KCarouselOrientation.Horizontal) {
                HorizontalPager(state = state, contentPadding = contentPadding, pageSpacing = pageSpacing, modifier = Modifier.fillMaxWidth()) { page ->
                    Box(modifier = Modifier.fillMaxWidth()) { content(page) }
                }
            } else {
                VerticalPager(state = state, contentPadding = contentPadding, pageSpacing = pageSpacing, modifier = Modifier.fillMaxWidth()) { page ->
                    Box(modifier = Modifier.fillMaxWidth()) { content(page) }
                }
            }

            if (showArrows && orientation == KCarouselOrientation.Horizontal) {
                CarouselArrow(left = true, enabled = state.currentPage > 0, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)) {
                    scope.launch { state.animateScrollToPage(state.currentPage - 1) }
                }
                CarouselArrow(left = false, enabled = state.currentPage < pageCount - 1, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)) {
                    scope.launch { state.animateScrollToPage(state.currentPage + 1) }
                }
            }
        }

        if (showDots && pageCount > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val cs = MaterialTheme.colorScheme
                repeat(pageCount) { i ->
                    val active = i == state.currentPage
                    Surface(
                        onClick  = { scope.launch { state.animateScrollToPage(i) } },
                        shape    = CircleShape,
                        color    = if (active) cs.primary else cs.outline,
                        modifier = Modifier.size(if (active) 8.dp else 6.dp)
                    ) {}
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarouselArrow(left: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick      = onClick,
        enabled      = enabled,
        shape        = RoundedCornerShape(6.dp),
        color        = cs.surface.copy(alpha = 0.85f),
        contentColor = if (enabled) cs.onSurface else cs.onSurface.copy(alpha = 0.38f),
        shadowElevation = 2.dp,
        modifier     = modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector  = if (left) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier     = Modifier.size(20.dp)
            )
        }
    }
}
