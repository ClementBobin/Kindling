package dev.kindling.core.components.ui.maskInput

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import dev.kindling.core.components.ui.KInput
import dev.kindling.core.theme.LocalKindlingShapes
import dev.kindling.core.theme.kindlingShadowXs

/**
 * Shadcn/ui-style masked input field — mirrors `KMaskInput` from `mask-input.tsx`.
 *
 * Applies a `#`-placeholder [mask] or [customPattern] on every keystroke.
 * [onValueChange] receives the **masked** display value; use [getUnmaskedValue]
 * to strip formatting before storing.
 */
@Composable
fun KMaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    mask: KMaskPattern? = null,
    customPattern: String? = null,
    allowLetters: Boolean = mask?.keyboardType == KeyboardType.Text && mask.withoutMask.not(),
    withoutMask: Boolean = mask?.withoutMask ?: false,
    placeholder: String = mask?.placeholder ?: customPattern?.replace('#', '0') ?: "",
    enabled: Boolean = true,
    isError: Boolean = false,
    autoValidate: Boolean = true,
    customValidate: ((String) -> Boolean)? = null,
    onValidationChange: ((Boolean) -> Unit)? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val pattern = customPattern ?: mask?.pattern ?: ""

    var touched by remember { mutableStateOf(false) }

    val validator = customValidate ?: mask?.validate
    val isValid = validator?.invoke(value) != false
    val autoError = autoValidate && touched && value.isNotEmpty() && !isValid

    val interactionSource = remember { MutableInteractionSource() }
    val shape = LocalKindlingShapes.current.radiusMd

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is FocusInteraction.Unfocus) touched = true
        }
    }

    LaunchedEffect(value, touched) {
        if (validator != null && (touched || value.isNotEmpty())) {
            onValidationChange?.invoke(isValid)
        }
    }

    val displayValue = remember(value, pattern, withoutMask) {
        if (withoutMask || pattern.isEmpty()) value
        else applyMask(getUnmaskedValue(value, allowLetters), pattern, allowLetters)
    }

    KInput(
        value         = displayValue,
        onValueChange = { typed ->
            val raw    = if (withoutMask || pattern.isEmpty()) typed
            else getUnmaskedValue(typed, allowLetters)
            val masked = if (withoutMask || pattern.isEmpty()) raw
            else applyMask(raw, pattern, allowLetters)
            onValueChange(masked)
        },
        modifier          = modifier
            .kindlingShadowXs(shape)
            .clip(shape),
        placeholder       = placeholder,
        enabled           = enabled,
        isError           = isError || autoError,
        interactionSource = interactionSource,
        keyboardOptions   = KeyboardOptions(
            keyboardType = mask?.keyboardType
                ?: if (allowLetters) KeyboardType.Text else KeyboardType.Number,
            imeAction    = ImeAction.Done
        ),
        keyboardActions   = keyboardActions
    )
}