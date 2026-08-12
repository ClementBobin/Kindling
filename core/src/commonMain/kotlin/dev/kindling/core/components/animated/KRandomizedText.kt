package dev.kindling.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

// ─── Enums & Defaults ────────────────────────────────────────────────────────

enum class KRandomizedSplitMode {
    /** Splits the text into individual words with randomized delays. */
    WORDS,

    /** Splits the text into individual characters with randomized delays. */
    CHARS
}

object KRandomizedTextDefaults {
    val SplitMode = KRandomizedSplitMode.WORDS
    const val BaseDelaySec: Float = 0.2f
    const val DurationMs: Int = 1200
}

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * Text component that reveals words or characters with randomized stagger delays
 * and a smooth exponential fade-in transition (Spell.sh / React RandomizedText inspired).
 *
 * @param text The source string to render and animate.
 * @param modifier Applied to the FlowRow or text wrapper layout.
 * @param visible Controls whether the randomized fade-in animation triggers or resets.
 * @param split Specifies whether to break down by [KRandomizedSplitMode.WORDS] or [KRandomizedSplitMode.CHARS].
 * @param baseDelaySec Base delay offset in seconds before elements start animating.
 * @param durationMs Animation duration per element in milliseconds.
 * @param style Typography style applied to the text elements.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KRandomizedText(
    text: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    split: KRandomizedSplitMode = KRandomizedTextDefaults.SplitMode,
    baseDelaySec: Float = KRandomizedTextDefaults.BaseDelaySec,
    durationMs: Int = KRandomizedTextDefaults.DurationMs,
    style: TextStyle = LocalTextStyle.current
) {
    val elements = remember(text, split) {
        if (split == KRandomizedSplitMode.CHARS) {
            text.map { if (it == ' ') "\u00A0" else it.toString() }
        } else {
            text.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        }
    }

    // Generate stable randomized delay offsets for each element
    val randomizedDelays = remember(text, split, baseDelaySec) {
        elements.map {
            (baseDelaySec + (Random.nextFloat() * 0.2f) + (Random.nextFloat() * 0.03f)) * 1000f
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMs + 400, easing = LinearEasing)
            )
        } else {
            progress.snapTo(0f)
        }
    }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (split == KRandomizedSplitMode.WORDS) 6.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        elements.forEachIndexed { index, element ->
            val elementDelay = randomizedDelays[index]
            val p = remember(progress.value, elementDelay, durationMs) {
                val totalTime = progress.value * (durationMs + 400f)
                val currentProgress = (totalTime - elementDelay) / durationMs
                currentProgress.coerceIn(0f, 1f)
            }

            // Custom Exponential Out easing function: 1 - 2^(-10 * t)
            val easedAlpha = if (p >= 1f) 1f else if (p <= 0f) 0f else (1f - Math.pow(2.0, -10.0 * p.toDouble())).toFloat()

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = easedAlpha
                    }
            ) {
                Text(text = element, style = style)
            }
        }
    }
}