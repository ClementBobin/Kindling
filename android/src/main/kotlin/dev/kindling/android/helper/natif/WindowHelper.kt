package dev.kindling.android.helper.natif

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// ─────────────────────────────────────────────
//  WindowConfig
// ─────────────────────────────────────────────

/**
 * Décrit la configuration d'affichage de la fenêtre.
 *
 * Presets :
 * - [WindowConfig.Immersive]   → masque barres système (jeux, lecteurs)
 * - [WindowConfig.EdgeToEdge]  → contenu plein écran avec insets gérés par l'app
 * - [WindowConfig.Normal]      → affichage standard avec barres visibles
 */
data class WindowConfig(
    val hideStatusBar: Boolean     = false,
    val hideNavBar: Boolean        = false,
    val lightStatusBarIcons: Boolean? = null,
    val lightNavBarIcons: Boolean? = null
) {
    companion object {
        val Normal     = WindowConfig()
        val Immersive  = WindowConfig(hideStatusBar = true, hideNavBar = true)
        val EdgeToEdge = WindowConfig(hideStatusBar = false, hideNavBar = false)
    }
}

// ─────────────────────────────────────────────
//  WindowHelper
// ─────────────────────────────────────────────

/**
 * Helper de gestion de fenêtre centralisé.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { WindowHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * windowHelper.apply(activity, WindowConfig.Immersive)
 * windowHelper.apply(activity, WindowConfig.EdgeToEdge)
 *
 * // Icônes sombres sur barre de statut claire
 * windowHelper.setStatusBarIconsLight(activity, light = true)
 *
 * // Activer edge-to-edge (à appeler avant setContentView)
 * windowHelper.enableEdgeToEdge(activity)
 * ```
 */
class WindowHelper(context: Context) {

    internal val appContext = context.applicationContext

    // ── Public API ────────────────────────────────────────────────────────────

    fun apply(activity: Activity, config: WindowConfig) {
        val window = activity.window
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        if (config.hideStatusBar || config.hideNavBar) {
            val types = buildList {
                if (config.hideStatusBar) add(WindowInsetsCompat.Type.statusBars())
                if (config.hideNavBar)    add(WindowInsetsCompat.Type.navigationBars())
            }.reduce { a, b -> a or b }

            controller.hide(types)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(
                WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
            )
        }

        config.lightStatusBarIcons?.let { controller.isAppearanceLightStatusBars = it }
        config.lightNavBarIcons?.let    { controller.isAppearanceLightNavigationBars = it }
    }

    /** Déclenche le mode edge-to-edge — à appeler avant `setContentView`. */
    fun enableEdgeToEdge(activity: Activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    }

    fun setStatusBarIconsLight(activity: Activity, light: Boolean) {
        WindowInsetsControllerCompat(activity.window, activity.window.decorView)
            .isAppearanceLightStatusBars = light
    }

    fun setNavBarIconsLight(activity: Activity, light: Boolean) {
        WindowInsetsControllerCompat(activity.window, activity.window.decorView)
            .isAppearanceLightNavigationBars = light
    }

    /** `true` si la barre de statut est actuellement visible. */
    @RequiresApi(Build.VERSION_CODES.M)
    fun isStatusBarVisible(activity: Activity): Boolean {
        val insets =
            activity.window.decorView.rootWindowInsets ?: return true
        return WindowInsetsCompat.toWindowInsetsCompat(insets)
            .isVisible(WindowInsetsCompat.Type.statusBars())
    }
}