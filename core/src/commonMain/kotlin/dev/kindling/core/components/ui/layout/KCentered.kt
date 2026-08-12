package dev.kindling.core.components.layout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fills its parent and centres content. */
@Composable
fun KCenteredBox(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 32.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier         = modifier.fillMaxSize().padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
        content          = content
    )
}

/** Full-width column, content centred by default. */
@Composable
fun KCenteredColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        modifier            = modifier.fillMaxWidth(),
        content             = content
    )
}