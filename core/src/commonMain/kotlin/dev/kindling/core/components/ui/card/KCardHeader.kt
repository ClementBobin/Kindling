package dev.kindling.core.components.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes

/**
 * A container for card titles and descriptions, with an optional trailing [action] slot.
 *
 * Respects [androidx.compose.ui.platform.LocalLayoutDirection] for proper alignment of content and action.
 *
 * ### Example usage:
 * ```kotlin
 * KCardHeader(
 *     action = {
 *         IconButton(onClick = { /* ... */ }) {
 *             Icon(Icons.Default.MoreVert, contentDescription = "Settings")
 *         }
 *     }
 * ) {
 *     KCardTitle("Project Status")
 *     KCardDescription("Current progress of the Kindling library.")
 * }
 * ```
 *
 * @param modifier The modifier to be applied to the header layout.
 * @param action Optional trailing composable slot (e.g., for a menu button or badge).
 * @param content The title and description components.
 */
@Composable
fun KCardHeader(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val rounded = LocalKindlingShapes.current.roundedXl
    Row(
        modifier              = modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(topStart = rounded)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Column(
            modifier           = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content            = content
        )
        if (action != null) {
            Box(Modifier.padding(start = 12.dp), contentAlignment = Alignment.TopEnd) {
                action()
            }
        }
    }
}

@Composable
fun KCardTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, lineHeight = 20.sp),
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
fun KCardDescription(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/** Trailing action slot — pass as `action` param of [KCardHeader]. */
@Composable
fun KCardAction(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) { content() }
}