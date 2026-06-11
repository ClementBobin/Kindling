package dev.kindling.android.natif

import android.content.Context
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager

// ─────────────────────────────────────────────
//  DisplayInfo
// ─────────────────────────────────────────────

/**
 * Informations sur l'écran principal.
 *
 * @param widthPx     Largeur en pixels.
 * @param heightPx    Hauteur en pixels.
 * @param densityDpi  Densité en dpi.
 * @param density     Facteur de densité (ex. 2.0 pour xxhdpi).
 * @param refreshRate Fréquence de rafraîchissement en Hz.
 * @param orientation [Configuration.ORIENTATION_PORTRAIT] ou [Configuration.ORIENTATION_LANDSCAPE].
 */
data class DisplayInfo(
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Int,
    val density: Float,
    val refreshRate: Float,
    val orientation: Int
) {
    val widthDp: Float  get() = widthPx  / density
    val heightDp: Float get() = heightPx / density

    val isPortrait:  Boolean get() = orientation == Configuration.ORIENTATION_PORTRAIT
    val isLandscape: Boolean get() = orientation == Configuration.ORIENTATION_LANDSCAPE

    val densityBucket: String get() = when {
        densityDpi <= 120 -> "ldpi"
        densityDpi <= 160 -> "mdpi"
        densityDpi <= 240 -> "hdpi"
        densityDpi <= 320 -> "xhdpi"
        densityDpi <= 480 -> "xxhdpi"
        else              -> "xxxhdpi"
    }
}

// ─────────────────────────────────────────────
//  DisplayHelper
// ─────────────────────────────────────────────

/**
 * Helper d'affichage centralisé.
 *
 * Aucune permission requise.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { DisplayHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * val info = displayHelper.getDisplayInfo()
 * println("${info.widthDp} x ${info.heightDp} dp — ${info.densityBucket}")
 *
 * val isTablet = displayHelper.isTablet()
 * val px = displayHelper.dpToPx(16f)
 * ```
 */
class DisplayHelper(context: Context) {

    internal val appContext     = context.applicationContext
    internal val windowManager  = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    internal val displayManager = appContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    // ── Display info ──────────────────────────────────────────────────────────

    fun getDisplayInfo(): DisplayInfo {
        val metrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds  = windowManager.currentWindowMetrics.bounds
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            display.getMetrics(metrics)
            return DisplayInfo(
                widthPx     = bounds.width(),
                heightPx    = bounds.height(),
                densityDpi  = metrics.densityDpi,
                density     = metrics.density,
                refreshRate = display.refreshRate,
                orientation = appContext.resources.configuration.orientation
            )
        } else {
            val display = windowManager.defaultDisplay
            display.getRealMetrics(metrics)
            return DisplayInfo(
                widthPx     = metrics.widthPixels,
                heightPx    = metrics.heightPixels,
                densityDpi  = metrics.densityDpi,
                density     = metrics.density,
                refreshRate = display.refreshRate,
                orientation = appContext.resources.configuration.orientation
            )
        }
    }

    /** `true` si la plus petite dimension de l'écran dépasse 600dp (tablet heuristic). */
    fun isTablet(): Boolean =
        appContext.resources.configuration.smallestScreenWidthDp >= 600

    // ── Unit conversion ───────────────────────────────────────────────────────

    fun dpToPx(dp: Float): Float =
        dp * appContext.resources.displayMetrics.density

    fun pxToDp(px: Float): Float =
        px / appContext.resources.displayMetrics.density

    fun spToPx(sp: Float): Float {
        val config = appContext.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 34+ : fontScale est dans la configuration
            sp * appContext.resources.displayMetrics.density * config.fontScale
        } else {
            sp * appContext.resources.displayMetrics.scaledDensity
        }
    }

    // ── Refresh rate ──────────────────────────────────────────────────────────

    fun getSupportedRefreshRates(): FloatArray {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        } else {
            windowManager.defaultDisplay
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            display.supportedModes.map { it.refreshRate }.toFloatArray()
        } else {
            floatArrayOf(display.refreshRate)
        }
    }

    fun getCurrentRefreshRate(): Float = getDisplayInfo().refreshRate
}