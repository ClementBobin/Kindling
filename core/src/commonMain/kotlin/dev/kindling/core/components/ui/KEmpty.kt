package dev.kindling.core.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

@Composable
fun KEmptyHeader(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
fun KEmptyMedia(
    modifier: Modifier = Modifier,
    variant: KEmptyMediaVariant = KEmptyMediaVariant.Icon,
    size: Dp = 56.dp,
    iconBoxColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable BoxScope.() -> Unit
) {
    val boxMod = when (variant) {
        KEmptyMediaVariant.Icon   -> modifier.size(size).clip(RoundedCornerShape(12.dp)).background(iconBoxColor).padding(12.dp)
        KEmptyMediaVariant.Avatar -> modifier.size(size).clip(CircleShape)
        KEmptyMediaVariant.Image  -> modifier.size(size)
    }
    Box(modifier = boxMod, contentAlignment = Alignment.Center, content = content)
}

@Composable
fun KEmptyTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
}

@Composable
fun KEmptyDescription(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier.padding(horizontal = 16.dp), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 20.sp)
}

@Composable
fun KEmptyContent(modifier: Modifier = Modifier, verticalSpacing: Dp = 8.dp, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(8.dp))
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(verticalSpacing), content = content)
}

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
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(4.dp))
            KEmptyTitle(title)
            KEmptyDescription(description)
        }
        if (actionLabel != null || secondaryActionLabel != null) {
            KEmptyContent {
                if (actionLabel != null && onAction != null) KButton(actionLabel, onAction)
                if (secondaryActionLabel != null && onSecondaryAction != null) KButton(secondaryActionLabel, onSecondaryAction, variant = KButtonVariant.Outline)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
