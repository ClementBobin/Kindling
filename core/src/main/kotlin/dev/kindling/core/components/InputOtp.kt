package dev.kindling.core.components

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

// ─────────────────────────────────────────────
//  KInputOtp
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style OTP / PIN input.
 *
 * Renders [length] individual digit cells backed by a single hidden
 * [BasicTextField].  The active cell shows an animated blinking caret.
 *
 * Mirrors `input-otp.tsx` (input-otp library), replicating the visual of
 * `InputOTPGroup` + `InputOTPSlot` + `InputOTPSeparator`.
 *
 * ```kotlin
 * var pin by remember { mutableStateOf("") }
 *
 * KInputOtp(
 *     value        = pin,
 *     onValueChange = { pin = it },
 *     length       = 6
 * )
 *
 * // With separator after position 3 (0-indexed)
 * KInputOtp(
 *     value         = pin,
 *     onValueChange = { pin = it },
 *     length        = 6,
 *     separatorAt   = setOf(2)
 * )
 * ```
 *
 * @param value         Current OTP string (digits only, length ≤ [length]).
 * @param onValueChange Callback with the new value on each keystroke.
 * @param length        Total number of digit slots.
 * @param separatorAt   0-based slot indices *after* which a separator dash is drawn.
 * @param isError       Highlights slots with error styling.
 * @param enabled       Whether the input is interactive.
 * @param modifier      Applied to the outer container row.
 */
@Composable
fun KInputOtp(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = 6,
    separatorAt: Set<Int> = emptySet(),
    isError: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    // Hidden backing field
    BasicTextField(
        value         = value.filter { it.isDigit() }.take(length),
        onValueChange = { raw ->
            val digits = raw.filter { it.isDigit() }.take(length)
            onValueChange(digits)
        },
        enabled       = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction    = ImeAction.Done
        ),
        cursorBrush = SolidColor(Color.Transparent), // caret handled in slot
        textStyle   = TextStyle(color = Color.Transparent, fontSize = 1.sp),
        modifier    = Modifier
            .size(1.dp)  // keep it out of visual layout
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
        decorationBox = { /* invisible */ }
    )

    // Visual slots row
    Row(
        modifier          = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(length) { index ->
            val char    = value.getOrNull(index)
            val isActive = isFocused && index == value.length.coerceAtMost(length - 1)

            OtpSlot(
                char     = char,
                isActive = isActive,
                isError  = isError,
                isFirst  = index == 0,
                isLast   = index == length - 1,
                onClick  = { if (enabled) focusRequester.requestFocus() }
            )

            if (index in separatorAt) {
                OtpSeparator()
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Internal — single slot cell
// ─────────────────────────────────────────────

@Composable
private fun OtpSlot(
    char: Char?,
    isActive: Boolean,
    isError: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    val borderColor = when {
        isError  -> cs.error
        isActive -> cs.primary
        else     -> cs.outline
    }

    val borderWidth = if (isActive) 2.dp else 1.dp

    // Shape: rounded only on the outside edges to create a joined-slot look
    val shape = RoundedCornerShape(
        topStart     = if (isFirst) 4.dp else 0.dp,
        bottomStart  = if (isFirst) 4.dp else 0.dp,
        topEnd       = if (isLast)  4.dp else 0.dp,
        bottomEnd    = if (isLast)  4.dp else 0.dp
    )

    Box(
        modifier = Modifier
            .size(width = 32.dp, height = 40.dp)
            .clip(shape)
            .background(cs.surface)
            .border(borderWidth, borderColor, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (char != null) {
            Text(
                text      = char.toString(),
                fontSize  = 14.sp,
                color     = cs.onSurface,
                textAlign = TextAlign.Center
            )
        } else if (isActive) {
            BlinkingCaret(color = cs.onSurface)
        }
    }
}

// ─────────────────────────────────────────────
//  Internal — blinking caret
// ─────────────────────────────────────────────

@Composable
private fun BlinkingCaret(color: Color) {
    val infinite = rememberInfiniteTransition(label = "caret")
    val alpha by infinite.animateFloat(
        initialValue  = 1f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1_000, easing = LinearEasing),
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

// ─────────────────────────────────────────────
//  Internal — separator
// ─────────────────────────────────────────────

@Composable
private fun OtpSeparator() {
    Box(
        modifier         = Modifier
            .padding(horizontal = 6.dp)
            .width(12.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "-",
            fontSize  = 14.sp,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────
//  Group / slot convenience wrappers
//  (mirrors InputOTPGroup / InputOTPSlot as
//   standalone composables for custom layouts)
// ─────────────────────────────────────────────

/**
 * Groups a set of [KInputOtp] slots visually — adds a shared border radius
 * to a horizontally arranged block of slots.
 *
 * For most use-cases prefer [KInputOtp] directly.
 */
@Composable
fun KInputOtpGroup(
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