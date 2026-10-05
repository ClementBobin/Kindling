package dev.kindling.core.components.ui.toast

import dev.kindling.utils.method.throttleFirst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.time.Duration

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

    /**
     * Minimum delay between two toasts reaching the screen. Toasts emitted while the
     * window is still open are dropped (leading-edge throttle, see [throttleFirst]).
     * [Duration.ZERO] (default) disables the limit.
     *
     * Set it **before** [KToaster] starts collecting (Application / first composition):
     * the value is read once, when the toaster subscribes.
     *
     * ```kotlin
     * KToastManager.throttle = 500.milliseconds
     * ```
     */
    var throttle: Duration = Duration.ZERO

    /** What [KToaster] actually collects: [flow], throttled according to [throttle]. */
    internal val displayFlow: Flow<KToastData>
        get() = if (throttle > Duration.ZERO) flow.throttleFirst(throttle) else flow

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