package dev.kindling.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.internal.KindlingPreviewSurface
import dev.kindling.core.components.internal.PreviewLabel

/**
 * Render a labeled form field with helper or error text.
 *
 * ```kotlin
 * KFormField(
 *     label = "Email",
 *     value = email,
 *     onValueChange = { email = it },
 *     placeholder = "m@example.com",
 *     helperText = "We'll never share your email.",
 *     isError = email.isNotEmpty() && !email.contains("@"),
 *     errorMessage = "Please enter a valid email address."
 * )
 * ```
 *
 * @param label Label shown above the input.
 * @param value Current input value.
 * @param onValueChange Callback invoked when the text changes.
 * @param modifier Applied to the outermost layout element.
 * @param placeholder Placeholder text shown when the input is empty.
 * @param helperText Supporting text shown below the input.
 * @param errorMessage Error text shown when [isError] is `true`.
 * @param isError When `true`, highlights the input and shows [errorMessage].
 * @param enabled When `false`, the field is non-interactive and dimmed.
 * @param isPassword When `true`, masks the input using a password transformation.
 * @param leadingIcon Optional leading icon slot.
 * @param trailingIcon Optional trailing icon slot.
 * @param keyboardOptions Keyboard configuration for the input.
 * @param keyboardActions IME action callbacks.
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
    val cs = MaterialTheme.colorScheme
    val showErr = isError && errorMessage != null

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        KLabel(text = label, disabled = !enabled)

        KInput(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            enabled = enabled,
            isError = isError,
            isPassword = isPassword,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
        )

        val helpMsg = when {
            showErr -> errorMessage
            helperText != null -> helperText
            else -> null
        }
        if (helpMsg != null) {
            Text(
                text = helpMsg,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (showErr) cs.error else cs.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "KFormField — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KFormField — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKFormField() {
    KindlingPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PreviewLabel("Helper text")
            KFormField(
                label = "Email",
                value = "hello@kindling.dev",
                onValueChange = { },
                helperText = "We'll never share your email."
            )

            PreviewLabel("Error state")
            KFormField(
                label = "Email",
                value = "invalid",
                onValueChange = { },
                isError = true,
                errorMessage = "Please enter a valid email address."
            )

            PreviewLabel("Disabled")
            KFormField(
                label = "Email",
                value = "disabled",
                onValueChange = { },
                enabled = false,
                helperText = "Field is disabled."
            )
        }
    }
}
