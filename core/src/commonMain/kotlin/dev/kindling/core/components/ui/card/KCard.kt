package dev.kindling.core.components.ui.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.kindling.core.theme.LocalKindlingShapes

/**
 * Shadcn/ui-style Card root — mirrors `card.tsx`.
 *
 * ```kotlin
 * KCard {
 *     KCardHeader { KCardTitle("Title"); KCardDescription("Desc") }
 *     KCardContent { Text("Body") }
 *     KCardFooter { KButton("Action", onClick = {}) }
 * }
 * ```
 */
@Composable
fun KCard(
    modifier: Modifier = Modifier,
    size: KCardSize = KCardSize.Default,
    image: Painter? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val vPad    = if (size == KCardSize.Sm) 12.dp else 16.dp
    val gap     = if (size == KCardSize.Sm) 8.dp  else 12.dp
    val shape   = LocalKindlingShapes.current.radiusXl
    val rounded = LocalKindlingShapes.current.roundedXl

    Surface(
        modifier        = modifier,
        shape           = shape,
        color           = MaterialTheme.colorScheme.surface,
        tonalElevation  = 0.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (image != null) {
                Image(
                    painter            = image,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = rounded, topEnd = rounded))
                )
            }
            Column(
                modifier            = Modifier.fillMaxWidth().padding(vertical = vPad),
                verticalArrangement = Arrangement.spacedBy(gap),
                content             = content
            )
        }
    }
}

@Composable
fun KCardContent(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier            = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = verticalArrangement,
        content             = content
    )
}

@Composable
fun KCardFooter(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    content: @Composable RowScope.() -> Unit
) {
    val rounded = LocalKindlingShapes.current.roundedXl
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
    Row(
        modifier              = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).clip(RoundedCornerShape(topEnd = rounded)),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment     = Alignment.CenterVertically,
        content               = content
    )
}