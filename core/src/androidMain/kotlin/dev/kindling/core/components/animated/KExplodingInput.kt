package dev.kindling.core.components.animated

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.engine.KParticle
import kotlinx.coroutines.launch

// ─── Defaults ─────────────────────────────────────────────────────────────────

object KExplodingInputDefaults {
    val Height: Dp = 56.dp
    val ParticleCount: Int = 36
    val ExplosionForce: Float = 450f

    val DefaultColors = listOf(
        Color(0xFFFF5964),
        Color(0xFFFFAD05),
        Color(0xFF35A7FF),
        Color(0xFF38B000),
        Color(0xFF9D4EDD),
        Color(0xFFF72585)
    )
}

// ─── Component Implementation ────────────────────────────────────────────────

/**
 * An interactive text input that triggers a vibrant particle explosion effect upon
 * submission or text changes (spell.sh / magic UI inspired).
 *
 * @param value Current input text value.
 * @param onValueChange Callback when text changes.
 * @param modifier Applied to the input container layout.
 * @param placeholder Placeholder text when value is empty.
 * @param onSubmit Triggered when submission key or action button is pressed.
 * @param particleCount Number of particles generated per burst.
 * @param particleColors Color palette for exploding particles.
 * @param explodeOnSubmit Triggers explosion burst when user submits input.
 * @param explodeOnType Triggers small micro-bursts on every keystroke.
 * @param enabled Controls input enabled state.
 */
@Composable
fun KExplodingInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Type something...",
    onSubmit: ((String) -> Unit)? = null,
    particleCount: Int = KExplodingInputDefaults.ParticleCount,
    particleColors: List<Color> = KExplodingInputDefaults.DefaultColors,
    explodeOnSubmit: Boolean = true,
    explodeOnType: Boolean = false,
    enabled: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    val particles = remember { mutableStateListOf<KParticle>() }
    val animatable = remember { Animatable(0f) }

    var originOffset by remember { mutableStateOf(Offset.Zero) }

    fun triggerExplosion(origin: Offset, count: Int = particleCount, forceMultiplier: Float = 1f) {
        val newParticles = KParticle.createExplosionBurst(
            origin = origin,
            count = count,
            colors = particleColors,
            baseForce = KExplodingInputDefaults.ExplosionForce,
            forceMultiplier = forceMultiplier
        )

        particles.addAll(newParticles)

        scope.launch {
            animatable.snapTo(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
            ) {
                val deltaTime = 0.016f // ~60fps frame step

                particles.forEach { p ->
                    p.update(deltaTime = deltaTime)
                }

                particles.removeAll { it.isDead }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(KExplodingInputDefaults.Height)
    ) {
        // Input Field Container
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.extraLarge,
            color = cs.surfaceVariant.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                cs.outline.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Text Field Input
                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        onValueChange(newValue)
                        if (explodeOnType && newValue.length > value.length) {
                            triggerExplosion(originOffset, count = 8, forceMultiplier = 0.4f)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = cs.onSurface
                    ),
                    cursorBrush = SolidColor(cs.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (value.isNotBlank()) {
                                if (explodeOnSubmit) triggerExplosion(originOffset)
                                onSubmit?.invoke(value)
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontSize = 15.sp,
                                    color = cs.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Submit Button / Explosion Anchor
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (value.isNotBlank()) cs.primary else cs.surfaceVariant)
                        .graphicsLayer {
                            originOffset = Offset(
                                x = size.width / 2f + x,
                                y = size.height / 2f + y
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (value.isNotBlank()) {
                                if (explodeOnSubmit) triggerExplosion(originOffset)
                                onSubmit?.invoke(value)
                            }
                        },
                        enabled = value.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowForward,
                            contentDescription = "Submit",
                            tint = if (value.isNotBlank()) cs.onPrimary else cs.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Particle Overlay Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.currentRadius,
                    center = Offset(p.x, p.y)
                )
            }
        }
    }
}