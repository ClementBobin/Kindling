package dev.kindling.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Alignment enum
// ─────────────────────────────────────────────

/**
 * Position of an addon relative to the input control.
 *
 * Matches the `align` prop variants on `InputGroupAddon` in the React version:
 * `inline-start`, `inline-end`, `block-start`, `block-end`.
 */
enum class KInputGroupAlign {
    InlineStart,
    InlineEnd,
    BlockStart,
    BlockEnd
}

// ─────────────────────────────────────────────
//  KInputGroup
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style InputGroup — a bordered container that composes an input
 * control with inline or block addons (icons, text, buttons).
 *
 * Mirrors `input-group.tsx`.  The group draws a single shared border so that
 * all addons look visually attached to the field.
 *
 * ```kotlin
 * // Leading icon
 * KInputGroup {
 *     KInputGroupAddon(align = KInputGroupAlign.InlineStart) {
 *         Icon(Icons.Default.Search, contentDescription = null)
 *     }
 *     KInputGroupInput(value = query, onValueChange = { query = it }, placeholder = "Search…")
 * }
 *
 * // Trailing button
 * KInputGroup {
 *     KInputGroupInput(value = url, onValueChange = { url = it }, placeholder = "https://…")
 *     KInputGroupAddon(align = KInputGroupAlign.InlineEnd) {
 *         KInputGroupButton(onClick = { copy(url) }) { Icon(Icons.Default.ContentCopy, null) }
 *     }
 * }
 *
 * // Block label above
 * KInputGroup {
 *     KInputGroupAddon(align = KInputGroupAlign.BlockStart) { Text("https://") }
 *     KInputGroupInput(value = path, onValueChange = { path = it })
 * }
 * ```
 */
@Composable
fun KInputGroup(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    content: @Composable KInputGroupScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme

    val focusRequester = remember { FocusRequester() }
    val scope          = remember(focusRequester) { KInputGroupScope(focusRequester) }

    val borderColor = when {
        isError  -> cs.error
        else     -> cs.outline
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor)
    ) {
        scope.content()
    }
}

// ─────────────────────────────────────────────
//  Scope — passed to KInputGroup content block
// ─────────────────────────────────────────────

/** Receiver for [KInputGroup] content, exposing the internal focus requester. */
class KInputGroupScope internal constructor(
    internal val focusRequester: FocusRequester
)

// ─────────────────────────────────────────────
//  KInputGroupAddon
// ─────────────────────────────────────────────

/**
 * An addon container placed before, after, above, or below the field.
 *
 * For [KInputGroupAlign.InlineStart] / [KInputGroupAlign.InlineEnd] the addon
 * is rendered as a [Row] cell next to the input.
 *
 * For [KInputGroupAlign.BlockStart] / [KInputGroupAlign.BlockEnd] it spans the
 * full width above / below the input.
 *
 * @param align  Where to place this addon relative to the input control.
 */
@Composable
fun KInputGroupScope.KInputGroupAddon(
    align: KInputGroupAlign = KInputGroupAlign.InlineStart,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    when (align) {
        KInputGroupAlign.BlockStart -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 8.dp, bottom = 4.dp)
            ) { content() }
        }
        KInputGroupAlign.BlockEnd -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 4.dp, bottom = 8.dp)
            ) { content() }
        }
        else -> {
            // Inline addons are rendered directly by KInputGroupRow;
            // here we just emit the content with consistent padding.
            val padding = if (align == KInputGroupAlign.InlineStart)
                PaddingValues(start = 8.dp)
            else
                PaddingValues(end = 8.dp)

            Box(
                modifier         = modifier.padding(padding),
                contentAlignment = Alignment.Center
            ) {
                ProvideTextStyle(
                    LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        color    = cs.onSurfaceVariant
                    )
                ) { content() }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  KInputGroupInput
// ─────────────────────────────────────────────

/**
 * The text input control inside a [KInputGroup].
 *
 * Strips the outer border (the group draws its own) and fills available space.
 * Reuses [KInput] internally.
 *
 * ```kotlin
 * KInputGroup {
 *     KInputGroupInput(value = text, onValueChange = { text = it }, placeholder = "Type here…")
 * }
 * ```
 */
@Composable
fun KInputGroupScope.KInputGroupInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val cs = MaterialTheme.colorScheme

    OutlinedTextField(
        value             = value,
        onValueChange     = onValueChange,
        modifier          = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .defaultMinSize(minHeight = 32.dp),
        enabled           = enabled,
        isError           = isError,
        singleLine        = singleLine,
        maxLines          = maxLines,
        placeholder       = if (placeholder.isNotEmpty()) {
            { Text(placeholder, fontSize = 12.sp, color = cs.onSurface.copy(alpha = 0.5f)) }
        } else null,
        textStyle         = TextStyle(fontSize = 12.sp, color = cs.onBackground),
        visualTransformation = if (isPassword)
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        else
            androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions   = keyboardOptions,
        keyboardActions   = keyboardActions,
        shape             = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        colors            = OutlinedTextFieldDefaults.colors(
            focusedBorderColor         = Color.Transparent,
            unfocusedBorderColor       = Color.Transparent,
            disabledBorderColor        = Color.Transparent,
            errorBorderColor           = Color.Transparent,
            focusedContainerColor      = Color.Transparent,
            unfocusedContainerColor    = Color.Transparent,
            disabledContainerColor     = Color.Transparent,
            errorContainerColor        = Color.Transparent,
            focusedTextColor           = cs.onBackground,
            unfocusedTextColor         = cs.onBackground,
            disabledTextColor          = cs.onSurface.copy(alpha = 0.38f),
            cursorColor                = cs.primary
        )
    )
}

// ─────────────────────────────────────────────
//  KInputGroupTextarea
// ─────────────────────────────────────────────

/**
 * Multi-line textarea variant for [KInputGroup].
 *
 * Mirrors `InputGroupTextarea` — strips borders, auto-resizes.
 */
@Composable
fun KInputGroupScope.KInputGroupTextarea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    minLines: Int = 2,
    maxLines: Int = Int.MAX_VALUE
) {
    KInputGroupInput(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        placeholder   = placeholder,
        enabled       = enabled,
        singleLine    = false,
        maxLines      = maxLines
    )
}

// ─────────────────────────────────────────────
//  KInputGroupButton
// ─────────────────────────────────────────────

/**
 * A ghost-style icon button sized to sit flush inside a [KInputGroup] addon.
 *
 * Mirrors `InputGroupButton` — uses [KButton] with [KButtonVariant.Ghost].
 *
 * ```kotlin
 * KInputGroupAddon(align = KInputGroupAlign.InlineEnd) {
 *     KInputGroupButton(onClick = { clear() }) {
 *         Icon(Icons.Default.Clear, contentDescription = "Clear")
 *     }
 * }
 * ```
 */
@Composable
fun KInputGroupButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    KButton(
        onClick  = onClick,
        modifier = modifier.size(24.dp),
        variant  = KButtonVariant.Ghost,
        size     = KButtonSize.Icon,
        enabled  = enabled,
        content  = content
    )
}

// ─────────────────────────────────────────────
//  KInputGroupText
// ─────────────────────────────────────────────

/**
 * Plain muted text shown inside a [KInputGroup] addon.
 *
 * ```kotlin
 * KInputGroupAddon(align = KInputGroupAlign.InlineStart) {
 *     KInputGroupText("https://")
 * }
 * ```
 */
@Composable
fun KInputGroupText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        fontSize = 12.sp,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}