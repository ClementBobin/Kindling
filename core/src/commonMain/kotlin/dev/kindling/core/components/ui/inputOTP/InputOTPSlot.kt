package dev.kindling.core.components.ui.inputOTP

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.LocalKindlingShapes

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
    val cs         = MaterialTheme.colorScheme
    val slotState    = state.slots.getOrNull(index) ?: return
    val focusRequest = LocalOTPFocusRequester.current
    val rounded      = LocalKindlingShapes.current.roundedLg

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