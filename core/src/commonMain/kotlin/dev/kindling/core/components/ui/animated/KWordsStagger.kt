package dev.kindling.core.components.ui.animated

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ─── Defaults ─────────────────────────────────────────────────────────────────

object KWordsStaggerDefaults {
    val InitialYOffset: Dp = 10.dp
    val InitialBlurRadius: Dp = 10.dp
    val DurationMs: Int = 500
    val StaggerMs: Int = 100
    val DelayMs: Int = 0
    val DefaultEasing: Easing = FastOutSlowInEasing
}

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * Animated word-by-word stagger text reveal container combining Y-axis translation, fade,
 * and blur transitions (Spell.sh / React WordsStagger inspired).
 *
 * @param text The string content to split and stagger.
 * @param modifier Applied to the FlowRow container layout.
 * @param visible Controls whether the stagger animation runs or resets.
 * @param autoStart Automatically triggers animation on initial composition if true.
 * @param style Typography style applied to the text words.
 * @param initialYOffset Starting downward Y translation for each word.
 * @param initialBlurRadius Starting blur radius for each word.
 * @param durationMs Animation duration per word in milliseconds.
 * @param staggerMs Stagger interval delay between successive words.
 * @param delayMs Initial base delay before the sequence starts.
 * @param easing Easing transition specification.
 * @param onStart Callback triggered when animation starts.
 * @param onComplete Callback triggered when animation completes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KWordsStagger(
    text: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    autoStart: Boolean = true,
    style: TextStyle = LocalTextStyle.current,
    initialYOffset: Dp = KWordsStaggerDefaults.InitialYOffset,
    initialBlurRadius: Dp = KWordsStaggerDefaults.InitialBlurRadius,
    durationMs: Int = KWordsStaggerDefaults.DurationMs,
    staggerMs: Int = KWordsStaggerDefaults.StaggerMs,
    delayMs: Int = KWordsStaggerDefaults.DelayMs,
    easing: Easing = KWordsStaggerDefaults.DefaultEasing,
    onStart: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null
) {
    val words = remember(text) { text.split("\\s+".toRegex()).filter { it.isNotEmpty() } }
    val progress = remember { Animatable(0f) }
    var hasStarted by remember { mutableStateOf(false) }

    val shouldAnimate = visible || autoStart

    LaunchedEffect(shouldAnimate) {
        if (shouldAnimate) {
            if (!hasStarted) {
                hasStarted = true
                onStart?.invoke()
            }
            if (delayMs > 0) delay(delayMs.toLong())

            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMs, easing = easing)
            )
            onComplete?.invoke()
        } else {
            progress.snapTo(0f)
            hasStarted = false
        }
    }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        words.forEachIndexed { index, word ->
            val wordDelay = index * staggerMs
            val p = remember(progress.value, wordDelay, durationMs) {
                val totalTime = progress.value * (durationMs + (words.size * staggerMs))
                val currentWordProgress = (totalTime - wordDelay) / durationMs
                currentWordProgress.coerceIn(0f, 1f)
            }

            val currentAlpha = p
            val currentY = initialYOffset * (1f - p)
            val currentBlur = initialBlurRadius * (1f - p)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = currentAlpha
                        translationY = currentY.toPx()
                    }
                    .blur(currentBlur)
            ) {
                Text(text = word, style = style)
            }
        }
    }
}