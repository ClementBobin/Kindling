package dev.kindling.core.components.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes

/**
 * Shadcn/ui-style Input — mirrors `input.tsx`.
 *
 * A standardized text input component that follows the Kindling design system.
 * It provides built-in support for placeholders, error states, disabled states,
 * and password masking.
 *
 * Respects [androidx.compose.ui.platform.LocalLayoutDirection] via Compose RTL support.
 *
 * ### Example usage:
 * ```kotlin
 * var email by remember { mutableStateOf("") }
 * 
 * KInput(
 *     value = email,
 *     onValueChange = { email = it },
 *     placeholder = "name@example.com",
 *     leadingIcon = { Icon(Icons.Default.Email, null) }
 * )
 * ```
 *
 * @param value The current text value to display.
 * @param onValueChange Callback invoked when the text value changes.
 * @param modifier The modifier to be applied to the input layout.
 * @param placeholder The placeholder text to display when the input is empty.
 * @param enabled Whether the input is enabled for interaction.
 * @param isPassword Whether the input should mask the text (e.g., for passwords).
 * @param isError Whether the input should display an error state.
 * @param singleLine Whether the input should be restricted to a single line.
 * @param maxLines The maximum number of lines allowed.
 * @param minLines The minimum number of lines allowed.
 * @param leadingIcon Optional leading composable slot.
 * @param trailingIcon Optional trailing composable slot.
 * @param keyboardOptions Software keyboard options.
 * @param keyboardActions Software keyboard actions.
 * @param interactionSource The [MutableInteractionSource] to represent the stream of interactions.
 */
@Composable
fun KInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    isPassword: Boolean = false,
    isError: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val cs = MaterialTheme.colorScheme
    val shape = LocalKindlingShapes.current.radiusLg

    OutlinedTextField(
        value                = value,
        onValueChange        = onValueChange,
        modifier             = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 32.dp)
            .clip(shape),
        enabled              = enabled,
        isError              = isError,
        singleLine           = singleLine,
        maxLines             = maxLines,
        minLines             = minLines,
        placeholder          = if (placeholder.isNotEmpty()) {
            { Text(placeholder, style = TextStyle(fontSize = 14.sp, color = cs.onSurface.copy(.5f))) }
        } else null,
        leadingIcon          = leadingIcon,
        trailingIcon         = trailingIcon,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions      = keyboardOptions,
        keyboardActions      = keyboardActions,
        interactionSource    = interactionSource,
        textStyle            = TextStyle(
            fontSize   = 14.sp,
            fontWeight = FontWeight.Normal,
            color      = if (enabled) cs.onBackground else cs.onSurface.copy(.38f)
        ),
        shape  = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor         = cs.primary,
            unfocusedBorderColor       = cs.outline,
            disabledBorderColor        = cs.outline.copy(.38f),
            errorBorderColor           = cs.error,
            focusedContainerColor      = Color.Transparent,
            unfocusedContainerColor    = Color.Transparent,
            disabledContainerColor     = cs.onSurface.copy(.04f),
            errorContainerColor        = Color.Transparent,
            focusedTextColor           = cs.onBackground,
            unfocusedTextColor         = cs.onBackground,
            disabledTextColor          = cs.onSurface.copy(.38f),
            errorTextColor             = cs.error,
            cursorColor                = cs.primary,
            errorCursorColor           = cs.error,
            focusedLeadingIconColor    = cs.onSurfaceVariant,
            unfocusedLeadingIconColor  = cs.onSurfaceVariant,
            focusedTrailingIconColor   = cs.onSurfaceVariant,
            unfocusedTrailingIconColor = cs.onSurfaceVariant
        )
    )
}