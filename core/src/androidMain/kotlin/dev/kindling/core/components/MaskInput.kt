package dev.kindling.core.components

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

// ─────────────────────────────────────────────
//  Public mask helpers  (mirror mask-input.tsx exports)
// ─────────────────────────────────────────────

/**
 * Applies a `#`-placeholder [pattern] to [value].
 *
 * Mirrors `applyMask` from `mask-input.tsx`.
 */
fun applyMask(value: String, pattern: String, allowLetters: Boolean = false): String {
    val clean = if (allowLetters)
        value.filter { it.isLetterOrDigit() }.uppercase()
    else
        value.filter { it.isDigit() }

    val sb = StringBuilder()
    var ci = 0
    for (ch in pattern) {
        if (ci >= clean.length) break
        if (ch == '#') sb.append(clean[ci++]) else sb.append(ch)
    }
    return sb.toString()
}

/**
 * Mirrors `applyCurrencyMask` from `mask-input.tsx`.
 *
 * Formats a raw numeric string as a locale-aware currency display value.
 */
fun applyCurrencyMask(
    value: String,
    currencyCode: String = "USD",
    locale: java.util.Locale = java.util.Locale.getDefault()
): String {
    if (value.isEmpty()) return ""
    val num = value.toDoubleOrNull() ?: return value
    return try {
        val fmt = java.text.NumberFormat.getCurrencyInstance(locale).apply {
            currency = java.util.Currency.getInstance(currencyCode)
        }
        fmt.format(num)
    } catch (_: Exception) {
        value
    }
}

/**
 * Mirrors `applyPercentageMask` from `mask-input.tsx`.
 */
fun applyPercentageMask(value: String): String {
    if (value.isEmpty()) return ""
    val clean = value.filter { it.isDigit() || it == '.' }
    val parts = clean.split(".")
    val integer = parts[0]
    val decimal = parts.getOrNull(1)?.take(2) ?: ""
    return if (decimal.isNotEmpty()) "$integer.$decimal%" else "$integer%"
}

/**
 * Strips all non-digit (or non-alphanumeric) chars from [value].
 *
 * Mirrors `getUnmaskedValue` from `mask-input.tsx`.
 */
fun getUnmaskedValue(value: String, allowLetters: Boolean = false): String =
    if (allowLetters) value.filter { it.isLetterOrDigit() }
    else              value.filter { it.isDigit() }

/**
 * Converts a caret position in the masked string to an index in the raw string.
 *
 * Mirrors `toUnmaskedIndex` from `mask-input.tsx`.
 */
fun toUnmaskedIndex(masked: String, pattern: String, caret: Int): Int {
    var idx = 0
    for (i in 0 until minOf(caret, masked.length, pattern.length)) {
        if (pattern[i] == '#') idx++
    }
    return idx
}

/**
 * Converts a raw-string index back to a caret position in the masked string.
 *
 * Mirrors `fromUnmaskedIndex` from `mask-input.tsx`.
 */
fun fromUnmaskedIndex(masked: String, pattern: String, unmaskedIndex: Int): Int {
    var seen = 0
    for (i in masked.indices.take(pattern.length)) {
        if (pattern[i] == '#') {
            seen++
            if (seen == unmaskedIndex) return i + 1
        }
    }
    return masked.length
}

// ─────────────────────────────────────────────
//  Named mask patterns
// ─────────────────────────────────────────────

