package dev.kindling.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  KCardSize
// ─────────────────────────────────────────────

enum class KCardSize { Default, Sm }

// ─────────────────────────────────────────────
//  KCard
// ─────────────────────────────────────────────

/**
 * Root card container — dark rounded surface matching the shadcn/ui nova style.
 *
 * Supports an optional [image] painter rendered full-width above the header,
 * and a [size] prop that tightens internal spacing when set to [KCardSize.Sm].
 *
 * ```kotlin
 * // Default card
 * KCard {
 *     KCardHeader(action = { KCardAction { Text("Sign Up") } }) {
 *         KCardTitle("Login to your account")
 *         KCardDescription("Enter your email below to login to your account")
 *     }
 *     KCardContent { /* form fields */ }
 *     KCardFooter { KButton("Login", onClick = {}) }
 * }
 *
 * // Small card
 * KCard(size = KCardSize.Sm) {
 *     KCardHeader {
 *         KCardTitle("Small Card")
 *         KCardDescription("This card uses the small size variant.")
 *     }
 *     KCardContent { Text("The card component supports a size prop…") }
 *     KCardFooter { KButton("Action", onClick = {}) }
 * }
 *
 * // Image card
 * KCard(image = painterResource(R.drawable.meetup)) {
 *     KCardHeader(action = { KCardBadge("Featured") }) {
 *         KCardTitle("Design systems meetup")
 *         KCardDescription("A practical talk on component APIs…")
 *     }
 *     KCardFooter { KButton("View Event", onClick = {}) }
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
    val cornerRadius      = 12.dp
    val verticalPadding   = if (size == KCardSize.Sm) 12.dp else 16.dp
    val gap               = if (size == KCardSize.Sm) 8.dp  else 12.dp

    Surface(
        modifier        = modifier,
        shape           = RoundedCornerShape(cornerRadius),
        color           = MaterialTheme.colorScheme.surface,       // ~#1c1c1e in dark theme
        tonalElevation  = 0.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Optional full-bleed image ──────────────────────────
            if (image != null) {
                Image(
                    painter            = image,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(200.dp)                            // matches screenshot ratio
                        .clip(RoundedCornerShape(
                            topStart    = cornerRadius,
                            topEnd      = cornerRadius,
                            bottomStart = 0.dp,
                            bottomEnd   = 0.dp
                        ))
                )
            }

            // ── Slotted content ───────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(vertical = verticalPadding),
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
 * Header section with an optional trailing [action] slot (top-right).
 *
 * Mirrors the CSS grid layout that places [KCardAction] at `self-start`
 * in the top-right corner regardless of the title height.
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content             = content
        )
        if (action != null) {
            Box(
                modifier          = Modifier.padding(start = 12.dp),
                contentAlignment  = Alignment.TopEnd
            ) {
                action()
            }
        }
    }
}

// ─────────────────────────────────────────────
//  KCardTitle
// ─────────────────────────────────────────────

/**
 * Primary card title — rendered in [MaterialTheme.colorScheme.onSurface]
 * at 14 sp semi-bold.
 */
@Composable
fun KCardTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleSmall.copy(
            fontSize   = 14.sp,
            lineHeight = 20.sp
        ),
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
//  KCardDescription
// ─────────────────────────────────────────────

/**
 * Muted helper text beneath [KCardTitle].
 * Uses [MaterialTheme.colorScheme.onSurfaceVariant] at 12 sp.
 */
@Composable
fun KCardDescription(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.bodySmall.copy(
            fontSize   = 12.sp,
            lineHeight = 18.sp
        ),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
//  KCardAction
// ─────────────────────────────────────────────

/**
 * Trailing slot rendered in the top-right corner of [KCardHeader].
 *
 * Pass composable content (a [KButton], [Badge], plain [Text], etc.)
 * via the `action` parameter of [KCardHeader]:
 *
 * ```kotlin
 * KCardHeader(action = { KCardAction { Text("Sign Up") } }) { … }
 * ```
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
 * Main card body — full-width, horizontally padded.
 *
 * Use for form fields, body copy, or any free-form content.
 */
@Composable
fun KCardContent(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = verticalArrangement,
        content             = content
    )
}

// ─────────────────────────────────────────────
//  KCardFooter
// ─────────────────────────────────────────────

/**
 * Footer section separated from the card body by a thin divider.
 *
 * Renders action buttons full-width by default — pass [Arrangement.End]
 * to right-align them, or [Arrangement.SpaceBetween] for a split layout.
 *
 * ```kotlin
 * KCardFooter {
 *     KButton("Login",              onClick = {}, modifier = Modifier.weight(1f))
 *     KButton("Login with Google",  onClick = {}, modifier = Modifier.weight(1f))
 * }
 * ```
 */
@Composable
fun KCardFooter(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    content: @Composable RowScope.() -> Unit
) {
    HorizontalDivider(
        color     = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp
    )
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment     = Alignment.CenterVertically,
        content               = content
    )
}