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
 * Respects [androidx.compose.ui.platform.LocalLayoutDirection] for RTL text alignment automatically.
 *
 * ```kotlin
 * var message by remember { mutableStateOf("") }
 * Textarea(
 *     value         = message,
 *     onValueChange = { message = it },
 *     placeholder   = "Type your message here."
 * )
 * ```
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