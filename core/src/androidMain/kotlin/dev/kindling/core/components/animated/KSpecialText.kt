package dev.kindling.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.sin
import kotlin.random.Random

// ─── Enums & Defaults ────────────────────────────────────────────────────────

enum class KSpecialEffect {
    /** Matrix-style character scrambling effect that resolves to target text. */
    SCRAMBLE,

    /** Smooth animated gradient shimmer sweeping horizontally across text. */
    SHIMMER,

    /** Continuous animated spectrum hue transition across the text brush. */
    RAINBOW,

    /** Pulsating background light bloom effect behind text. */
    NEON_GLOW,

    /** Animated four-point star sparkles floating around text bounds. */
    SPARKLE
}

object KSpecialTextDefaults {
    val ShimmerColors = listOf(
        Color(0xFF6366F1),
        Color(0xFFA855F7),
        Color(0xFFEC4899),
        Color(0xFF3B82F6),
        Color(0xFF6366F1)
    )

    val RainbowColors = listOf(
        Color(0xFFFF0000),
        Color(0xFFFF7F00),
        Color(0xFFFFFF00),
        Color(0xFF00FF00),
        Color(0xFF0000FF),
        Color(0xFF4B0082),
        Color(0xFF8B00FF),
        Color(0xFFFF0000)
    )

    val GlowColor = Color(0xFF8B5CF6)
    val SparkleColor = Color(0xFFFFD700)

    const val SpeedMs: Int = 2400
    const val ScrambleSpeedMs: Long = 20L
    const val ScrambleRandomChars: String = "_!X$0-+*#"
    val GlowRadius: Dp = 14.dp
    const val SparkleCount: Int = 6
}

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * Enhanced text component supporting character scramble animations (inspired by Spell.sh / React SpecialText),
 * gradient shimmers, pulsating neon glows, rainbow shifts, and particle sparkles.
 *
 * @param text Content string to display.
 * @param modifier Applied to the outer layout box.
 * @param effect Visual effect mode ([KSpecialEffect.SCRAMBLE], [KSpecialEffect.SHIMMER], [KSpecialEffect.RAINBOW], [KSpecialEffect.NEON_GLOW], or [KSpecialEffect.SPARKLE]).
 * @param visible Controls whether scramble animation runs or resets.
 * @param style Typography style configuration.
 * @param scrambleSpeedMs Interval in milliseconds per frame step during [KSpecialEffect.SCRAMBLE].
 * @param scrambleDelayMs Delay in milliseconds prior to starting the scramble sequence.
 * @param colors Gradient colors used for shimmer or rainbow sweeps.
 * @param glowColor Glow tint for [KSpecialEffect.NEON_GLOW].
 * @param glowRadius Blur radius for [KSpecialEffect.NEON_GLOW].
 * @param speedMs Animation cycle duration in milliseconds for continuous effects.
 * @param sparkleCount Number of floating star sparkles when using [KSpecialEffect.SPARKLE].
 */
