package dev.kindling.core.components.ui.carousel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Public state exposed by [KCarousel] — mirrors the web `CarouselApi`.
 *
 * Access via `rememberCarouselApi()` and pass to [KCarousel].
 */
@OptIn(ExperimentalFoundationApi::class)
class CarouselApi internal constructor(
    internal val pagerState: PagerState
) {
    /** Zero-based index of the currently visible slide. */
    val currentSlide: Int get() = pagerState.currentPage

    /** Total number of slides. */
    val slideCount: Int get() = pagerState.pageCount

    /** Whether the user can scroll to the previous slide. */
    val canScrollPrev: Boolean get() = pagerState.currentPage > 0

    /** Whether the user can scroll to the next slide. */
    val canScrollNext: Boolean get() = pagerState.currentPage < pagerState.pageCount - 1
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberCarouselApi(pageCount: Int): CarouselApi {
    val pager = rememberPagerState { pageCount }
    return remember(pager) { CarouselApi(pager) }
}

data class KCarouselAutoPlay(
    val delay: Long = 3_000L,
    val delayBeforeResume: Long = delay
)