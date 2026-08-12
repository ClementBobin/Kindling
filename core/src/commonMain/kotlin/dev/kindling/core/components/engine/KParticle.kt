package dev.kindling.core.components.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * State container for an individual physics particle in particle-based UI effects.
 */
data class KParticle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val maxLife: Float,
    var life: Float = 0f
) {
    /**
     * Calculates the remaining lifecycle ratio from `1.0f` (newly spawned) to `0.0f` (dead).
     */
    val alpha: Float
        get() = (1f - (life / maxLife)).coerceIn(0f, 1f)

    /**
     * Current scaled radius based on remaining lifecycle alpha.
     */
    val currentRadius: Float
        get() = size * alpha

    /**
     * Advance particle physics state by [deltaTime] seconds.
     */
    fun update(deltaTime: Float, gravity: Float = 180f) {
        life += deltaTime
        x += vx * deltaTime
        y += vy * deltaTime + (gravity * deltaTime)
    }

    /**
     * True when the particle lifetime has exceeded [maxLife].
     */
    val isDead: Boolean
        get() = life >= maxLife

    companion object {
        /**
         * Factory function to generate a burst of radial explosion particles.
         */
        fun createExplosionBurst(
            origin: Offset,
            count: Int,
            colors: List<Color>,
            baseForce: Float,
            forceMultiplier: Float = 1f
        ): List<KParticle> {
            return List(count) {
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val speed = (Random.nextFloat() * 0.7f + 0.3f) * baseForce * forceMultiplier
                val vx = cos(angle) * speed
                val vy = sin(angle) * speed

                KParticle(
                    x = origin.x,
                    y = origin.y,
                    vx = vx,
                    vy = vy,
                    color = colors.random(),
                    size = Random.nextFloat() * 12f + 6f,
                    maxLife = Random.nextFloat() * 0.4f + 0.6f
                )
            }
        }
    }
}