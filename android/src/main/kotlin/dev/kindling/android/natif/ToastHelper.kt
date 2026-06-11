package dev.kindling.android.natif

import android.content.Context
import android.widget.Toast

// ─────────────────────────────────────────────
//  ToastStyle
// ─────────────────────────────────────────────

/**
 * Décrit un style de toast sémantique.
 *
 * Presets :
 * - [ToastStyle.Short]  → durée courte  ([Toast.LENGTH_SHORT])
 * - [ToastStyle.Long]   → durée longue  ([Toast.LENGTH_LONG])
 */
data class ToastStyle(val duration: Int) {
    companion object {
        val Short = ToastStyle(Toast.LENGTH_SHORT)
        val Long  = ToastStyle(Toast.LENGTH_LONG)
    }
}

// ─────────────────────────────────────────────
//  ToastHelper
// ─────────────────────────────────────────────

/**
 * Helper toast centralisé.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { ToastHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * toastHelper.show("Copié !", ToastStyle.Short)
 * toastHelper.info("Chargement en cours…")
 * toastHelper.success("Abonnement activé")
 * toastHelper.warning("Connexion instable")
 * toastHelper.error("Échec de la requête")
 * ```
 *
 * Doit être appelé depuis le thread principal.
 */
class ToastHelper(context: Context) {

    internal val appContext = context.applicationContext

    // ── Public API ────────────────────────────────────────────────────────────

    fun show(message: String, style: ToastStyle = ToastStyle.Short) {
        Toast.makeText(appContext, message, style.duration).show()
    }

    // ── Semantic shorthands ───────────────────────────────────────────────────

    fun info(message: String)    = show(message, ToastStyle.Short)
    fun success(message: String) = show(message, ToastStyle.Short)
    fun warning(message: String) = show(message, ToastStyle.Long)
    fun error(message: String)   = show(message, ToastStyle.Long)
}