package dev.kindling.android

import android.app.Activity
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

object KSecureWindow {
    
    /**
     * Active ou désactive la protection contre les captures d'écran sur une [Window].
     */
    fun apply(window: Window, enable: Boolean = true) {
        if (enable) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

/**
 * Composable utilitaire pour appliquer [FLAG_SECURE] à l'activité hôte de manière déclarative.
 */
@Composable
fun RememberSecureWindow(enable: Boolean = true) {
    val view = LocalView.current
    DisposableEffect(enable) {
        val window = (view.context as? Activity)?.window
        window?.let { KSecureWindow.apply(it, enable) }
        onDispose {
            window?.let { KSecureWindow.apply(it, false) }
        }
    }
}