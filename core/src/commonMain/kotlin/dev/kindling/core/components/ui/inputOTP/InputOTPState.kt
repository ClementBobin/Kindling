package dev.kindling.core.components.ui.inputOTP

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder for the OTP input — created by [rememberInputOTPState].
 *
 * Matches the slot-based API of the web `InputOTP` component backed by
 * the `input-otp` library.
 */
@Stable
class InputOTPState internal constructor(
    initialValue: String,
    val length: Int,
    val onValueChange: (String) -> Unit
) {
    var value   by mutableStateOf(initialValue.filter { it.isDigit() }.take(length))
    var focused by mutableStateOf(false)

    val slots: List<InputOTPSlotState> get() = List(length) { i ->
        InputOTPSlotState(
            char         = value.getOrNull(i),
            isActive     = focused && i == value.length.coerceAtMost(length - 1),
            hasFakeCaret = focused && i == value.length.coerceAtMost(length - 1) && value.getOrNull(i) == null
        )
    }
}

@Stable
data class InputOTPSlotState(
    val char: Char?,
    val isActive: Boolean,
    val hasFakeCaret: Boolean
)

@Composable
fun rememberInputOTPState(
    value: String = "",
    length: Int = 6,
    onValueChange: (String) -> Unit = {}
): InputOTPState = remember(length) { InputOTPState(value, length, onValueChange) }
    .also { LaunchedEffect(value) { it.value = value.filter { c -> c.isDigit() }.take(length) } }