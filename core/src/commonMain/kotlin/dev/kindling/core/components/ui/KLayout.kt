package dev.kindling.core.components.layout.ui

import androidx.compose.foundation.layout.*
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

@Composable fun KSmallSpacer()      = Spacer(Modifier.height(8.dp))
@Composable fun KMediumSpacer()     = Spacer(Modifier.height(16.dp))
@Composable fun KLargeSpacer()      = Spacer(Modifier.height(24.dp))
@Composable fun KExtraLargeSpacer() = Spacer(Modifier.height(32.dp))
@Composable fun KCustomSpacer(height: Dp) = Spacer(Modifier.height(height))
