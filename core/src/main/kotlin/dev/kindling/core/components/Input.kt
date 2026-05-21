package dev.kindling.core.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.tooling.preview.Preview
import dev.kindling.core.components.internal.KindlingPreviewSurface
import dev.kindling.core.components.internal.PreviewLabel

/**
 * Render a shadcn/ui-style input field.
 *
 * Colours are resolved from [MaterialTheme.colorScheme], making the input automatically adapt
 * to light and dark themes.
 *
 * ```kotlin
 * var value by remember { mutableStateOf("") }
 * KInput(value = value, onValueChange = { value = it }, placeholder = "m@example.com")
 *
 * KInput(value = pwd, onValueChange = { pwd = it }, isPassword = true)
 * ```
 *
 * @param value Current text value.
 * @param onValueChange Callback invoked when the text changes.
 * @param modifier Applied to the outermost layout element.
 * @param placeholder Placeholder text shown when the input is empty.
 * @param enabled When `false`, the field is non-interactive and dimmed.
 * @param isPassword When `true`, masks the input using a password transformation.
 * @param isError When `true`, highlights the field as invalid.
 * @param singleLine When `true`, restricts input to a single line.
 * @param maxLines Maximum number of lines when `singleLine` is `false`.
 * @param minLines Minimum number of visible lines.
 * @param leadingIcon Optional leading icon slot.
 * @param trailingIcon Optional trailing icon slot.
 * @param keyboardOptions Keyboard configuration for the input.
 * @param keyboardActions IME action callbacks.
 * @param interactionSource Interaction source used for focus and pressed state.
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

@Preview(name = "KInput — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KInput — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKInput() {
    KindlingPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PreviewLabel("Default")
            KInput(value = "m@example.com", onValueChange = { }, placeholder = "m@example.com")

            PreviewLabel("Leading icon")
            KInput(
                value = "hello@kindling.dev",
                onValueChange = { },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) }
            )

            PreviewLabel("Password")
            KInput(value = "secret", onValueChange = { }, isPassword = true)

            PreviewLabel("Error")
            KInput(value = "bad value", onValueChange = { }, isError = true)

            PreviewLabel("Disabled")
            KInput(value = "disabled", onValueChange = { }, enabled = false)
        }
    }
}
