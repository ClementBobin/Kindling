package dev.kindling.core.components.ui.popover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.kindling.core.theme.LocalKindlingShapes
import dev.kindling.core.theme.kindlingShadowMd
import kotlin.math.roundToInt

@Composable
internal fun KPopoverInline(
    open: Boolean,
    onDismiss: () -> Unit,
    trigger: @Composable () -> Unit,
    side: KPopoverSide,
    align: KPopoverAlign,
    sideOffset: Dp,
    dismissOnClickOutside: Boolean,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs      = MaterialTheme.colorScheme
    val shape = LocalKindlingShapes.current.radiusLg
    val density = LocalDensity.current

    var triggerW  by remember { mutableStateOf(0) }
    var triggerH  by remember { mutableStateOf(0) }
    var panelW    by remember { mutableStateOf(0) }
    var panelH    by remember { mutableStateOf(0) }
    var rootW     by remember { mutableStateOf(Int.MAX_VALUE) }
    var triggerRootX by remember { mutableStateOf(0f) }

    val sideOffsetPx = with(density) { sideOffset.toPx().roundToInt() }

    fun rawOffset(): IntOffset = when (side) {
        KPopoverSide.Bottom -> IntOffset(
            x = when (align) {
                KPopoverAlign.Start  -> 0
                KPopoverAlign.End    -> triggerW - panelW
                KPopoverAlign.Center -> (triggerW - panelW) / 2
            },
            y = triggerH + sideOffsetPx
        )
        KPopoverSide.Top -> IntOffset(
            x = when (align) {
                KPopoverAlign.Start  -> 0
                KPopoverAlign.End    -> triggerW - panelW
                KPopoverAlign.Center -> (triggerW - panelW) / 2
            },
            y = -(panelH + sideOffsetPx)
        )
        KPopoverSide.Right -> IntOffset(
            x = triggerW + sideOffsetPx,
            y = when (align) {
                KPopoverAlign.Start  -> 0
                KPopoverAlign.End    -> triggerH - panelH
                KPopoverAlign.Center -> (triggerH - panelH) / 2
            }
        )
        KPopoverSide.Left -> IntOffset(
            x = -(panelW + sideOffsetPx),
            y = when (align) {
                KPopoverAlign.Start  -> 0
                KPopoverAlign.End    -> triggerH - panelH
                KPopoverAlign.Center -> (triggerH - panelH) / 2
            }
        )
    }

    fun clampedOffset(): IntOffset {
        val raw = rawOffset()
        val absLeft  = triggerRootX + raw.x
        val absRight = absLeft + panelW
        val clampedX = when {
            rootW < Int.MAX_VALUE && absRight > rootW -> raw.x - (absRight - rootW).roundToInt()
            absLeft < 0f                            -> raw.x - absLeft.roundToInt()
            else                                    -> raw.x
        }
        return IntOffset(clampedX, raw.y)
    }

    Box(
        modifier = modifier.onGloballyPositioned { coords ->
            rootW = coords.size.width
        }
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned { coords ->
                triggerW     = coords.size.width
                triggerH     = coords.size.height
                triggerRootX = coords.positionInRoot().x
            }
        ) {
            trigger()
        }

        if (open && dismissOnClickOutside) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(8f)
                    .requiredSize(with(density) { rootW.toDp() }, 4000.dp)
                    .offset { IntOffset(-triggerRootX.roundToInt(), -triggerH) }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onDismiss
                    )
            )
        }

        AnimatedVisibility(
            visible  = open,
            enter    = expandVertically(tween(150)) + fadeIn(tween(150)),
            exit     = shrinkVertically(tween(150)) + fadeOut(tween(150)),
            modifier = Modifier
                .offset { clampedOffset() }
                .zIndex(9f)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 200.dp, max = 320.dp)
                    .onGloballyPositioned { coords ->
                        panelW = coords.size.width
                        panelH = coords.size.height
                    }
                    .kindlingShadowMd(shape)
                    .clip(shape)
                    .background(cs.surface)
                    .border(1.dp, cs.outline.copy(alpha = 0.4f), shape)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content             = content
            )
        }
    }
}