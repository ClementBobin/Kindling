package dev.kindling.core.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shadcn/ui-style Input field.
 *
 * Fully theme-agnostic — colours are resolved from [MaterialTheme.colorScheme].
 *
 * ```kotlin
 * var value by remember { mutableStateOf("") }
 * KInput(value = value, onValueChange = { value = it }, placeholder = "m@example.com")
 *
 * // Password
 * KInput(value = pwd, onValueChange = { pwd = it }, isPassword = true)
 * ```
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
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val cs = MaterialTheme.colorScheme

    OutlinedTextField(
        value             = value,
        onValueChange     = onValueChange,
        modifier          = modifier.fillMaxWidth().defaultMinSize(minHeight = 36.dp),
        enabled           = enabled,
        isError           = isError,
        singleLine        = singleLine,
        maxLines          = maxLines,
        minLines          = minLines,
        placeholder       = if (placeholder.isNotEmpty()) {
            {
                Text(
                    text  = placeholder,
                    style = TextStyle(fontSize = 14.sp, color = cs.onSurface.copy(alpha = 0.5f))
                )
            }
        } else null,
        leadingIcon         = leadingIcon,
        trailingIcon        = trailingIcon,
        visualTransformation = if (isPassword) PasswordVisualTransformation()
                               else VisualTransformation.None,
        keyboardOptions     = keyboardOptions,
        keyboardActions     = keyboardActions,
        interactionSource   = interactionSource,
        textStyle           = TextStyle(
            fontSize   = 14.sp,
            fontWeight = FontWeight.Normal,
            color      = if (enabled) cs.onBackground else cs.onSurface.copy(alpha = 0.38f)
        ),
        shape  = RoundedCornerShape(6.dp),
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
            errorCursorColor           = cs.error,
            focusedLeadingIconColor    = cs.onSurfaceVariant,
            unfocusedLeadingIconColor  = cs.onSurfaceVariant,
            disabledLeadingIconColor   = cs.onSurface.copy(alpha = 0.38f),
            focusedTrailingIconColor   = cs.onSurfaceVariant,
            unfocusedTrailingIconColor = cs.onSurfaceVariant,
            disabledTrailingIconColor  = cs.onSurface.copy(alpha = 0.38f),
        )
    )
}

/**
 * Convenience wrapper: [KLabel] + [KInput] + optional helper / error text.
 *
 * ```kotlin
 * KFormField(
 *     label        = "Email",
 *     value        = email,
 *     onValueChange = { email = it },
 *     placeholder  = "m@example.com",
 *     helperText   = "We'll never share your email.",
 *     isError      = email.isNotEmpty() && !email.contains("@"),
 *     errorMessage = "Please enter a valid email address."
 * )
 * ```
 */
@Composable
fun KFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    errorMessage: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val cs      = MaterialTheme.colorScheme
    val showErr = isError && errorMessage != null

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        KLabel(text = label, disabled = !enabled)

        KInput(
            value           = value,
            onValueChange   = onValueChange,
            placeholder     = placeholder,
            enabled         = enabled,
            isError         = isError,
            isPassword      = isPassword,
            leadingIcon     = leadingIcon,
            trailingIcon    = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
        )

        val helpMsg = when {
            showErr            -> errorMessage
            helperText != null -> helperText
            else               -> null
        }
        if (helpMsg != null) {
            Text(
                text  = helpMsg,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (showErr) cs.error else cs.onSurfaceVariant
            )
        }
    }
}