@Composable
fun KSpecialText(
    text: String,
    modifier: Modifier = Modifier,
    effect: KSpecialEffect = KSpecialEffect.SHIMMER,
    visible: Boolean = true,
    style: TextStyle = LocalTextStyle.current,
    scrambleSpeedMs: Long = KSpecialTextDefaults.ScrambleSpeedMs,
    scrambleDelayMs: Long = 0L,
    colors: List<Color> = if (effect == KSpecialEffect.RAINBOW) KSpecialTextDefaults.RainbowColors else KSpecialTextDefaults.ShimmerColors,
    glowColor: Color = KSpecialTextDefaults.GlowColor,
    glowRadius: Dp = KSpecialTextDefaults.GlowRadius,
    speedMs: Int = KSpecialTextDefaults.SpeedMs,
    sparkleCount: Int = KSpecialTextDefaults.SparkleCount
) {
    val infiniteTransition = rememberInfiniteTransition(label = "KSpecialTextTransition")

    // Continuous offset driving gradient sweeps
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpecialTextProgress"
    )

    // Pulsing alpha driving neon glow effect
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speedMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SpecialTextGlowAlpha"
    )

    // State container for two-phase scramble text animation
    var scrambleText by remember(text) { mutableStateOf(" ".repeat(text.length)) }

    LaunchedEffect(text, visible, effect) {
        if (effect == KSpecialEffect.SCRAMBLE && visible) {
            if (scrambleDelayMs > 0) delay(scrambleDelayMs)

            fun getRandomChar(prevChar: Char? = null): Char {
                val chars = KSpecialTextDefaults.ScrambleRandomChars
                var c: Char
                do {
                    c = chars[Random.nextInt(chars.length)]
                } while (c == prevChar && chars.length > 1)
                return c
            }

            // Phase 1: Fill line with random scrambling characters
            var phase1Step = 0
            val maxPhase1Steps = text.length * 2

            while (phase1Step < maxPhase1Steps && isActive) {
                val currentLength = (phase1Step + 1).coerceAtMost(text.length)
                val builder = StringBuilder()

                for (i in 0 until currentLength) {
                    val prevChar = if (i > 0) builder.lastOrNull() else null
                    builder.append(getRandomChar(prevChar))
                }
                for (i in currentLength until text.length) {
                    builder.append('\u00A0')
                }

                scrambleText = builder.toString()
                phase1Step++
                delay(scrambleSpeedMs)
            }

            // Phase 2: Progressively resolve characters from left to right
            var phase2Step = 0
            val maxPhase2Steps = text.length * 2

            while (phase2Step < maxPhase2Steps && isActive) {
                val revealedCount = phase2Step / 2
                val builder = StringBuilder()

                for (i in 0 until revealedCount.coerceAtMost(text.length)) {
                    builder.append(text[i])
                }

                if (revealedCount < text.length) {
                    if (phase2Step % 2 == 0) {
                        builder.append('_')
                    } else {
                        builder.append(getRandomChar())
                    }
                }

                while (builder.length < text.length) {
                    builder.append(getRandomChar())
                }

                scrambleText = builder.toString()
                phase2Step++
                delay(scrambleSpeedMs)
            }

            scrambleText = text
        } else if (effect == KSpecialEffect.SCRAMBLE && !visible) {
            scrambleText = " ".repeat(text.length)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (effect) {
            KSpecialEffect.SCRAMBLE -> {
                Text(
                    text = scrambleText,
                    style = style
                )
            }

            KSpecialEffect.SHIMMER -> {
                val shimmerBrush = remember(progress, colors) {
                    val width = 1200f
                    val shift = progress * width
                    Brush.linearGradient(
                        colors = colors,
                        start = Offset(shift - width, 0f),
                        end = Offset(shift, 0f),
                        tileMode = TileMode.Mirror
                    )
                }

                Text(
                    text = text,
                    style = style.copy(brush = shimmerBrush)
                )
            }

            KSpecialEffect.RAINBOW -> {
                val rainbowBrush = remember(progress, colors) {
                    val shift = progress * 1000f
                    Brush.horizontalGradient(
                        colors = colors,
                        startX = shift,
                        endX = shift + 800f,
                        tileMode = TileMode.Repeated
                    )
                }

                Text(
                    text = text,
                    style = style.copy(brush = rainbowBrush)
                )
            }

            KSpecialEffect.NEON_GLOW -> {
                Box(contentAlignment = Alignment.Center) {
                    // Glow background layer
                    Text(
                        text = text,
                        style = style.copy(color = glowColor.copy(alpha = glowAlpha)),
                        modifier = Modifier
                            .blur(glowRadius)
                            .offset(y = 1.dp)
                    )

                    // Foreground crisp text layer
                    Text(
                        text = text,
                        style = style
                    )
                }
            }

            KSpecialEffect.SPARKLE -> {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = text,
                        style = style
                    )

                    // Sparkle particles overlay
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val width = size.width
                        val height = size.height

                        repeat(sparkleCount) { index ->
                            val phase = (progress + (index.toFloat() / sparkleCount)) % 1f
                            val sparkAlpha = (sin(phase * Math.PI * 2) * 0.5f + 0.5f).toFloat()

                            val x = (width * 0.15f) + ((index * 37) % width.toInt() * 0.7f)
                            val y = (height * 0.1f) + ((index * 23) % height.toInt() * 0.8f)
                            val starSize = 4f + (sparkAlpha * 6f)

                            val starPath = Path().apply {
                                createStarPath(center = Offset(x, y), size = starSize)
                            }

                            drawPath(
                                path = starPath,
                                color = KSpecialTextDefaults.SparkleColor.copy(alpha = sparkAlpha)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Path.createStarPath(center: Offset, size: Float) {
    reset()
    moveTo(center.x, center.y - size)
    quadraticTo(center.x, center.y, center.x + size, center.y)
    quadraticTo(center.x, center.y, center.x, center.y + size)
    quadraticTo(center.x, center.y, center.x - size, center.y)
    quadraticTo(center.x, center.y, center.x, center.y - size)
    close()
}