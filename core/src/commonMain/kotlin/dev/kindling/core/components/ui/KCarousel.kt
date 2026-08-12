package dev.kindling.core.components.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
//  Carousel API state
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  Auto-play config
// ─────────────────────────────────────────────

data class KCarouselAutoPlay(
    val delay: Long = 3_000L,
    val delayBeforeResume: Long = delay
)

// ─────────────────────────────────────────────
//  KCarousel (root)
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Carousel — mirrors `carousel.tsx`.
 *
 * Arrows are [KButton] instances rendered beside the pager (not overlaid).
 * Respects [LocalLayoutDirection] — arrows are visually mirrored in RTL.
 *
 * ```kotlin
 * val api = rememberCarouselApi(pageCount = items.size)
 * KCarousel(api = api) {
 *     KCarouselContent {
 *         items.forEach { item ->
 *             KCarouselItem { MySlide(item) }
 *         }
 *     }
 * }
 * // Anywhere:
 * Text("Slide ${api.currentSlide + 1} / ${api.slideCount}")
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KCarousel(
    api: CarouselApi,
    modifier: Modifier = Modifier,
    showArrows: Boolean = true,
    showDots: Boolean = api.slideCount > 1,
    autoPlay: KCarouselAutoPlay? = null,
    pageSpacing: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val scope  = rememberCoroutineScope()
    val state  = api.pagerState
    //val rtl    = LocalLayoutDirection.current == LayoutDirection.Rtl

    // ── Auto-play ────────────────────────────────────────────────────────
    val autoPlayJob = remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun startAutoPlay() {
        autoPlay ?: return
        autoPlayJob.value?.cancel()
        autoPlayJob.value = scope.launch {
            while (true) {
                delay(autoPlay.delay)
                state.animateScrollToPage((state.currentPage + 1) % state.pageCount)
            }
        }
    }

    LaunchedEffect(autoPlay) { startAutoPlay() }
    DisposableEffect(Unit) { onDispose { autoPlayJob.value?.cancel() } }

    fun navigateTo(page: Int) {
        scope.launch {
            autoPlayJob.value?.cancel()
            state.animateScrollToPage(page)
            if (autoPlay != null) {
                delay(autoPlay.delayBeforeResume)
                startAutoPlay()
            }
        }
    }

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pager row + arrows
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showArrows) {
                KCarouselPrevious(
                    onClick  = { navigateTo(state.currentPage - 1) },
                    enabled  = state.currentPage > 0
                )
            }

            if (showArrows) {
                KCarouselNext(
                    onClick  = { navigateTo(state.currentPage + 1) },
                    enabled  = state.currentPage < state.pageCount - 1
                )
            }
        }

        // Dot indicators
        if (showDots) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val cs = MaterialTheme.colorScheme
                repeat(state.pageCount) { i ->
                    Surface(
                        onClick  = { navigateTo(i) },
                        shape    = CircleShape,
                        color    = if (i == state.currentPage) cs.primary else cs.outline,
                        modifier = Modifier.size(if (i == state.currentPage) 8.dp else 6.dp)
                    ) {}
                }
            }
        }

        // Extra slots (KCarouselContent etc.)
        content()
    }
}

// ─────────────────────────────────────────────
//  Slot-based API (mirrors web component slots)
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  KCarouselPrevious — uses KButton
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  KCarouselNext — uses KButton
// ─────────────────────────────────────────────

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