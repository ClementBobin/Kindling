package dev.kindling.android.natif

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.annotation.RequiresApi
import kotlin.math.roundToInt
import androidx.core.net.toUri

// ─────────────────────────────────────────────
//  BrightnessLevel
// ─────────────────────────────────────────────

/**
 * Décrit un niveau de luminosité d'écran.
 *
 * [value] est normalisé entre 0f (minimum) et 1f (maximum).
 *
 * Presets :
 * - [BrightnessLevel.Min]    → luminosité minimale (0f)
 * - [BrightnessLevel.Low]    → faible (0.25f)
 * - [BrightnessLevel.Medium] → moyen (0.5f)
 * - [BrightnessLevel.High]   → élevé (0.75f)
 * - [BrightnessLevel.Max]    → maximum (1f)
 * - [BrightnessLevel.System] → délègue au réglage système (-1 via WindowManager)
 */
data class BrightnessLevel(val value: Float) {
    init { require(value in -1f..1f) { "BrightnessLevel.value must be in -1f..1f" } }

    companion object {
        val Min    = BrightnessLevel(0f)
        val Low    = BrightnessLevel(0.25f)
        val Medium = BrightnessLevel(0.5f)
        val High   = BrightnessLevel(0.75f)
        val Max    = BrightnessLevel(1f)
        val System = BrightnessLevel(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
    }
}

// ─────────────────────────────────────────────
//  BrightnessHelper
// ─────────────────────────────────────────────

/**
 * Helper de luminosité centralisé.
 *
 * Deux portées :
 * - **Fenêtre** (sans permission) — affecte uniquement l'Activity courante.
 * - **Système** (permission `WRITE_SETTINGS` requise) — affecte tout l'appareil.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { BrightnessHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Luminosité fenêtre (pas de permission)
 * brightnessHelper.setWindowBrightness(activity, BrightnessLevel.High)
 * brightnessHelper.resetWindowBrightness(activity)
 *
 * // Luminosité système (nécessite WRITE_SETTINGS — ouvrir les réglages si refusé)
 * if (brightnessHelper.canWriteSettings(context)) {
 *     brightnessHelper.setSystemBrightness(context, BrightnessLevel.Medium)
 * } else {
 *     brightnessHelper.openWriteSettingsScreen(context)
 * }
 * ```
 */
class BrightnessHelper(context: Context) {

    internal val appContext = context.applicationContext

    // ── Window brightness (no permission needed) ──────────────────────────────

    /** Applique [level] à la fenêtre de l'[activity] uniquement. */
    fun setWindowBrightness(activity: Activity, level: BrightnessLevel) {
        val params = activity.window.attributes
        params.screenBrightness = level.value
        activity.window.attributes = params
    }

    /** Rétablit la luminosité système pour la fenêtre de l'[activity]. */
    fun resetWindowBrightness(activity: Activity) =
        setWindowBrightness(activity, BrightnessLevel.System)

    /** Luminosité courante de la fenêtre [0f..1f], ou -1f si déléguée au système. */
    fun getWindowBrightness(activity: Activity): Float =
        activity.window.attributes.screenBrightness

    // ── System brightness (WRITE_SETTINGS required) ───────────────────────────

    /** `true` si l'app peut modifier les réglages système. */
    fun canWriteSettings(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            // API 21-22 : la permission WRITE_SETTINGS dans le Manifest suffit,
            // pas de vérification runtime possible — on suppose accordée.
            true
        }

    /**
     * Applique [level] au niveau système.
     * Pré-condition : [canWriteSettings] == true.
     * [BrightnessLevel.System] est réservé à la fenêtre — utilisez [resetWindowBrightness].
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun setSystemBrightness(context: Context, level: BrightnessLevel) {
        require(level.value >= 0f) {
            "BrightnessLevel.System is window-only; do not call setSystemBrightness with it — use resetWindowBrightness instead"
        }
        require(canWriteSettings(context)) {
            "WRITE_SETTINGS permission required — call openWriteSettingsScreen first"
        }
        val raw = (level.value.coerceIn(0f, 1f) * 255).roundToInt()
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            raw
        )
    }

    /** Luminosité système courante normalisée [0f..1f]. */
    fun getSystemBrightness(context: Context): Float {
        val raw = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            128
        )
        return raw / 255f
    }

    fun openWriteSettingsScreen(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                "package:${context.packageName}".toUri()
            )
        } else {
            // API 21-22 : WRITE_SETTINGS accordée via Manifest, pas d'écran dédié.
            // On redirige vers les infos de l'app comme fallback.
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                "package:${context.packageName}".toUri()
            )
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}