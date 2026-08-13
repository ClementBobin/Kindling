package dev.kindling.core.components.ui.empty

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shadcn/ui-style Empty state root.
 *
 * ```kotlin
 * KEmpty {
 *     KEmptyHeader {
 *         KEmptyMedia { Icon(Icons.Outlined.FolderOpen, null) }
 *         KEmptyTitle("No Projects Yet")
 *         KEmptyDescription("Create your first project to get started.")
 *     }
 *     KEmptyContent {
 *         KButton(text = "Create Project", onClick = { })
 *     }
 * }
 * ```
 */
@Composable
fun KEmpty(
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
    showBackground: Boolean = false,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (showBackground) Modifier.clip(shape).background(cs.surfaceVariant.copy(alpha = 0.4f)) else Modifier)
            .then(if (outlined) Modifier.clip(shape).border(1.dp, cs.outline, shape) else Modifier)
            .padding(if (outlined || showBackground) 32.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}