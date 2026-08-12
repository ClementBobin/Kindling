package dev.kindling.core.components.animated

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import dev.kindling.utils.method.KCounter

@Composable
fun KLogosCarousel(
    logos: List<KLogo>,
    modifier: Modifier = Modifier,
    direction: KLogosCarouselDirection = KLogosCarouselDirection.FORWARD,
    velocity: Dp = KLogosCarouselDefaults.Velocity,
    spacing: Dp = KLogosCarouselDefaults.Spacing,
    logoHeight: Dp = KLogosCarouselDefaults.LogoHeight,
    repeatCount: Int = 4,
    pauseOnTouch: Boolean = true,
    enableGrayscale: Boolean = true,
    tint: Color? = null,
    enableFadeEdges: Boolean = true,
    fadeColor: Color = Color.Unspecified,
    fadeWidth: Dp = KLogosCarouselDefaults.FadeWidth,
    loopCounter: KCounter? = null,
    speedStepCounter: KCounter? = null,
    customLogoContent: (@Composable (KLogo) -> Unit)? = null
) {
    var isPaused by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val currentSpeedLevel by speedStepCounter?.state?.collectAsState() ?: remember { mutableIntStateOf(1) }
    val effectiveVelocity = velocity * currentSpeedLevel.coerceAtLeast(1)

    val velocityPx = with(density) { effectiveVelocity.toPx() }
    val resolvedFadeColor = if (fadeColor != Color.Unspecified) fadeColor else MaterialTheme.colorScheme.surface

    val grayscaleFilter = remember(enableGrayscale) {
        if (enableGrayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .then(
                if (pauseOnTouch) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        KLogosContentEngine(
            direction = direction,
            velocityPx = velocityPx,
            spacing = spacing,
            repeatCount = repeatCount,
            isPaused = isPaused,
            loopCounter = loopCounter
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                logos.forEach { logo ->
                    if (customLogoContent != null) {
                        customLogoContent(logo)
                    } else {
                        KDefaultLogoItem(
                            logo = logo,
                            logoHeight = logoHeight,
                            colorFilter = tint?.let { ColorFilter.tint(it) } ?: grayscaleFilter
                        )
                    }
                }
            }
        }

        if (enableFadeEdges) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(fadeWidth)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(resolvedFadeColor, Color.Transparent)))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(fadeWidth)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, resolvedFadeColor)))
            )
        }
    }
}