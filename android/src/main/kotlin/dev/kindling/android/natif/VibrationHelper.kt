package dev.kindling.android.natif

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission

// ─────────────────────────────────────────────
//  VibrationPattern
// ─────────────────────────────────────────────

/**
 * Describes a semantic vibration pattern.
 *
 * Use the provided presets for consistent UI feedback:
 * - [VibrationPattern.Error]   -> Double strong pulse (for server or network errors).
 * - [VibrationPattern.Warning] -> Single moderate pulse (for user input errors).
 * - [VibrationPattern.Success] -> Short gentle pulse (for successful operations).
 * - [VibrationPattern.Light]   -> Subtle tick (generic UI feedback).
 *
 * ### Custom patterns:
 * ```kotlin
 * val pattern = VibrationPattern(
 *     timings = longArrayOf(0, 50, 100, 50),
 *     amplitudes = intArrayOf(0, 255, 0, 128),
 *     fallbackMs = 50L
 * )
 * vibrationHelper.vibrate(pattern)
 * ```
 */
data class VibrationPattern(
    val timings: LongArray,
    val amplitudes: IntArray,
    val fallbackMs: Long
) {
    init {
        require(timings.size == amplitudes.size) {
            "timings and amplitudes must have the same length (got ${timings.size} vs ${amplitudes.size})"
        }
        require(timings.all { it >= 0L }) {
            "all timing values must be non-negative"
        }
        require(amplitudes.all { it == -1 || it in 0..255 }) {
            "amplitude values must be -1 (DEFAULT_AMPLITUDE) or in 0..255"
        }
        require(fallbackMs > 0) {
            "fallbackMs must be positive (got $fallbackMs)"
        }
    }

    companion object {
        /** Double short/strong pulse - for server errors (5xx) or network failures. */
        val Error = VibrationPattern(
            timings    = longArrayOf(0, 80, 60, 80),
            amplitudes = intArrayOf(0, 220, 0, 220),
            fallbackMs = 80L
        )

        /** Single moderate pulse - for client input errors (4xx). */
        val Warning = VibrationPattern(
            timings    = longArrayOf(0, 120),
            amplitudes = intArrayOf(0, 160),
            fallbackMs = 120L
        )

        /** Short gentle pulse - for successful operation feedback. */
        val Success = VibrationPattern(
            timings    = longArrayOf(0, 60),
            amplitudes = intArrayOf(0, 100),
            fallbackMs = 60L
        )

        /** Subtle tick - for generic interactive feedback. */
        val Light = VibrationPattern(
            timings    = longArrayOf(0, 30),
            amplitudes = intArrayOf(0, 60),
            fallbackMs = 30L
        )
    }

    // LongArray / IntArray don't participate in data class equals/hashCode
    // automatically — override to keep the data class contract correct.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VibrationPattern) return false
        return fallbackMs == other.fallbackMs
                && timings.contentEquals(other.timings)
                && amplitudes.contentEquals(other.amplitudes)
    }

    override fun hashCode(): Int {
        var result = timings.contentHashCode()
        result = 31 * result + amplitudes.contentHashCode()
        result = 31 * result + fallbackMs.hashCode()
        return result
    }
}

// ─────────────────────────────────────────────
//  VibrationHelper
// ─────────────────────────────────────────────

/**
 * Centralized vibration helper for Android.
 *
 * Simplifies playing haptic feedback and custom vibration waveforms.
 *
 * **Requires permission:** `VIBRATE` must be declared in the Manifest.
 *
 * ### Example usage:
 * ```kotlin
 * val vibrationHelper = VibrationHelper(context)
 * 
 * // Using presets
 * vibrationHelper.error()
 * vibrationHelper.success()
 * 
 * // Using custom patterns
 * vibrationHelper.vibrate(myPattern)
 * ```
 */
class VibrationHelper(context: Context) {

    internal val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            ?: throw IllegalStateException("VibratorManager unavailable on API ${Build.VERSION.SDK_INT}")
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            ?: throw IllegalStateException("Vibrator service unavailable on this device")
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Joue le [pattern] fourni. */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(pattern: VibrationPattern) = vibratePattern(
        timings    = pattern.timings,
        amplitudes = pattern.amplitudes,
        fallbackMs = pattern.fallbackMs
    )

    // ── Convenience shorthands ────────────────────────────────────────────────

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun error()   = vibrate(VibrationPattern.Error)

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun warning() = vibrate(VibrationPattern.Warning)

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun success() = vibrate(VibrationPattern.Success)

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun light()   = vibrate(VibrationPattern.Light)

    // ── Internal ──────────────────────────────────────────────────────────────

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibratePattern(
        timings: LongArray,
        amplitudes: IntArray,
        fallbackMs: Long
    ) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (vibrator.hasAmplitudeControl()) {
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            } else {
                VibrationEffect.createOneShot(fallbackMs, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(fallbackMs)
        }
    }
}