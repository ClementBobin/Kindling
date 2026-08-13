package dev.kindling.core.components.ui.animated

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ─── Enums & Defaults ────────────────────────────────────────────────────────

enum class KSlideUpMode {
    /** Animates text word-by-word with staggered upward translations. */
    WORD,

    /** Animates text character-by-character with staggered upward translations. */
    CHARACTER,

    /** Animates the entire block as a single unit sliding up into view. */
    ALL
}

object KSlideUpTextDefaults {
    val InitialYOffset: Dp = 24.dp
    const val DurationMs: Int = 600
    const val StaggerMs: Int = 50
    const val DelayMs: Int = 0
    val DefaultEasing: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f) // Smooth ease-out curve
}

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * Animated slide-up container that translates content upward while fading in (Spell.sh inspired).
 *
 * @param visible Controls whether the slide-up animation triggers or resets.
 * @param modifier Applied to the component wrapper layout.
 * @param initialYOffset Starting downward Y-axis translation before animating into place.
 * @param clipToBounds Clips sliding content to its layout bounds for a clean mask reveal effect.
 * @param durationMs Animation duration in milliseconds.
 * @param delayMs Base delay before the animation starts.
 * @param easing Easing transition curve.
 * @param content Target content to reveal.
 */
@Composable
fun KSlideUp(
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    initialYOffset: Dp = KSlideUpTextDefaults.InitialYOffset,
    clipToBounds: Boolean = false,
    durationMs: Int = KSlideUpTextDefaults.DurationMs,
    delayMs: Int = KSlideUpTextDefaults.DelayMs,
    easing: Easing = KSlideUpTextDefaults.DefaultEasing,
    content: @Composable () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs.toLong())
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMs, easing = easing)
            )
        } else {
            progress.snapTo(0f)
        }
    }

    val p = progress.value
    val currentAlpha = p.coerceIn(0f, 1f)
    val currentY = initialYOffset * (1f - p)

    Box(
        modifier = modifier
            .then(if (clipToBounds) Modifier.clipToBounds() else Modifier)
            .graphicsLayer {
                alpha = currentAlpha
                translationY = currentY.toPx()
            }
    ) {
        content()
    }
}

/**
 * Text component that reveals text upward word-by-word, character-by-character, or as a full block
 * with staggered delays and smooth Y-axis physics (Spell.sh / Magic UI inspired).
 *
 * @param text Source text string to animate.
 * @param modifier Applied to the outer text container layout.
 * @param visible Controls whether the animation runs or resets.
 * @param mode Determines sequence division ([KSlideUpMode.WORD], [KSlideUpMode.CHARACTER], or [KSlideUpMode.ALL]).
 * @param style Typography style applied to text elements.
 * @param initialYOffset Distance below final position from which characters/words slide up.
 * @param clipToBounds Mask content to container bounds as it slides upward.
 * @param durationMs Animation duration for each individual element.
 * @param staggerMs Stagger delay interval between successive words/characters.
 * @param delayMs Initial delay before starting sequence.
 * @param easing Easing transition curve.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KSlideUpText(
    text: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    mode: KSlideUpMode = KSlideUpMode.WORD,
    style: TextStyle = LocalTextStyle.current,
    initialYOffset: Dp = KSlideUpTextDefaults.InitialYOffset,
    clipToBounds: Boolean = false,
    durationMs: Int = KSlideUpTextDefaults.DurationMs,
    staggerMs: Int = KSlideUpTextDefaults.StaggerMs,
    delayMs: Int = KSlideUpTextDefaults.DelayMs,
    easing: Easing = KSlideUpTextDefaults.DefaultEasing
) {
    when (mode) {
        KSlideUpMode.ALL -> {
            KSlideUp(
                visible = visible,
                modifier = modifier,
                initialYOffset = initialYOffset,
                clipToBounds = clipToBounds,
                durationMs = durationMs,
                delayMs = delayMs,
                easing = easing
            ) {
                Text(text = text, style = style)
            }
        }

        KSlideUpMode.WORD -> {
            val words = remember(text) { text.split("\\s+".toRegex()).filter { it.isNotEmpty() } }

            FlowRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                words.forEachIndexed { index, word ->
                    KSlideUp(
                        visible = visible,
                        initialYOffset = initialYOffset,
                        clipToBounds = clipToBounds,
                        durationMs = durationMs,
                        delayMs = delayMs + (index * staggerMs),
                        easing = easing
                    ) {
                        Text(text = word, style = style)
                    }
                }
            }
        }

        KSlideUpMode.CHARACTER -> {
            val chars = remember(text) { text.map { it.toString() } }

            FlowRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                chars.forEachIndexed { index, char ->
                    KSlideUp(
                        visible = visible,
                        initialYOffset = initialYOffset,
                        clipToBounds = clipToBounds,
                        durationMs = durationMs,
                        delayMs = delayMs + (index * staggerMs),
                        easing = easing
                    ) {
                        Text(
                            text = if (char == " ") "\u00A0" else char,
                            style = style
                        )
                    }
                }
            }
        }
    }
}