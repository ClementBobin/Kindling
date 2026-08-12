package dev.kindling.core.components.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes

// ─────────────────────────────────────────────
//  InputOTP state
// ─────────────────────────────────────────────

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
            char        = value.getOrNull(i),
            isActive    = focused && i == value.length.coerceAtMost(length - 1),
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

// ─────────────────────────────────────────────
//  InputOTP (root)
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style OTP / PIN input root — mirrors `InputOTP` from `input-otp.tsx`.
 *
 * Place [InputOTPGroup]s (with [InputOTPSlot]s) and [InputOTPSeparator]s inside.
 *
 * ```kotlin
 * val otpState = rememberInputOTPState(value = pin, length = 6) { pin = it }
 *
 * InputOTP(state = otpState) {
 *     InputOTPGroup {
 *         repeat(3) { i -> InputOTPSlot(state = otpState, index = i) }
 *     }
 *     InputOTPSeparator()
 *     InputOTPGroup {
 *         repeat(3) { i -> InputOTPSlot(state = otpState, index = i + 3) }
 *     }
 * }
 * ```
 */
@Composable
fun InputOTP(
    state: InputOTPState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val shape = LocalKindlingShapes.current.radiusLg

    // Hidden backing text field
    BasicTextField(
        value         = state.value,
        onValueChange = { raw ->
            val digits = raw.filter { it.isDigit() }.take(state.length)
            state.value = digits
            state.onValueChange(digits)
        },
        enabled       = enabled,
        singleLine    = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction    = ImeAction.Done
        ),
        cursorBrush = SolidColor(Color.Transparent),
        textStyle   = TextStyle(color = Color.Transparent, fontSize = 1.sp),
        modifier    = Modifier
            .size(1.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { state.focused = it.isFocused }
            .clip(shape)
    )

    // Visual row
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Pass click-to-focus down via custom local
        CompositionLocalProvider(LocalOTPFocusRequester provides focusRequester) {
            content()
        }
    }
}

// Internal composition local so slots can request focus
private val LocalOTPFocusRequester = compositionLocalOf<FocusRequester?> { null }

// ─────────────────────────────────────────────
//  InputOTPGroup
// ─────────────────────────────────────────────

/**
 * Groups consecutive [InputOTPSlot]s into a visually joined block.
 * Mirrors `InputOTPGroup`.
 *
 * ```kotlin
 * InputOTPGroup {
 *     InputOTPSlot(state, 0)
 *     InputOTPSlot(state, 1)
 *     InputOTPSlot(state, 2)
 * }
 * ```
 */
@Composable
fun RowScope.InputOTPGroup(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        content               = content
    )
}

// ─────────────────────────────────────────────
//  InputOTPSlot
// ─────────────────────────────────────────────

/**
 * Individual digit slot — mirrors `InputOTPSlot`.
 *
 * The active slot shows a blinking caret when empty.
 * Borders are joined (rounded only on outermost edges of the group).
 *
 * @param state  The shared [InputOTPState].
 * @param index  Zero-based slot index (0 … length - 1).
 * @param isFirst Whether this is the leftmost slot in its group.
 * @param isLast  Whether this is the rightmost slot in its group.
 */
@Composable
fun RowScope.InputOTPSlot(
    state: InputOTPState,
    index: Int,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val cs           = MaterialTheme.colorScheme
    val slotState    = state.slots.getOrNull(index) ?: return
    val focusRequest = LocalOTPFocusRequester.current
    val rounded       = LocalKindlingShapes.current.roundedLg

    val borderColor = when {
        slotState.isActive -> cs.primary
        else               -> cs.outline
    }
    val borderWidth = if (slotState.isActive) 2.dp else 1.dp

    val shape = RoundedCornerShape(
        topStart    = if (isFirst) rounded else 0.dp,
        bottomStart = if (isFirst) rounded else 0.dp,
        topEnd      = if (isLast)  rounded else 0.dp,
        bottomEnd   = if (isLast)  rounded else 0.dp
    )

    Box(
        modifier = modifier
            .size(width = 32.dp, height = 40.dp)
            .clip(shape)
            .background(cs.surface)
            .border(borderWidth, borderColor, shape)
            .clickable { focusRequest?.requestFocus() },
        contentAlignment = Alignment.Center
    ) {
        when {
            slotState.char != null ->
                Text(
                    text      = slotState.char.toString(),
                    fontSize  = 14.sp,
                    color     = cs.onSurface,
                    textAlign = TextAlign.Center
                )
            slotState.hasFakeCaret -> BlinkingOTPCaret(color = cs.onSurface)
        }
    }
}

// ─────────────────────────────────────────────
//  InputOTPSeparator
// ─────────────────────────────────────────────

/**
 * Separator dash between [InputOTPGroup]s — mirrors `InputOTPSeparator`.
 *
 * ```kotlin
 * InputOTPSeparator()
 * ```
 */
@Composable
fun RowScope.InputOTPSeparator(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier.padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text     = "–",
            fontSize = 16.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────
//  Internal — blinking caret
// ─────────────────────────────────────────────

@Composable
private fun BlinkingOTPCaret(color: Color) {
    val infinite = rememberInfiniteTransition(label = "otp-caret")
    val alpha by infinite.animateFloat(
        initialValue  = 1f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "caretAlpha"
    )
    Box(
        modifier = Modifier
            .width(1.5.dp)
            .height(18.dp)
            .background(color.copy(alpha = alpha))
    )
}