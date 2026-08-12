package dev.kindling.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes

// ─────────────────────────────────────────────
//  KCard
// ─────────────────────────────────────────────

enum class KCardSize { Default, Sm }

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
    val shape  = LocalKindlingShapes.current.radiusXl
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

// ─────────────────────────────────────────────
//  KCardHeader
// ─────────────────────────────────────────────

/**
 * Card header with an optional trailing [action] slot.
 * Respects [LocalLayoutDirection].
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
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content             = content
        )
        if (action != null) {
            Box(Modifier.padding(start = 12.dp), contentAlignment = Alignment.TopEnd) {
                action()
            }
        }
    }
}

// ─────────────────────────────────────────────
//  KCardTitle
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  KCardDescription
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  KCardAction
// ─────────────────────────────────────────────

/** Trailing action slot — pass as `action` param of [KCardHeader]. */
@Composable
fun KCardAction(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) { content() }
}

// ─────────────────────────────────────────────
//  KCardContent
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
//  KCardFooter
// ─────────────────────────────────────────────

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