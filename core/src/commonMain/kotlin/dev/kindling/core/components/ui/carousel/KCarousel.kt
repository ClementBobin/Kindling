package dev.kindling.core.components.ui.carousel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shadcn/ui-style Carousel — mirrors `carousel.tsx`.
 *
 * Arrows are [dev.kindling.core.components.ui.KButton] instances rendered beside the pager (not overlaid).
 * Respects [KLocalLayoutDirection] — arrows are visually mirrored in RTL.
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
 * Text("Slide ${api.currentSlide + 1} /${api.slideCount}")
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

    // ── Auto-play ────────────────────────────────────────────────────────
    val autoPlayJob = remember { mutableStateOf<Job?>(null) }

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
                    onClick = { navigateTo(state.currentPage - 1) },
                    enabled = state.currentPage > 0
                )
            }

            if (showArrows) {
                KCarouselNext(
                    onClick = { navigateTo(state.currentPage + 1) },
                    enabled = state.currentPage < state.pageCount - 1
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