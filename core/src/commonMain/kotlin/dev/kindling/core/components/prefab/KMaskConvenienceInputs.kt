package dev.kindling.core.components.ui.prefab

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.kindling.core.components.ui.maskInput.KMaskInput
import dev.kindling.core.components.ui.maskInput.KMaskPattern

/**
 * A pre-configured [KMaskInput] for phone numbers.
 *
 * Uses the default phone mask: `(###) ###-####`.
 */
@Composable
fun PhoneInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(
    value,
    onValueChange,
    modifier,
    KMaskPattern.Phone,
    enabled = enabled,
    isError = isError,
    onValidationChange = onValidationChange
)

/**
 * A pre-configured [KMaskInput] for Social Security Numbers (SSN).
 *
 * Uses the mask: `###-##-####`.
 */
@Composable
fun SsnInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(
    value,
    onValueChange,
    modifier,
    KMaskPattern.Ssn,
    enabled = enabled,
    isError = isError,
    onValidationChange = onValidationChange
)

@Composable
fun DateMaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(
    value,
    onValueChange,
    modifier,
    KMaskPattern.Date,
    enabled = enabled,
    isError = isError,
    onValidationChange = onValidationChange
)

/**
 * A pre-configured [KMaskInput] for credit card numbers.
 *
 * Uses a standard 16-digit mask: `#### #### #### ####`.
 */
@Composable
fun CreditCardInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(
    value,
    onValueChange,
    modifier,
    KMaskPattern.CreditCard,
    enabled = enabled,
    isError = isError,
    onValidationChange = onValidationChange
)

@Composable
fun EinInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(
    value,
    onValueChange,
    modifier,
    KMaskPattern.Ein,
    enabled = enabled,
    isError = isError,
    onValidationChange = onValidationChange
)

/**
 * A pre-configured [KMaskInput] for email addresses.
 * 
 * Note: While this uses [KMaskPattern.Email], email "masking" is typically
 * just validation rather than a rigid character-by-character mask.
 */
@Composable
fun EmailInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(
    value,
    onValueChange,
    modifier,
    KMaskPattern.Email,
    enabled = enabled,
    isError = isError,
    onValidationChange = onValidationChange
)

@Composable
fun UriInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(
    value,
    onValueChange,
    modifier,
    KMaskPattern.Uri,
    enabled = enabled,
    isError = isError,
    onValidationChange = onValidationChange
)

@Composable
fun NumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(
    value,
    onValueChange,
    modifier,
    KMaskPattern.Number,
    enabled = enabled,
    isError = isError,
    onValidationChange = onValidationChange
)

@Composable
fun DecimalInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(
    value,
    onValueChange,
    modifier,
    KMaskPattern.Decimal,
    enabled = enabled,
    isError = isError,
    onValidationChange = onValidationChange
)