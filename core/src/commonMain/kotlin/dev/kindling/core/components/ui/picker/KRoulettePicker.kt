package dev.kindling.core.components.ui.picker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import dev.kindling.core.theme.kindlingShapes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KRoulettePicker(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 5,
    itemHeight: Dp = 40.dp
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedIndex - visibleItemsCount / 2).coerceAtLeast(0)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val shapes = MaterialTheme.kindlingShapes

    val currentCenterIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                selectedIndex
            } else {
                val containerCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                val closest = visibleItems.minByOrNull { item ->
                    kotlin.math.abs((item.offset + item.size / 2) - containerCenter)
                }
                closest?.index?.minus(visibleItemsCount / 2)?.coerceIn(0, (items.size - 1).coerceAtLeast(0)) ?: selectedIndex
            }
        }
    }

    LaunchedEffect(currentCenterIndex) {
        if (currentCenterIndex != selectedIndex && currentCenterIndex in items.indices) {
            onItemSelected(currentCenterIndex)
        }
    }

    val totalHeight = itemHeight * visibleItemsCount

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .drawWithCache {
                    val gradient = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black, Color.Transparent),
                        startY = 0f,
                        endY = size.height
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush = gradient, blendMode = BlendMode.DstIn)
                    }
                }
        ) {
            // Padding items at top and bottom to center the selection
            val paddingCount = visibleItemsCount / 2
            items(paddingCount) {
                Box(modifier = Modifier.fillMaxWidth().height(itemHeight))
            }

            items(items.size) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = index == currentCenterIndex
                    Text(
                        text = items[index],
                        style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(paddingCount) {
                Box(modifier = Modifier.fillMaxWidth().height(itemHeight))
            }
        }

        // iOS style central selection indicator borders
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .height(itemHeight)
                .padding(horizontal = 16.dp)
        ) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}