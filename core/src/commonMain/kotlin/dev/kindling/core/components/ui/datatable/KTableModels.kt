package dev.kindling.core.components.ui.datatable

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign

/**
 * Describes one column in a [KDataTable] preset.
 *
 * This is the data-driven convenience layer on top of the
 * primitive [Table] / [TableHead] / [TableCell] slots.
 */
data class KTableColumn<T>(
    val key: String,
    val header: String,
    val weight: Float = 1f,
    val sortable: Boolean = false,
    val align: TextAlign = TextAlign.Start,
    val cell: @Composable RowScope.(row: T) -> Unit
)