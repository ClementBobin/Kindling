package dev.kindling.core.components.ui.prefab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.KLabel
import dev.kindling.core.components.ui.maskInput.KMaskPattern
import dev.kindling.core.components.ui.maskInput.KMaskInput

@Composable
fun KFieldSimple(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    mask: KMaskPattern? = null,
    customPattern: String? = null,
    allowLetters: Boolean = mask?.keyboardType == KeyboardType.Text && mask.withoutMask.not(),
    withoutMask: Boolean = mask?.withoutMask ?: false,
    placeholder: String = mask?.placeholder ?: customPattern?.replace('#', '0') ?: "",
    enabled: Boolean = true,
    autoValidate: Boolean = true,
    customValidate: ((String) -> Boolean)? = null,   // ← new
    onValidationChange: ((Boolean) -> Unit)? = null,
    isError: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        KLabel(label)
        if (trailingContent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                KMaskInput(
                    value              = value,
                    onValueChange      = onValueChange,
                    mask               = mask,
                    customPattern      = customPattern,
                    allowLetters       = allowLetters,
                    withoutMask        = withoutMask,
                    placeholder        = placeholder,
                    enabled            = enabled,
                    autoValidate       = autoValidate,
                    customValidate     = customValidate,
                    onValidationChange = onValidationChange,
                    isError            = isError,
                    modifier           = Modifier.weight(1f)
                )
                trailingContent()
            }
        } else {
            KMaskInput(
                value              = value,
                onValueChange      = onValueChange,
                mask               = mask,
                customPattern      = customPattern,
                allowLetters       = allowLetters,
                withoutMask        = withoutMask,
                placeholder        = placeholder,
                enabled            = enabled,
                autoValidate       = autoValidate,
                customValidate     = customValidate,
                onValidationChange = onValidationChange,
                isError            = isError,
                modifier           = Modifier.fillMaxWidth()
            )
        }
    }
}