package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes

/**
 * Shadcn/ui-style Textarea — mirrors `textarea.tsx`.
 *
 * A multi-line text input component designed for longer entries like messages or descriptions.
 * It provides a consistent look and feel with other Kindling input components and handles
 * error states, disabled states, and placeholder text.
 *
 * Respects [androidx.compose.ui.platform.LocalLayoutDirection] for RTL text alignment automatically.
 *
 * ### Example usage:
 * ```kotlin
 * var message by remember { mutableStateOf("") }
 * Textarea(
 *     value         = message,
 *     onValueChange = { message = it },
 *     placeholder   = "Type your message here...",
 *     minLines      = 3
 * )
 * ```
 *
 * @param value The current text value to display in the textarea.
 * @param onValueChange Callback invoked when the text value changes.
 * @param modifier The modifier to be applied to the layout.
 * @param placeholder The placeholder text to display when the textarea is empty.
 * @param enabled Whether the textarea is enabled for user interaction.
 * @param isError Whether the textarea should display an error state.
 * @param minLines The minimum number of lines to display.
 * @param maxLines The maximum number of lines to display.
 * @param keyboardOptions Software keyboard options.
 * @param keyboardActions Software keyboard actions.
 */
@Composable
fun Textarea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    minLines: Int = 2,
    maxLines: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction    = ImeAction.Default
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val cs = MaterialTheme.colorScheme
    val shape = LocalKindlingShapes.current.radiusLg

    OutlinedTextField(
        value           = value,
        onValueChange   = onValueChange,
        modifier        = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .clip(shape),
        enabled         = enabled,
        isError         = isError,
        singleLine      = false,
        minLines        = minLines,
        maxLines        = maxLines,
        placeholder     = if (placeholder.isNotEmpty()) {
            { Text(placeholder, style = TextStyle(fontSize = 14.sp, color = cs.onSurface.copy(.5f))) }
        } else null,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle       = TextStyle(
            fontSize   = 14.sp,
            color      = if (enabled) cs.onBackground else cs.onSurface.copy(.38f),
            lineHeight = 20.sp
        ),
        shape  = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = cs.primary,
            unfocusedBorderColor    = cs.outline,
            disabledBorderColor     = cs.outline.copy(.38f),
            errorBorderColor        = cs.error,
            focusedContainerColor   = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor  = cs.onSurface.copy(.04f),
            errorContainerColor     = Color.Transparent,
            focusedTextColor        = cs.onBackground,
            unfocusedTextColor      = cs.onBackground,
            disabledTextColor       = cs.onSurface.copy(.38f),
            errorTextColor          = cs.error,
            cursorColor             = cs.primary,
            errorCursorColor        = cs.error
        )
    )
}