package dev.kindling.android.natif

import android.content.Context
import android.widget.Toast

// ─────────────────────────────────────────────
//  ToastStyle
// ─────────────────────────────────────────────

/**
 * Describes a semantic toast style, determining how long the message is displayed.
 *
 * Use the provided presets:
 * - [ToastStyle.Short] -> 2 seconds ([Toast.LENGTH_SHORT])
 * - [ToastStyle.Long]  -> 3.5 seconds ([Toast.LENGTH_LONG])
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
 * Centralized Toast helper for Android.
 *
 * Simplifies showing standard Android toasts with semantic shorthands (info, success, etc.).
 *
 * **Important:** All calls must be made from the main (UI) thread.
 *
 * ### Example usage:
 * ```kotlin
 * val toastHelper = ToastHelper(context)
 * 
 * toastHelper.show("Copied to clipboard!", ToastStyle.Short)
 * toastHelper.success("Profile updated")
 * toastHelper.error("Connection failed")
 * ```
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