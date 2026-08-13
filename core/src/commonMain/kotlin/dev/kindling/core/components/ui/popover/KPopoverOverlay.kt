package dev.kindling.core.components.ui.popover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
internal fun KPopoverOverlay(
    open: Boolean,
    onDismiss: () -> Unit,
    position: KPopoverOverlayPosition,
    dismissOnClickOutside: Boolean,
    trigger: @Composable () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        trigger()

        if (open) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cs.scrim.copy(alpha = 0.4f))
                        .then(
                            if (dismissOnClickOutside) Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = onDismiss
                            ) else Modifier
                        )
                )

                val panelAlignment: Alignment = when (position) {
                    KPopoverOverlayPosition.Center      -> Alignment.Center
                    KPopoverOverlayPosition.BottomSheet -> Alignment.BottomCenter
                    KPopoverOverlayPosition.TopSheet    -> Alignment.TopCenter
                    KPopoverOverlayPosition.StartDrawer -> Alignment.CenterStart
                    KPopoverOverlayPosition.EndDrawer   -> Alignment.CenterEnd
                }

                val panelModifier: Modifier = when (position) {
                    KPopoverOverlayPosition.BottomSheet,
                    KPopoverOverlayPosition.TopSheet    -> Modifier.fillMaxWidth()
                    KPopoverOverlayPosition.StartDrawer,
                    KPopoverOverlayPosition.EndDrawer   -> Modifier.fillMaxHeight().widthIn(max = 320.dp)
                    KPopoverOverlayPosition.Center      -> Modifier.widthIn(min = 200.dp, max = 320.dp)
                }

                AnimatedVisibility(
                    visible  = open,
                    enter    = fadeIn(tween(200)) + expandVertically(tween(200), expandFrom = when (position) {
                        KPopoverOverlayPosition.TopSheet    -> Alignment.Top
                        KPopoverOverlayPosition.StartDrawer,
                        KPopoverOverlayPosition.EndDrawer   -> Alignment.CenterVertically
                        else                                -> Alignment.Bottom
                    }),
                    exit     = fadeOut(tween(200)) + shrinkVertically(tween(200), shrinkTowards = when (position) {
                        KPopoverOverlayPosition.TopSheet    -> Alignment.Top
                        KPopoverOverlayPosition.StartDrawer,
                        KPopoverOverlayPosition.EndDrawer   -> Alignment.CenterVertically
                        else                                -> Alignment.Bottom
                    }),
                    modifier = Modifier.align(panelAlignment)
                ) {
                    Column(
                        modifier = panelModifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = {}
                            )
                            .shadow(8.dp, when (position) {
                                KPopoverOverlayPosition.BottomSheet -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                KPopoverOverlayPosition.TopSheet    -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                KPopoverOverlayPosition.StartDrawer -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                                KPopoverOverlayPosition.EndDrawer   -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                                KPopoverOverlayPosition.Center      -> RoundedCornerShape(16.dp)
                            })
                            .clip(when (position) {
                                KPopoverOverlayPosition.BottomSheet -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                KPopoverOverlayPosition.TopSheet    -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                KPopoverOverlayPosition.StartDrawer -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                                KPopoverOverlayPosition.EndDrawer   -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                                KPopoverOverlayPosition.Center      -> RoundedCornerShape(16.dp)
                            })
                            .background(cs.surface)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        content             = content
                    )
                }
            }
        }
    }
}