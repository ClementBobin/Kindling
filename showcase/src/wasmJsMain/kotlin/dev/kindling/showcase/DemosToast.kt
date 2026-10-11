package dev.kindling.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.KAspectRatio
import dev.kindling.core.components.ui.animated.KHighlightedText
import dev.kindling.core.components.ui.animated.KSlideUpMode
import dev.kindling.core.components.ui.animated.KSlideUpText
import dev.kindling.core.components.ui.animated.KSpecialEffect
import dev.kindling.core.components.ui.animated.KSpecialText
import dev.kindling.core.components.ui.animated.KWordsStagger
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonSize
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.core.components.ui.carousel.KCarousel
import dev.kindling.core.components.ui.carousel.KCarouselContent
import dev.kindling.core.components.ui.carousel.rememberCarouselApi
import dev.kindling.core.components.ui.logosCarousel.KLogo
import dev.kindling.core.components.ui.logosCarousel.KLogosCarousel
import dev.kindling.core.components.ui.masonry.KMasonryGrid
import dev.kindling.core.components.ui.masonry.KMasonryItem
import dev.kindling.core.components.ui.stepper.KStepper
import dev.kindling.core.components.ui.stepper.KStepperContent
import dev.kindling.core.components.ui.stepper.KStepperDescription
import dev.kindling.core.components.ui.stepper.KStepperIndicator
import dev.kindling.core.components.ui.stepper.KStepperItem
import dev.kindling.core.components.ui.stepper.KStepperList
import dev.kindling.core.components.ui.stepper.KStepperNext
import dev.kindling.core.components.ui.stepper.KStepperPrev
import dev.kindling.core.components.ui.stepper.KStepperSeparator
import dev.kindling.core.components.ui.stepper.KStepperTitle
import dev.kindling.core.components.ui.stepper.KStepperTrigger
import dev.kindling.core.components.ui.stepper.rememberKStepperState
import dev.kindling.core.components.ui.toast.KToastManager
import dev.kindling.core.components.ui.toast.KToaster

// ---------------------------------------------------------------- Toast

@Composable
fun ToastDemo() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KButton(
                    text = "Default",
                    onClick = { KToastManager.show("Event created", "Friday at 5:00 PM") },
                    variant = KButtonVariant.Outline,
                    size = KButtonSize.Sm
                )
                KButton(
                    text = "Success",
                    onClick = { KToastManager.success("Saved", "Your changes were saved.") },
                    variant = KButtonVariant.Outline,
                    size = KButtonSize.Sm
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KButton(
                    text = "Error",
                    onClick = { KToastManager.error("Upload failed", "Please try again.") },
                    variant = KButtonVariant.Outline,
                    size = KButtonSize.Sm
                )
                KButton(
                    text = "Action",
                    onClick = {
                        KToastManager.show(
                            message = "File deleted",
                            actionLabel = "Undo",
                            onAction = { KToastManager.info("Restored") }
                        )
                    },
                    variant = KButtonVariant.Outline,
                    size = KButtonSize.Sm
                )
            }
        }
        KToaster(maxVisible = 2)
    }
}

