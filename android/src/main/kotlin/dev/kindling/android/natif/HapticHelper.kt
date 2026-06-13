package dev.kindling.android.natif

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

// ─────────────────────────────────────────────
//  HapticEffect
// ─────────────────────────────────────────────

/**
 * Décrit un effet haptique sémantique via [HapticFeedbackConstants].
 *
 * Distincts de [VibrationPattern] : ces effets s'appuient sur les primitives
 * haptiques système (calibrées par le constructeur), pas sur des waveforms manuelles.
 *
 * Presets :
 * - [HapticEffect.Click]        → feedback de clic standard
 * - [HapticEffect.LongPress]    → feedback d'appui long
 * - [HapticEffect.DoubleClick]  → feedback de double-clic (API 27+)
 * - [HapticEffect.HeavyClick]   → clic lourd (API 23+)
 * - [HapticEffect.Tick]         → tick léger (API 21+)
 * - [HapticEffect.Confirm]      → confirmation (API 30+)
 * - [HapticEffect.Reject]       → rejet (API 30+)
 * - [HapticEffect.TextHandleMove] → déplacement curseur texte (API 27+)
 *
 * Personnalisé (constante arbitraire) :
 * ```kotlin
 * val effect = HapticEffect(HapticFeedbackConstants.KEYBOARD_TAP)
 * hapticHelper.perform(view, effect)
 * ```
 */
data class HapticEffect(val constant: Int) {
    companion object {
        val Click           = HapticEffect(HapticFeedbackConstants.VIRTUAL_KEY)
        val LongPress       = HapticEffect(HapticFeedbackConstants.LONG_PRESS)
        val DoubleClick     = HapticEffect(
            if (Build.VERSION.SDK_INT >= 27) HapticFeedbackConstants.VIRTUAL_KEY_RELEASE
            else HapticFeedbackConstants.VIRTUAL_KEY
        )
        val HeavyClick = HapticEffect(
            if (Build.VERSION.SDK_INT >= 23) HapticFeedbackConstants.CONTEXT_CLICK
            else HapticFeedbackConstants.VIRTUAL_KEY
        )
        val Tick = HapticEffect(
            if (Build.VERSION.SDK_INT >= 21) HapticFeedbackConstants.CLOCK_TICK
            else HapticFeedbackConstants.VIRTUAL_KEY
        )
        val Confirm         = HapticEffect(
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
            else HapticFeedbackConstants.VIRTUAL_KEY
        )
        val Reject          = HapticEffect(
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT
            else HapticFeedbackConstants.LONG_PRESS
        )
        val TextHandleMove  = HapticEffect(
            if (Build.VERSION.SDK_INT >= 27) HapticFeedbackConstants.TEXT_HANDLE_MOVE
            else HapticFeedbackConstants.VIRTUAL_KEY
        )
    }
}

// ─────────────────────────────────────────────
//  HapticHelper
// ─────────────────────────────────────────────

/**
 * Helper haptique centralisé basé sur [HapticFeedbackConstants].
 *
 * Complémentaire à [VibrationHelper] : utilise les retours haptiques calibrés
 * par le constructeur plutôt que des waveforms manuelles — préférable pour les
 * interactions UI standard.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { HapticHelper() }
 * ```
 *
 * Utilisation (Compose — récupérer la view racine) :
 * ```kotlin
 * val view = LocalView.current
 * hapticHelper.perform(view, HapticEffect.Click)
 * ```
 *
 * La vue doit avoir `android:hapticFeedbackEnabled="true"` (défaut système).
 */
class HapticHelper {

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Joue l'[effect] sur la [view] fournie.
     *
     * @param ignoreViewSetting `true` (défaut) → joue le feedback même si la view
     *   a désactivé le haptic via `android:hapticFeedbackEnabled="false"`.
     *   Passer `false` pour respecter les préférences utilisateur et système
     *   (recommandé pour les feedbacks discrets comme [HapticEffect.Tick] ou
     *   [HapticEffect.TextHandleMove]).
     */
    fun perform(view: View, effect: HapticEffect, ignoreViewSetting: Boolean = true) {
        val flags = if (ignoreViewSetting) HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING else 0
        view.performHapticFeedback(effect.constant, flags)
    }

    // ── Convenience shorthands ────────────────────────────────────────────────

    fun click(view: View)          = perform(view, HapticEffect.Click)
    fun longPress(view: View)      = perform(view, HapticEffect.LongPress)
    fun doubleClick(view: View)    = perform(view, HapticEffect.DoubleClick)
    fun heavyClick(view: View)     = perform(view, HapticEffect.HeavyClick)
    fun tick(view: View)           = perform(view, HapticEffect.Tick,           ignoreViewSetting = false)
    fun confirm(view: View)        = perform(view, HapticEffect.Confirm)
    fun reject(view: View)         = perform(view, HapticEffect.Reject)
    fun textHandleMove(view: View) = perform(view, HapticEffect.TextHandleMove, ignoreViewSetting = false)
}