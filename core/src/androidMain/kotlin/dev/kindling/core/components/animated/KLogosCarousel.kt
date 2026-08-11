package dev.kindling.core.components.animated

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.kindling.utils.method.KCounter

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

@Composable
fun KDefaultLogoItem(
    logo: KLogo,
    logoHeight: Dp,
    colorFilter: ColorFilter?
) {
    val context = LocalContext.current
    val model = logo.url ?: logo.resId

    Box(
        modifier = Modifier.height(logoHeight),
        contentAlignment = Alignment.Center
    ) {
        if (model != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(model)
                    .crossfade(true)
                    .build(),
                contentDescription = logo.alt.ifEmpty { null },
                colorFilter = colorFilter,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

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
    // ── KCounter Integration ────────────────────────
    loopCounter: KCounter? = null,
    speedStepCounter: KCounter? = null,
    customLogoContent: (@Composable (KLogo) -> Unit)? = null
) {
    var isPaused by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    // Observe speed multiplier from KCounter if provided
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

@Composable
private fun KLogosContentEngine(
    direction: KLogosCarouselDirection,
    velocityPx: Float,
    spacing: Dp,
    repeatCount: Int,
    isPaused: Boolean,
    loopCounter: KCounter?,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "KLogosTransition")
    var singleSetWidthPx by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val totalShiftPx = singleSetWidthPx + spacingPx

    val durationMillis = if (totalShiftPx > 0f && velocityPx > 0f) {
        ((totalShiftPx / velocityPx) * 1000).toInt().coerceAtLeast(1)
    } else 1000

    val offsetAnimation by transition.animateFloat(
        initialValue = if (direction == KLogosCarouselDirection.FORWARD) 0f else -totalShiftPx,
        targetValue = if (direction == KLogosCarouselDirection.FORWARD) -totalShiftPx else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "KLogosOffset"
    )

    // Trigger KCounter increment when offset wraps back to start
    var lastOffset by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(offsetAnimation) {
        if (direction == KLogosCarouselDirection.FORWARD && offsetAnimation > lastOffset && lastOffset < -totalShiftPx * 0.9f) {
            loopCounter?.increment()
        } else if (direction == KLogosCarouselDirection.BACKWARD && offsetAnimation < lastOffset && lastOffset > -totalShiftPx * 0.1f) {
            loopCounter?.increment()
        }
        lastOffset = offsetAnimation
    }

    val currentOffset = if (isPaused) 0f else offsetAnimation

    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val sampleMeasurables = subcompose("sample_measure") { content() }
        val samplePlaceables = sampleMeasurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val measuredSingleWidth = samplePlaceables.maxOfOrNull { it.width }?.toFloat() ?: 0f

        if (singleSetWidthPx != measuredSingleWidth) {
            singleSetWidthPx = measuredSingleWidth
        }

        val repeatedMeasurables = subcompose("repeated_logos") {
            Row {
                repeat(repeatCount) {
                    content()
                    Spacer(modifier = Modifier.width(spacing))
                }
            }
        }

        val placeables = repeatedMeasurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val maxContentHeight = placeables.maxOfOrNull { it.height } ?: 0

        layout(
            width = constraints.maxWidth,
            height = if (constraints.hasBoundedHeight) constraints.maxHeight else maxContentHeight
        ) {
            placeables.forEach { placeable ->
                placeable.placeRelative(x = currentOffset.toInt(), y = 0)
            }
        }
    }
}