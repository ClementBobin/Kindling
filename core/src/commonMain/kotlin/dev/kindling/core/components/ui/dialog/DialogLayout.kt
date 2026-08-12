package dev.kindling.core.components.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes
import dev.kindling.core.components.ui.KButton
import dev.kindling.core.components.ui.KButtonVariant

@Composable
fun DialogHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier          = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content           = content
    )
}

@Composable
fun DialogTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
fun DialogDescription(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * Footer row with optional built-in close button.
 * Uses [dev.kindling.core.components.ui.KButton] for [showCloseButton].
 */
@Composable
fun DialogFooter(
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val rounded = LocalKindlingShapes.current.roundedXl
    val cs = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = rounded, bottomEnd = rounded))) {
        HorizontalDivider(color = cs.outlineVariant, thickness = 0.5.dp)
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(cs.surfaceVariant.copy(.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            content()
            if (showCloseButton && onDismiss != null) {
                KButton(
                    onClick = onDismiss,
                    variant = KButtonVariant.Outline
                ) { Text("Close") }
            }
        }
    }
}