enum class KMaskPattern(
    val pattern: String,
    val placeholder: String = "",
    val keyboardType: KeyboardType = KeyboardType.Number,
    val withoutMask: Boolean = false,
    val validate: ((String) -> Boolean)? = null
) {
    Phone("(###) ###-####",           "(555) 000-0000",    validate = { getUnmaskedValue(it).length == 10 }),
    Ssn("###-##-####",                "000-00-0000",       validate = { getUnmaskedValue(it).length == 9 }),
    Date("##/##/####",                "MM/DD/YYYY",        validate = { getUnmaskedValue(it).length == 8 }),
    Time("##:##",                     "HH:MM",             validate = { getUnmaskedValue(it).length == 4 }),
    CreditCard("#### #### #### ####", "0000 0000 0000 0000", validate = { getUnmaskedValue(it).length == 16 }),
    CreditCardExpiry("##/##",         "MM/YY",             validate = { getUnmaskedValue(it).length == 4 }),
    ZipCode("#####",                  "00000",             validate = { getUnmaskedValue(it).length == 5 }),
    ZipCodeExtended("#####-####",     "00000-0000",        validate = { getUnmaskedValue(it).length == 9 }),
    Ein("##-#######",                 "00-0000000",        validate = { getUnmaskedValue(it).length == 9 }),
    Isbn("###-#-###-#####-#",         "000-0-000-00000-0", validate = { getUnmaskedValue(it).length == 13 }),
    LicensePlate("###-###",           "AAA-000", KeyboardType.Text, validate = { getUnmaskedValue(it, allowLetters = true).length == 6 }),
    MacAddress("##:##:##:##:##:##",   "00:00:00:00:00:00", KeyboardType.Text, validate = { getUnmaskedValue(it, allowLetters = true).length == 12 }),
    Email("", "name@example.com",     KeyboardType.Email,   withoutMask = true, validate = { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }),
    Uri("", "https://",               KeyboardType.Uri,     withoutMask = true, validate = { android.util.Patterns.WEB_URL.matcher(it).matches() }),
    Number("", "",                    KeyboardType.Number,  withoutMask = true, validate = { it.toDoubleOrNull() != null }),
    Decimal("", "0.00",               KeyboardType.Decimal, withoutMask = true, validate = { it.toDoubleOrNull() != null }),
}
// ─────────────────────────────────────────────
//  MaskInput
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style masked input field — mirrors `MaskInput` from `mask-input.tsx`.
 *
 * Applies a `#`-placeholder [mask] or [customPattern] on every keystroke.
 * [onValueChange] receives the **masked** display value; use [getUnmaskedValue]
 * to strip formatting before storing.
 *
 * ```kotlin
 * var phone by remember { mutableStateOf("") }
 * MaskInput(
 *     value         = phone,
 *     onValueChange = { phone = it },
 *     mask          = KMaskPattern.Phone
 * )
 *
 * // Custom pattern
 * MaskInput(
 *     value         = value,
 *     onValueChange = { value = it },
 *     customPattern = "##-##-####"
 * )
 * ```
 *
 * @param value           Current value (masked or raw — will be re-masked on first render).
 * @param onValueChange   Called with the new masked display value on each keystroke.
 * @param mask            Named mask preset; ignored when [customPattern] is set.
 * @param customPattern   Custom `#`-placeholder pattern (overrides [mask]).
 * @param allowLetters    `true` for alphanumeric masks (e.g. license plate, MAC address).
 * @param withoutMask     `true` disables masking entirely (plain text field).
 * @param placeholder     Hint text shown when empty. Defaults to mask placeholder.
 * @param enabled         Whether the field is editable.
 * @param isError         Applies error styling.
 * @param modifier        Applied to the underlying [KInput].
 */
@Composable
fun MaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    mask: KMaskPattern? = null,
    customPattern: String? = null,
    allowLetters: Boolean = mask?.keyboardType == KeyboardType.Text && mask.withoutMask.not(),
    withoutMask: Boolean = mask?.withoutMask ?: false,
    placeholder: String = mask?.placeholder
        ?: customPattern?.replace('#', '0')
        ?: "",
    enabled: Boolean = true,
    isError: Boolean = false,
    autoValidate: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val pattern = customPattern ?: mask?.pattern ?: ""

    var touched by remember { mutableStateOf(false) }
    val autoError = autoValidate && touched && value.isNotEmpty() && mask?.validate?.invoke(value) == false
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is FocusInteraction.Unfocus) touched = true
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
        modifier          = modifier,
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

// ─────────────────────────────────────────────
//  Convenience wrappers
// ─────────────────────────────────────────────

@Composable fun PhoneInput(value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) =
    MaskInput(value, onValueChange, modifier, KMaskPattern.Phone, enabled = enabled, isError = isError)

@Composable fun SsnInput(value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) =
    MaskInput(value, onValueChange, modifier, KMaskPattern.Ssn, enabled = enabled, isError = isError)

@Composable fun DateMaskInput(value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) =
    MaskInput(value, onValueChange, modifier, KMaskPattern.Date, enabled = enabled, isError = isError)

@Composable fun CreditCardInput(value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) =
    MaskInput(value, onValueChange, modifier, KMaskPattern.CreditCard, enabled = enabled, isError = isError)

@Composable fun EinInput(value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) =
    MaskInput(value, onValueChange, modifier, KMaskPattern.Ein, enabled = enabled, isError = isError)

@Composable fun EmailInput(value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) =
    MaskInput(value, onValueChange, modifier, KMaskPattern.Email, enabled = enabled, isError = isError)

@Composable fun UriInput(value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) =
    MaskInput(value, onValueChange, modifier, KMaskPattern.Uri, enabled = enabled, isError = isError)

@Composable fun NumberInput(value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) =
    MaskInput(value, onValueChange, modifier, KMaskPattern.Number, enabled = enabled, isError = isError)

@Composable fun DecimalInput(value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) =
    MaskInput(value, onValueChange, modifier, KMaskPattern.Decimal, enabled = enabled, isError = isError)