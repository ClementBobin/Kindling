package dev.kindling.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.KAspectRatio
import dev.kindling.core.components.ui.animated.KHighlightStyle
import dev.kindling.core.components.ui.animated.KHighlightedText
import dev.kindling.core.components.ui.animated.KMarquee
import dev.kindling.core.components.ui.animated.KMarqueeDirection
import dev.kindling.core.components.ui.animated.KTextMarquee
import dev.kindling.core.components.ui.animated.KWordsStagger
import dev.kindling.core.components.ui.carousel.KCarousel
import dev.kindling.core.components.ui.carousel.KCarouselContent
import dev.kindling.core.components.ui.carousel.KCarouselItem
import dev.kindling.core.components.ui.carousel.rememberCarouselApi
import dev.kindling.core.components.ui.logosCarousel.KLogo
import dev.kindling.core.components.ui.logosCarousel.KLogosCarousel
import dev.kindling.core.components.ui.masonry.KMasonryGrid
import dev.kindling.core.components.ui.masonry.KMasonryItem
import dev.kindling.core.components.ui.picker.KRoulettePicker

@Composable
fun AspectRatioDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.width(260.dp)) {
        KAspectRatio(ratio = 16f / 9f, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("16 / 9", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Text("Content keeps its ratio at any width.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CarouselDemo() {
    val colors = listOf(
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary,
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    )
    val api = rememberCarouselApi(pageCount = colors.size)
    KCarousel(api = api, modifier = Modifier.width(340.dp)) {
        KCarouselContent(api = api) { page ->
            KCarouselItem {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors[page].first),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Slide ${page + 1}", color = colors[page].second)
                }
            }
        }
        Text(
            "${api.currentSlide + 1} / ${api.slideCount}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LogosCarouselDemo() {
    val logos = remember {
        listOf("Acme", "Globex", "Initech", "Umbrella", "Hooli", "Stark").map { KLogo(alt = it) }
    }
    KLogosCarousel(
        logos = logos,
        modifier = Modifier.width(340.dp).height(48.dp),
        customLogoContent = { logo ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(logo.alt, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun MasonryDemo() {
    val items = remember {
        listOf(
            KMasonryItem(title = "Fast", text = "Lightweight components."),
            KMasonryItem(title = "Themed", text = "Follows your color scheme and shapes across every card, with longer copy."),
            KMasonryItem(text = "Short note."),
            KMasonryItem(title = "Shortest column", text = "Cards fall into the shortest column."),
            KMasonryItem(title = "Multiplatform", text = "One codebase.")
        )
    }
    KMasonryGrid(
        items = items,
        modifier = Modifier.width(360.dp),
        columns = 2,
        gap = 8.dp,
        cardPadding = 12.dp
    )
}

@Composable
fun PickerDemo() {
    val items = remember { listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") }
    var selected by remember { mutableIntStateOf(2) }
    Column(
        modifier = Modifier.width(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KRoulettePicker(
            items = items,
            selectedIndex = selected,
            onItemSelected = { selected = it },
            visibleItemsCount = 5,
            itemHeight = 36.dp
        )
        Text("Selected: ${items[selected]}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AnimatedDemo() {
    val cs = MaterialTheme.colorScheme
    val words = remember { listOf("Compose", "Kotlin", "Multiplatform", "Kindling") }
    Column(
        modifier = Modifier.width(340.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KWordsStagger(
            text = "Build beautiful interfaces, one word at a time.",
            style = MaterialTheme.typography.titleMedium.copy(color = cs.onSurface)
        )
        KHighlightedText(
            text = "Highlighted text",
            style = KHighlightStyle.BACKGROUND,
            textColor = Color(0xFF1C1917)
        )
        KMarquee(
            modifier = Modifier.fillMaxWidth().height(32.dp),
            direction = KMarqueeDirection.FORWARD,
            spacing = 16.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                words.forEach { Text(it, color = cs.onSurfaceVariant) }
            }
        }
        KTextMarquee(
            items = words.map { w -> { Text(w, color = cs.onSurface) } },
            prefix = { Text("Built with", color = cs.onSurfaceVariant) },
            containerHeight = 80.dp,
            itemHeight = 28.dp
        )
    }
}
