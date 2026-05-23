package dev.kindling.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  KCard
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Card container.
 *
 * Mirrors `card.tsx` — `sm` size reduces padding and gaps.
 *
 * ```kotlin
 * KCard {
 *     KCardHeader {
 *         KCardTitle("Notifications")
 *         KCardDescription("You have 3 unread messages.")
 *     }
 *     KCardContent { Text("Content goes here.") }
 *     KCardFooter { KButton(text = "Dismiss", onClick = {}) }
 * }
 * ```
 */
@Composable
fun KCard(
    modifier: Modifier = Modifier,
    size: KCardSize = KCardSize.Default,
    content: @Composable ColumnScope.() -> Unit
) {
    val verticalPadding    = if (size == KCardSize.Sm) 12.dp else 16.dp
    val gap                = if (size == KCardSize.Sm) 8.dp  else 16.dp

    Surface(
        modifier      = modifier,
        shape         = RoundedCornerShape(0.dp),
        color         = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(gap),
            content             = content
        )
    }
}

// ─────────────────────────────────────────────
//  KCardHeader
// ─────────────────────────────────────────────

/**
 * Card header section — holds [KCardTitle], [KCardDescription], and an
 * optional [KCardAction] trailing slot.
 *
 * Mirrors the `card-header` slot with `has-data-[slot=card-action]` grid layout.
 */
@Composable
fun KCardHeader(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content             = content
        )
        if (action != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) { action() }
        }
    }
}

// ─────────────────────────────────────────────
//  KCardTitle
// ─────────────────────────────────────────────

/**
 * Card title — medium-weight label at the top of the card.
 */
@Composable
fun KCardTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
//  KCardDescription
// ─────────────────────────────────────────────

/**
 * Muted secondary text below the card title.
 */
@Composable
fun KCardDescription(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
//  KCardAction
// ─────────────────────────────────────────────

/**
 * Trailing action slot rendered in the top-right of [KCardHeader].
 * Pass it as the `action` parameter of [KCardHeader].
 */
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

/**
 * Main card body — horizontally padded content area.
 */
@Composable
fun KCardContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        content  = content
    )
}

// ─────────────────────────────────────────────
//  KCardFooter
// ─────────────────────────────────────────────

/**
 * Card footer — renders a top-bordered action row.
 *
 * Mirrors `card-footer` with `border-t` style.
 */
@Composable
fun KCardFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content               = content
    )
}