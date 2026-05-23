package dev.kindling.core.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

// ─────────────────────────────────────────────
//  Mask pattern keys
// ─────────────────────────────────────────────

/**
 * Named mask patterns — mirrors `MaskPatternKey` from `mask-input.tsx`.
 */
enum class KMaskPattern(
    /** The `#`-placeholder pattern string. */
    val pattern: String,
    /** Optional display hint shown as placeholder. */
    val placeholder: String = "",
    /** Keyboard type suited for this mask. */
    val keyboardType: KeyboardType = KeyboardType.Number
) {
    Phone("(###) ###-####",           "(555) 000-0000"),
    Ssn("###-##-####",                "000-00-0000"),
    Date("##/##/####",                "MM/DD/YYYY"),
    Time("##:##",                     "HH:MM"),
    CreditCard("#### #### #### ####", "0000 0000 0000 0000"),
    CreditCardExpiry("##/##",         "MM/YY"),
    ZipCode("#####",                  "00000"),
    ZipCodeExtended("#####-####",     "00000-0000"),
    Ein("##-#######",                 "00-0000000"),
    Isbn("###-#-###-#####-#",         "000-0-000-00000-0"),
    LicensePlate("###-###",           "AAA-000", KeyboardType.Text),
    MacAddress("##:##:##:##:##:##",   "00:00:00:00:00:00", KeyboardType.Text);
}

// ─────────────────────────────────────────────
//  Mask transform helpers
// ─────────────────────────────────────────────

/**
 * Strips non-digit (or non-alphanumeric for text-pattern masks) characters
 * from [raw], then applies the `#`-based [pattern].
 *
 * Returns the formatted display string.
 */
fun applyKMask(raw: String, pattern: String, allowLetters: Boolean = false): String {
    val clean = if (allowLetters)
        raw.filter { it.isLetterOrDigit() }.uppercase()
    else
        raw.filter { it.isDigit() }

    val result = StringBuilder()
    var cleanIdx = 0

    for (ch in pattern) {
        if (cleanIdx >= clean.length) break
        if (ch == '#') {
            result.append(clean[cleanIdx++])
        } else {
            result.append(ch)
        }
    }
    return result.toString()
}

/**
 * Returns the raw (un-masked) digits / characters from a masked [value].
 */
fun stripKMask(value: String, allowLetters: Boolean = false): String =
    if (allowLetters) value.filter { it.isLetterOrDigit() }
    else              value.filter { it.isDigit() }

// ─────────────────────────────────────────────
//  KMaskInput
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style masked input field.
 *
 * Applies a `#`-placeholder [mask] pattern on every keystroke, keeping the
 * underlying value as raw digits/characters.  Mirrors the behaviour of
 * `mask-input.tsx` (MaskInput component).
 *
 * For currency and percentage use [KCurrencyInput] and [KPercentageInput]
 * helpers below — they handle the decimal formatting separately.
 *
 * ```kotlin
 * var phone by remember { mutableStateOf("") }
 *
 * KMaskInput(
 *     value         = phone,
 *     onValueChange = { phone = it },        // raw digits
 *     mask          = KMaskPattern.Phone,
 *     placeholder   = "(555) 000-0000"
 * )
 *
 * // Custom pattern
 * KMaskInput(
 *     value         = value,
 *     onValueChange = { value = it },
 *     customPattern = "##-##-####"
 * )
 * ```
 *
 * @param value           Raw (unmasked) value — digits for numeric masks.
 * @param onValueChange   Called with the new raw value on each keystroke.
 * @param mask            Named mask preset; ignored when [customPattern] is set.
 * @param customPattern   Custom `#`-placeholder pattern (overrides [mask]).
 * @param allowLetters    Set `true` for alphanumeric masks (e.g. license plate).
 * @param placeholder     Hint text shown when empty.
 * @param enabled         Whether the field is editable.
 * @param isError         Applies error styling.
 * @param modifier        Applied to the underlying [KInput].
 */
