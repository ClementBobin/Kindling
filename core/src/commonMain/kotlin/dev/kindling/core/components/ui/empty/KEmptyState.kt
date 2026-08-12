package dev.kindling.core.components.ui.empty

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.KButton
import dev.kindling.core.components.ui.KButtonVariant

/**
 * Convenience preset: icon + title + description + optional CTA buttons.
 *
 * ```kotlin
 * KEmptyState(
 *     icon        = Icons.Outlined.FolderOpen,
 *     title       = "No projects",
 *     description = "Create your first project.",
 *     actionLabel = "Create Project",
 *     onAction    = { }
 * )
 * ```
 */
@Composable
fun KEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
    showBackground: Boolean = false,
    actionLabel: String? = null,
    secondaryActionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    KEmpty(modifier = modifier, outlined = outlined, showBackground = showBackground) {
        KEmptyHeader {
            Spacer(Modifier.height(8.dp))
            KEmptyMedia(variant = KEmptyMediaVariant.Icon) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(4.dp))
            KEmptyTitle(title)
            KEmptyDescription(description)
        }
        if (actionLabel != null || secondaryActionLabel != null) {
            KEmptyContent {
                if (actionLabel != null && onAction != null) KButton(actionLabel, onAction)
                if (secondaryActionLabel != null && onSecondaryAction != null) KButton(
                    secondaryActionLabel,
                    onSecondaryAction,
                    variant = KButtonVariant.Outline
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}