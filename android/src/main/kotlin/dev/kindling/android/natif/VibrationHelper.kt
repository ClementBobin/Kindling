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
 * Décrit un pattern de vibration sémantique.
 *
 * Les presets alignés avec les états UI sont disponibles via le companion :
 * - [VibrationPattern.Error]   → double impulsion courte/forte  (erreur serveur, réseau)
 * - [VibrationPattern.Warning] → impulsion unique modérée        (erreur client 4xx)
 * - [VibrationPattern.Success] → impulsion douce courte          (opération réussie)
 * - [VibrationPattern.Light]   → tick léger                      (feedback UI générique)
 *
 * Patterns personnalisés :
 * ```kotlin
 * val myPattern = VibrationPattern(
 *     timings    = longArrayOf(0, 50, 30, 50, 30, 50),
 *     amplitudes = intArrayOf(0, 255, 0, 200, 0, 150),
 *     fallbackMs = 50L
 * )
 * vibrationHelper.vibrate(myPattern)
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
    }

    companion object {
        /** Double impulsion courte/forte — erreur serveur (5xx) ou réseau. */
        val Error = VibrationPattern(
            timings    = longArrayOf(0, 80, 60, 80),
            amplitudes = intArrayOf(0, 220, 0, 220),
            fallbackMs = 80L
        )

        /** Impulsion unique modérée — erreur client (4xx). */
        val Warning = VibrationPattern(
            timings    = longArrayOf(0, 120),
            amplitudes = intArrayOf(0, 160),
            fallbackMs = 120L
        )

        /** Impulsion douce courte — succès d'une opération. */
        val Success = VibrationPattern(
            timings    = longArrayOf(0, 60),
            amplitudes = intArrayOf(0, 100),
            fallbackMs = 60L
        )

        /** Tick léger — feedback générique. */
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
 * Helper de vibration centralisé.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { VibrationHelper(androidContext()) }
 * ```
 *
 * Utilisation avec un preset :
 * ```kotlin
 * val vibrationHelper: VibrationHelper by inject()
 * vibrationHelper.vibrate(VibrationPattern.Error)
 * ```
 *
 * Utilisation avec un pattern personnalisé :
 * ```kotlin
 * vibrationHelper.vibrate(
 *     VibrationPattern(
 *         timings    = longArrayOf(0, 100, 50, 100),
 *         amplitudes = intArrayOf(0, 255, 0, 180),
 *         fallbackMs = 100L
 *     )
 * )
 * ```
 *
 * L'app doit déclarer `<uses-permission android:name="android.permission.VIBRATE"/>`
 * dans son `AndroidManifest.xml`.
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