@Composable
fun KMaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    mask: KMaskPattern? = null,
    customPattern: String? = null,
    allowLetters: Boolean = mask?.keyboardType == KeyboardType.Text,
    placeholder: String = mask?.placeholder ?: customPattern?.replace('#', '0') ?: "",
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val pattern = customPattern ?: mask?.pattern ?: ""

    // Strip non-digit/non-alpha characters coming in from external state
    val cleanValue = remember(value) { stripKMask(value, allowLetters) }

    // The visible, masked display value
    val displayValue = remember(cleanValue, pattern) {
        if (pattern.isEmpty()) cleanValue
        else applyKMask(cleanValue, pattern, allowLetters)
    }

    KInput(
        value         = displayValue,
        onValueChange = { typed ->
            // Extract raw chars from whatever the user typed (handles
            // paste / autocomplete as well as single-key edits).
            val raw = stripKMask(typed, allowLetters)
            onValueChange(raw)
        },
        modifier      = modifier,
        placeholder   = placeholder,
        enabled       = enabled,
        isError       = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = mask?.keyboardType ?: if (allowLetters) KeyboardType.Text else KeyboardType.Number,
            imeAction    = ImeAction.Done
        ),
        keyboardActions = keyboardActions
    )
}

// ─────────────────────────────────────────────
//  Convenience wrappers for common masks
// ─────────────────────────────────────────────

/** Phone number input — formats as `(###) ###-####`. */
@Composable
fun KPhoneInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "(555) 000-0000",
    enabled: Boolean = true,
    isError: Boolean = false
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Phone, placeholder = placeholder, enabled = enabled, isError = isError)

/** US SSN input — formats as `###-##-####`. */
@Composable
fun KSsnInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Ssn, enabled = enabled, isError = isError)

/** Date input — formats as `MM/DD/YYYY`. */
@Composable
fun KDateMaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Date, enabled = enabled, isError = isError)

/** Credit-card input — formats as `#### #### #### ####`. */
@Composable
fun KCreditCardInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.CreditCard, enabled = enabled, isError = isError)

/** EIN input — formats as `##-#######`. */
@Composable
fun KEinInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) = KMaskInput(value, onValueChange, modifier, KMaskPattern.Ein, enabled = enabled, isError = isError)

// ─────────────────────────────────────────────
//  Currency / percentage helpers
// ─────────────────────────────────────────────

/**
 * Currency input field.
 *
 * Formats the raw numeric string with thousand separators and two decimal
 * places using [java.text.NumberFormat] for the given [locale].
 *
 * The [onValueChange] callback receives the raw numeric string (digits + optional decimal point).
 */
@Composable
fun KCurrencyInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$",
    locale: java.util.Locale = java.util.Locale.getDefault(),
    enabled: Boolean = true,
    isError: Boolean = false,
    placeholder: String = "${currencySymbol}0.00"
) {
    val display = remember(value, locale) {
        formatCurrency(value, currencySymbol, locale)
    }

    KInput(
        value         = display,
        onValueChange = { typed ->
            val raw = typed.filter { it.isDigit() || it == '.' }
            onValueChange(raw)
        },
        modifier      = modifier,
        placeholder   = placeholder,
        enabled       = enabled,
        isError       = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

private fun formatCurrency(raw: String, symbol: String, locale: java.util.Locale): String {
    if (raw.isEmpty()) return ""
    val num = raw.toDoubleOrNull() ?: return raw
    return try {
        val fmt = java.text.NumberFormat.getCurrencyInstance(locale)
        fmt.currency = java.util.Currency.getInstance(locale)
        fmt.format(num)
    } catch (_: Exception) {
        "$symbol$raw"
    }
}

/**
 * Percentage input — appends `%` to the display value.
 */
@Composable
fun KPercentageInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val display = if (value.isEmpty()) "" else "$value%"
    KInput(
        value         = display,
        onValueChange = { typed ->
            val raw = typed.filter { it.isDigit() || it == '.' }.trimEnd('%')
            onValueChange(raw)
        },
        modifier      = modifier,
        placeholder   = "0.00%",
        enabled       = enabled,
        isError       = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}