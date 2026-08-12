package dev.kindling.core.components.ui.inputGroup

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.kindlingClipNone
import dev.kindling.core.theme.kindlingShadowNone

/**
 * The text input control inside an [InputGroup].
 *
 * Strips outer borders (the group draws its own).
 * Uses [dev.kindling.core.components.ui.KInput] internally.
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
        value              = value,
        onValueChange      = onValueChange,
        modifier           = modifier
            .fillMaxWidth()
            .kindlingShadowNone()
            .kindlingClipNone()
            .focusRequester(focusRequester)
            .defaultMinSize(minHeight = 32.dp),
        enabled            = enabled,
        isError            = isError,
        singleLine         = singleLine,
        maxLines           = maxLines,
        placeholder        = if (placeholder.isNotEmpty()) {
            { Text(placeholder, style = TextStyle(fontSize = 14.sp, color = cs.onSurface.copy(.5f))) }
        } else null,
        textStyle          = TextStyle(fontSize = 14.sp, color = cs.onBackground),
        visualTransformation = if (isPassword)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
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

/**
 * Multi-line textarea variant for an [InputGroup].
 * Delegates to [dev.kindling.core.components.ui.Textarea] with borders suppressed.
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
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .defaultMinSize(minHeight = 64.dp)
            .kindlingClipNone(),
        enabled       = enabled,
        singleLine    = false,
        minLines      = minLines,
        maxLines      = maxLines,
        placeholder   = if (placeholder.isNotEmpty()) {
            { Text(placeholder, style = TextStyle(fontSize = 14.sp, color = cs.onSurface.copy(.5f))) }
        } else null,
        textStyle     = TextStyle(fontSize = 14.sp, color = cs.onBackground, lineHeight = 20.sp),
        shape         = RoundedCornerShape(0.dp),
        colors        = OutlinedTextFieldDefaults.colors(
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