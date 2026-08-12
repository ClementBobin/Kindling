package dev.kindling.core.components.ui.masonry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A responsive masonry grid layout component for Jetpack Compose that distributes cards
 * into columns based on measured item heights, stacking them into the shortest column dynamically.
 *
 * @param items List of masonry items containing title and text content.
 * @param modifier Applied to the outer masonry grid container.
 * @param columns Number of columns in the grid.
 * @param gap Spacing gap between cards in DP.
 * @param cardPadding Internal padding applied inside each masonry card.
 */
@Composable
fun KMasonryGrid(
    items: List<KMasonryItem>,
    modifier: Modifier = Modifier,
    columns: Int = KMasonryGridDefaults.Columns,
    gap: Dp = KMasonryGridDefaults.Gap,
    cardPadding: Dp = KMasonryGridDefaults.CardPadding
) {
    if (items.isEmpty()) return

    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        val containerWidth = constraints.maxWidth
        if (containerWidth <= 0) {
            return@SubcomposeLayout layout(0, 0) {}
        }

        val gapPx = gap.roundToPx()
        val totalGapWidth = gapPx * (columns - 1)
        val colWidth = (containerWidth - totalGapWidth) / columns

        // 1. Measure all item cards using subcompose to get accurate dynamic heights
        val placeables = items.mapIndexed { index, item ->
            val measurable = subcompose(index) {
                Card(
                    modifier = Modifier.width(with(density) { colWidth.toDp() }),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(cardPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (item.title != null) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }.first()

            measurable.measure(Constraints.fixedWidth(colWidth))
        }

        // 2. Compute column layout coordinates (shortest-column distribution algorithm)
        val colHeights = IntArray(columns) { 0 }
        val cardPositions = placeables.map { placeable ->
            var shortestCol = 0
            for (c in 1 until columns) {
                if (colHeights[c] < colHeights[shortestCol]) {
                    shortestCol = c
                }
            }

            val x = shortestCol * (colWidth + gapPx)
            val y = colHeights[shortestCol]

            colHeights[shortestCol] += placeable.height + gapPx

            Pair(x, y)
        }

        val totalHeight = colHeights.maxOrNull()?.let { it - gapPx } ?: 0

        // 3. Layout placeables at their calculated masonry positions
        layout(containerWidth, totalHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (x, y) = cardPositions[index]
                placeable.placeRelative(x = x, y = y)
            }
        }
    }
}