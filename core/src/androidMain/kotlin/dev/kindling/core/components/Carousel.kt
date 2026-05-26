package dev.kindling.core.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
//  KCarouselOrientation
// ─────────────────────────────────────────────

enum class KCarouselOrientation { Horizontal, Vertical }

// ─────────────────────────────────────────────
//  KCarouselAutoPlay
// ─────────────────────────────────────────────

/**
 * Configuration for auto-play behaviour.
 *
 * @param delay             Interval between automatic page advances (ms). Default 3 000.
 * @param delayBeforeResume After the user manually taps an arrow, how long to wait before
 *                          restarting auto-play (ms). Defaults to [delay].
 *
 * ```kotlin
 * KCarouselAutoPlay(delay = 4_000, delayBeforeResume = 8_000)
 * ```
 */
data class KCarouselAutoPlay(
    val delay: Long = 3_000L,
    val delayBeforeResume: Long = delay
)

// ─────────────────────────────────────────────
//  KCarousel
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Carousel backed by [HorizontalPager] / [VerticalPager].
 *
 * Arrows sit **beside** the pager in a Row (horizontal) or above/below (vertical),
 * so they are always vertically centred relative to the slide — not overlaid inside it.
 *
 * ```kotlin
 * // Basic
 * KCarousel(pageCount = 5) { page ->
 *     KCard { KCardContent { Text("Slide ${page + 1}") } }
 * }
 *
 * // 3 visible, auto-play, footer
 * val state = rememberPagerState { items.size }
 * KCarousel(
 *     pageCount    = items.size,
 *     visibleItems = 3,
 *     autoPlay     = KCarouselAutoPlay(delay = 4_000, delayBeforeResume = 8_000),
 *     state        = state,
 *     footer       = { KCarouselFooter { KCarouselSlideCounter(state, items.size) } }
 * ) { page -> MySlide(items[page]) }
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KCarousel(
    pageCount: Int,
    modifier: Modifier = Modifier,
    orientation: KCarouselOrientation = KCarouselOrientation.Horizontal,

    // ── Visible items ────────────────────────────────────────────────────
    /** Number of slides visible simultaneously. Arrows still scroll one at a time. */
    visibleItems: Int = 1,

    // ── Navigation ──────────────────────────────────────────────────────
    showArrows: Boolean = true,
    showDots: Boolean = pageCount > 1,

    // ── Auto-play ───────────────────────────────────────────────────────
    autoPlay: KCarouselAutoPlay? = null,

    // ── Layout ──────────────────────────────────────────────────────────
    pageSpacing: Dp = 8.dp,
    /**
     * Explicit height for the [VerticalPager]. Has no effect on horizontal carousels
     * (which size to their slide content naturally). Required for vertical because
     * [VerticalPager] needs a bounded height constraint to render.
     */
    verticalPagerHeight: Dp = 300.dp,
    state: PagerState = rememberPagerState { pageCount },

    // ── Footer slot ─────────────────────────────────────────────────────
    footer: (@Composable () -> Unit)? = null,

    content: @Composable BoxScope.(page: Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    // ── Auto-play ────────────────────────────────────────────────────────
    val autoPlayJobHolder = remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun startAutoPlay() {
        autoPlay ?: return
        autoPlayJobHolder.value?.cancel()
        autoPlayJobHolder.value = scope.launch {
            while (true) {
                delay(autoPlay.delay)
                state.animateScrollToPage((state.currentPage + 1) % pageCount)
            }
        }
    }

    LaunchedEffect(autoPlay) { startAutoPlay() }
    DisposableEffect(Unit) { onDispose { autoPlayJobHolder.value?.cancel() } }

    fun navigateTo(page: Int) {
        scope.launch {
            autoPlayJobHolder.value?.cancel()
            state.animateScrollToPage(page)
            if (autoPlay != null) {
                delay(autoPlay.delayBeforeResume)
                startAutoPlay()
            }
        }
    }

    // ── Content padding for visibleItems > 1 ────────────────────────────
    val contentPadding: PaddingValues = when {
        visibleItems <= 1 -> PaddingValues(0.dp)
        orientation == KCarouselOrientation.Horizontal ->
            PaddingValues(horizontal = (20 * (visibleItems - 1)).dp)
        else ->
            PaddingValues(vertical = (20 * (visibleItems - 1)).dp)
    }

    // ── Root column ──────────────────────────────────────────────────────
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Top arrow — vertical only ────────────────────────────────────
        if (showArrows && orientation == KCarouselOrientation.Vertical) {
            KCarouselArrowButton(
                direction = KCarouselArrowDirection.Up,
                enabled   = state.currentPage > 0,
                onClick   = { navigateTo(state.currentPage - 1) }
            )
        }

        // ── Pager row: [← arrow] [pager] [→ arrow] ──────────────────────
        // Arrows are siblings of the pager in a Row, NOT overlaid inside it.
        // This guarantees they are always vertically centred on the slide
        // regardless of slide content height.
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left arrow
            if (showArrows && orientation == KCarouselOrientation.Horizontal) {
                KCarouselArrowButton(
                    direction = KCarouselArrowDirection.Left,
                    enabled   = state.currentPage > 0,
                    modifier  = Modifier.padding(end = 8.dp),
                    onClick   = { navigateTo(state.currentPage - 1) }
                )
            }

            // Pager — horizontal wraps slide content height naturally;
            // vertical needs an explicit bounded height to render.
            if (orientation == KCarouselOrientation.Horizontal) {
                HorizontalPager(
                    state          = state,
                    contentPadding = contentPadding,
                    pageSpacing    = pageSpacing,
                    modifier       = Modifier.weight(1f)
                ) { page ->
                    Box(modifier = Modifier.fillMaxWidth()) { content(page) }
                }
            } else {
                VerticalPager(
                    state          = state,
                    contentPadding = contentPadding,
                    pageSpacing    = pageSpacing,
                    modifier       = Modifier
                        .weight(1f)
                        .height(verticalPagerHeight) // bounded height required for VerticalPager
                ) { page ->
                    Box(modifier = Modifier.fillMaxWidth()) { content(page) }
                }
            }

            // Right arrow
            if (showArrows && orientation == KCarouselOrientation.Horizontal) {
                KCarouselArrowButton(
                    direction = KCarouselArrowDirection.Right,
                    enabled   = state.currentPage < pageCount - 1,
                    modifier  = Modifier.padding(start = 8.dp),
                    onClick   = { navigateTo(state.currentPage + 1) }
                )
            }
        }

        // ── Bottom arrow — vertical only ─────────────────────────────────
        if (showArrows && orientation == KCarouselOrientation.Vertical) {
            KCarouselArrowButton(
                direction = KCarouselArrowDirection.Down,
                enabled   = state.currentPage < pageCount - 1,
                onClick   = { navigateTo(state.currentPage + 1) }
            )
        }

        // ── Dot indicators ───────────────────────────────────────────────
        if (showDots) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val cs = MaterialTheme.colorScheme
                repeat(pageCount) { i ->
                    val active = i == state.currentPage
                    Surface(
                        onClick  = { navigateTo(i) },
                        shape    = CircleShape,
                        color    = if (active) cs.primary else cs.outline,
                        modifier = Modifier.size(if (active) 8.dp else 6.dp)
                    ) {}
                }
            }
        }

        // ── Footer slot ──────────────────────────────────────────────────
        footer?.invoke()
    }
}

// ─────────────────────────────────────────────
//  KCarouselFooter
// ─────────────────────────────────────────────

/**
 * Footer row rendered below the carousel. Pass via [KCarousel]'s `footer` param.
 *
 * ```kotlin
 * KCarousel(
 *     pageCount = 5,
 *     footer    = { KCarouselFooter { KCarouselSlideCounter(state, 5) } }
 * ) { page -> … }
 * ```
 */
@Composable
fun KCarouselFooter(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment     = Alignment.CenterVertically,
        content               = content
    )
}

// ─────────────────────────────────────────────
//  KCarouselSlideCounter
// ─────────────────────────────────────────────

/**
 * Pre-built "Slide N of M" label for [KCarouselFooter].
 *
 * ```kotlin
 * KCarouselFooter {
 *     KCarouselSlideCounter(state, 5)                          // "Slide 1 of 5"
 *     KCarouselSlideCounter(state, 5) { n, t -> "$n / $t" }   // custom format
 * }
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KCarouselSlideCounter(
    state: PagerState,
    total: Int,
    modifier: Modifier = Modifier,
    label: (current: Int, total: Int) -> String = { n, t -> "Slide $n of $t" }
) {
    Text(
        text     = label(state.currentPage + 1, total),
        style    = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
//  Internal — arrow button
// ─────────────────────────────────────────────

private enum class KCarouselArrowDirection { Left, Right, Up, Down }

@Composable
private fun KCarouselArrowButton(
    direction: KCarouselArrowDirection,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick         = onClick,
        enabled         = enabled,
        shape           = RoundedCornerShape(6.dp),
        color           = cs.surface.copy(alpha = 0.85f),
        contentColor    = if (enabled) cs.onSurface else cs.onSurface.copy(alpha = 0.38f),
        shadowElevation = 2.dp,
        modifier        = modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = when (direction) {
                    KCarouselArrowDirection.Left  -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    KCarouselArrowDirection.Right -> Icons.AutoMirrored.Filled.KeyboardArrowRight
                    KCarouselArrowDirection.Up    -> Icons.Default.KeyboardArrowUp
                    KCarouselArrowDirection.Down  -> Icons.Default.KeyboardArrowDown
                },
                contentDescription = when (direction) {
                    KCarouselArrowDirection.Left,
                    KCarouselArrowDirection.Up   -> "Previous"
                    KCarouselArrowDirection.Right,
                    KCarouselArrowDirection.Down -> "Next"
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}