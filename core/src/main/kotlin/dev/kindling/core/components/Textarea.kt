package dev.kindling.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shadcn/ui-style Textarea.
 *
 * A multi-line text input that mirrors `textarea.tsx`.  The field
 * auto-grows with content (no fixed `minH`) up to [maxLines].
 *
 * Fully theme-agnostic — colours are resolved from [MaterialTheme.colorScheme].
 *
 * ```kotlin
 * var message by remember { mutableStateOf("") }
 *
 * KTextarea(
 *     value         = message,
 *     onValueChange = { message = it },
 *     placeholder   = "Type your message here."
 * )
 * ```
 *
 * @param value           Current text value.
 * @param onValueChange   Called on every keystroke.
 * @param modifier        Applied to the underlying [OutlinedTextField].
 * @param placeholder     Hint shown when [value] is empty.
 * @param enabled         Whether the field is editable.
 * @param isError         Applies error-border styling.
 * @param minLines        Minimum number of visible lines (default 2, matching `min-h-16`).
 * @param maxLines        Maximum lines before the field starts scrolling.
 * @param keyboardOptions Keyboard configuration.
 * @param keyboardActions Keyboard action handlers.
 */
@Composable
fun KTextarea(
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

    OutlinedTextField(
        value             = value,
        onValueChange     = onValueChange,
        modifier          = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp),   // mirrors min-h-16 (16 × 4 dp)
        enabled           = enabled,
        isError           = isError,
        singleLine        = false,
        minLines          = minLines,
        maxLines          = maxLines,
        placeholder       = if (placeholder.isNotEmpty()) {
            {
                Text(
                    text  = placeholder,
                    style = TextStyle(fontSize = 12.sp, color = cs.onSurface.copy(alpha = 0.5f))
                )
            }
        } else null,
        keyboardOptions   = keyboardOptions,
        keyboardActions   = keyboardActions,
        textStyle         = TextStyle(
            fontSize = 12.sp,
            color    = if (enabled) cs.onBackground else cs.onSurface.copy(alpha = 0.38f),
            lineHeight = 18.sp
        ),
        shape  = RoundedCornerShape(0.dp),   // matches rounded-none in the TSX
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor         = cs.primary,
            unfocusedBorderColor       = cs.outline,
            disabledBorderColor        = cs.outline.copy(alpha = 0.38f),
            errorBorderColor           = cs.error,
            focusedContainerColor      = Color.Transparent,
            unfocusedContainerColor    = Color.Transparent,
            disabledContainerColor     = cs.onSurface.copy(alpha = 0.04f),
            errorContainerColor        = Color.Transparent,
            focusedTextColor           = cs.onBackground,
            unfocusedTextColor         = cs.onBackground,
            disabledTextColor          = cs.onSurface.copy(alpha = 0.38f),
            errorTextColor             = cs.error,
            cursorColor                = cs.primary,
            errorCursorColor           = cs.error
        )
    )
}