package dev.kindling.core.components.ui.toast

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global toast dispatcher — call from anywhere in your app.
 *
 * ```kotlin
 * KToastManager.success("Saved!")
 * KToastManager.error("Upload failed", "Please try again.")
 * KToastManager.show("Event created", actionLabel = "Undo") { /* undo */ }
 * ```
 */
object KToastManager {
    private val _flow = MutableSharedFlow<KToastData>(extraBufferCapacity = 8)
    val flow = _flow.asSharedFlow()

    fun show(
        message: String,
        description: String? = null,
        type: KToastType = KToastType.Default,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        durationMs: Long = 4_000L
    ) {
        _flow.tryEmit(
            KToastData(
                message = message,
                description = description,
                type = type,
                actionLabel = actionLabel,
                onAction = onAction,
                durationMs = durationMs
            )
        )
    }

    fun success(message: String, description: String? = null) =
        show(message, description, KToastType.Success)
    fun error(message: String, description: String? = null) =
        show(message, description, KToastType.Error)
    fun warning(message: String, description: String? = null) =
        show(message, description, KToastType.Warning)
    fun info(message: String, description: String? = null) =
        show(message, description, KToastType.Info)
}