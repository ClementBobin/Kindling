package dev.kindling.core.components.ui.animated.logosCarousel

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KLogo(
    val url: String? = null,
    val resId: Int? = null,
    val alt: String = ""
)

enum class KLogosCarouselDirection {
    FORWARD, BACKWARD
}

object KLogosCarouselDefaults {
    val Velocity: Dp = 50.dp
    val Spacing: Dp = 24.dp
    val LogoHeight: Dp = 32.dp
    val FadeWidth: Dp = 48.dp
}