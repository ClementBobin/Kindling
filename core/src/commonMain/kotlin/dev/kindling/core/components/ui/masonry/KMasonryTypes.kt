package dev.kindling.core.components.ui.masonry

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KMasonryItem(
    /** Text content of the card. */
    val text: String,
    /** Optional title above the text. */
    val title: String? = null
)

object KMasonryGridDefaults {
    const val Columns: Int = 3
    val Gap: Dp = 12.dp
    val CardPadding: Dp = 16.dp
}