package dev.kindling.core.components.ui.inputOTP

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes

// Internal composition local so slots can request focus
val LocalOTPFocusRequester = compositionLocalOf<FocusRequester?> { null }

/**
 * Shadcn/ui-style OTP / PIN input root — mirrors `InputOTP` from `input-otp.tsx`.
 *
 * Place [InputOTPGroup]s (with [InputOTPSlot]s) and [InputOTPSeparator]s inside.
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

/**
 * Groups consecutive [InputOTPSlot]s into a visually joined block.
 * Mirrors `InputOTPGroup`.
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

/**
 * Separator dash between [InputOTPGroup]s — mirrors `InputOTPSeparator`.
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