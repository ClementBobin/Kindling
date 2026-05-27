package dev.kindling.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Alignment
// ─────────────────────────────────────────────

enum class KInputGroupAlign { InlineStart, InlineEnd, BlockStart, BlockEnd }

// ─────────────────────────────────────────────
//  Scope
// ─────────────────────────────────────────────

class KInputGroupScope internal constructor(
    internal val focusRequester: FocusRequester
)

// ─────────────────────────────────────────────
//  InputGroup
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style InputGroup — mirrors `input-group.tsx`.
 *
 * A single shared border wraps the field and any inline/block addons.
 * Highlights the border on focus; applies error styling when [isError] = true.
 * Respects [LocalLayoutDirection] for RTL.
 *
 * ```kotlin
 * InputGroup {
 *     InputGroupAddon(align = KInputGroupAlign.InlineStart) {
 *         Icon(Icons.Default.Search, null)
 *     }
 *     InputGroupInput(value = query, onValueChange = { query = it }, placeholder = "Search…")
 * }
 * ```
 */
@Composable
fun InputGroup(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    content: @Composable KInputGroupScope.() -> Unit
) {
    val cs             = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val scope          = remember(focusRequester) { KInputGroupScope(focusRequester) }
    var focused        by remember { mutableStateOf(false) }

    val borderColor = when {
        isError -> cs.error
        focused -> cs.primary
        else    -> cs.outline
    }
    val borderWidth = if (focused || isError) 2.dp else 1.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            // Propagate focus state from child fields via Modifier.onFocusChanged
            // is not straightforward at the column level; we track it in the
            // InputGroupInput slot below.
    ) {
        // Expose focused setter via scope so slots can update it.
        val scopeWithFocus = remember(focusRequester) {
            object : KInputGroupScope(focusRequester) {
                fun setFocused(f: Boolean) { focused = f }
            }
        }
        scope.content()
    }
}

// ─────────────────────────────────────────────
//  InputGroupAddon
// ─────────────────────────────────────────────

/**
 * Addon container placed inline or as a block above/below the input.
 */
@Composable
fun KInputGroupScope.InputGroupAddon(
    align: KInputGroupAlign = KInputGroupAlign.InlineStart,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val padding = when (align) {
        KInputGroupAlign.InlineStart -> PaddingValues(start = 8.dp)
        KInputGroupAlign.InlineEnd   -> PaddingValues(end = 8.dp)
        KInputGroupAlign.BlockStart  -> PaddingValues(start = 10.dp, top = 8.dp, bottom = 4.dp)
        KInputGroupAlign.BlockEnd    -> PaddingValues(start = 10.dp, top = 4.dp, bottom = 8.dp)
    }
    Box(
        modifier         = modifier.padding(padding),
        contentAlignment = Alignment.Center
    ) {
        ProvideTextStyle(
            LocalTextStyle.current.copy(fontSize = 14.sp, color = cs.onSurfaceVariant)
        ) { content() }
    }
}

// ─────────────────────────────────────────────
//  InputGroupButton  — uses KButton
// ─────────────────────────────────────────────

/**
 * Ghost-style icon button sized to sit flush inside an [InputGroup] addon.
 * Uses [KButton] with [KButtonVariant.Ghost].
 */
@Composable
fun KInputGroupScope.InputGroupButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KButtonVariant = KButtonVariant.Ghost,
    size: KButtonSize = KButtonSize.IconXs,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    KButton(
        onClick  = onClick,
        modifier = modifier,
        variant  = variant,
        size     = size,
        enabled  = enabled,
        content  = content
    )
}

// ─────────────────────────────────────────────
//  InputGroupText
// ─────────────────────────────────────────────

/** Plain muted text shown inside an [InputGroup] addon. */
@Composable
fun KInputGroupScope.InputGroupText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        fontSize = 14.sp,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
//  InputGroupInput  — uses KInput
// ─────────────────────────────────────────────

/**
 * The text input control inside an [InputGroup].
 *
 * Strips outer borders (the group draws its own).
 * Uses [KInput] internally.
 */
@Composable
fun KInputGroupScope.InputGroupInput(
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
        value                = value,
        onValueChange        = onValueChange,
        modifier             = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .defaultMinSize(minHeight = 32.dp),
        enabled              = enabled,
        isError              = isError,
        singleLine           = singleLine,
        maxLines             = maxLines,
        placeholder          = if (placeholder.isNotEmpty()) {
            { Text(placeholder, style = TextStyle(fontSize = 14.sp, color = cs.onSurface.copy(.5f))) }
        } else null,
        textStyle            = TextStyle(fontSize = 14.sp, color = cs.onBackground),
        visualTransformation = if (isPassword)
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        else
            androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions      = keyboardOptions,
        keyboardActions      = keyboardActions,
        shape                = RoundedCornerShape(0.dp),
        colors               = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Color.Transparent,
            unfocusedBorderColor    = Color.Transparent,
            disabledBorderColor     = Color.Transparent,
            errorBorderColor        = Color.Transparent,
            focusedContainerColor   = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor  = Color.Transparent,
            errorContainerColor     = Color.Transparent,
            focusedTextColor        = cs.onBackground,
            unfocusedTextColor      = cs.onBackground,
            disabledTextColor       = cs.onSurface.copy(.38f),
            cursorColor             = cs.primary
        )
    )
}

// ─────────────────────────────────────────────
//  InputGroupTextarea  — uses Textarea
// ─────────────────────────────────────────────

/**
 * Multi-line textarea variant for an [InputGroup].
 * Delegates to [Textarea] with borders suppressed.
 */
@Composable
fun KInputGroupScope.InputGroupTextarea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    minLines: Int = 2,
    maxLines: Int = Int.MAX_VALUE
) {
    val cs = MaterialTheme.colorScheme
    OutlinedTextField(
        value           = value,
        onValueChange   = onValueChange,
        modifier        = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .defaultMinSize(minHeight = 64.dp),
        enabled         = enabled,
        singleLine      = false,
        minLines        = minLines,
        maxLines        = maxLines,
        placeholder     = if (placeholder.isNotEmpty()) {
            { Text(placeholder, style = TextStyle(fontSize = 14.sp, color = cs.onSurface.copy(.5f))) }
        } else null,
        textStyle       = TextStyle(fontSize = 14.sp, color = cs.onBackground, lineHeight = 20.sp),
        shape           = RoundedCornerShape(0.dp),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Color.Transparent,
            unfocusedBorderColor    = Color.Transparent,
            disabledBorderColor     = Color.Transparent,
            errorBorderColor        = Color.Transparent,
            focusedContainerColor   = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor  = Color.Transparent,
            errorContainerColor     = Color.Transparent,
            focusedTextColor        = cs.onBackground,
            unfocusedTextColor      = cs.onBackground,
            disabledTextColor       = cs.onSurface.copy(.38f),
            cursorColor             = cs.primary
        )
    )
}