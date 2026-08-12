package dev.kindling.core.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PhoneInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Phone, enabled = enabled, isError = isError, onValidationChange = onValidationChange)

@Composable
fun SsnInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Ssn, enabled = enabled, isError = isError, onValidationChange = onValidationChange)

@Composable
fun DateMaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Date, enabled = enabled, isError = isError, onValidationChange = onValidationChange)

@Composable
fun CreditCardInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.CreditCard, enabled = enabled, isError = isError, onValidationChange = onValidationChange)

@Composable
fun EinInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Ein, enabled = enabled, isError = isError, onValidationChange = onValidationChange)

@Composable
fun EmailInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Email, enabled = enabled, isError = isError, onValidationChange = onValidationChange)

@Composable
fun UriInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Uri, enabled = enabled, isError = isError, onValidationChange = onValidationChange)

@Composable
fun NumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Number, enabled = enabled, isError = isError, onValidationChange = onValidationChange)

@Composable
fun DecimalInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValidationChange: ((Boolean) -> Unit)? = null
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Decimal, enabled = enabled, isError = isError, onValidationChange = onValidationChange)