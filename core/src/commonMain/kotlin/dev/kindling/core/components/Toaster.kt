package dev.kindling.core.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// ─────────────────────────────────────────────
//  Toast  (single toast item)
// ─────────────────────────────────────────────

/**
 * Renders a single toast notification.
 *
 * Mirrors the individual toast card from shadcn/ui `sonner.tsx`.
 * Typically not used directly — prefer [Toaster] + [KToastManager].
 *
 * ```kotlin
 * Toast(
 *     data     = KToastData(message = "Saved!", type = KToastType.Success),
 *     onClose  = { }
 * )
 * ```
 */
@Composable
fun Toast(
    data: KToastData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    data class ToastStyle(val bg: Color, val icon: Color, val text: Color)

    val style = when (data.type) {
        KToastType.Success -> ToastStyle(Color(0xFF166534), Color(0xFF4ADE80), Color(0xFFDCFCE7))
        KToastType.Error   -> ToastStyle(cs.errorContainer, cs.error, cs.onErrorContainer)
        KToastType.Warning -> ToastStyle(Color(0xFF78350F), Color(0xFFFBBF24), Color(0xFFFEF3C7))
        KToastType.Info    -> ToastStyle(cs.primaryContainer, cs.primary, cs.onPrimaryContainer)
        KToastType.Default -> ToastStyle(cs.surface, cs.onSurface, cs.onSurface)
    }

    val icon = when (data.type) {
        KToastType.Success -> Icons.Default.Check
        KToastType.Error   -> Icons.Default.Close
        KToastType.Warning -> Icons.Default.Warning
        KToastType.Info    -> Icons.Default.Info
        KToastType.Default -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(style.bg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = style.icon, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(data.message, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = style.text)
            if (data.description != null) {
                Text(data.description, fontSize = 12.sp, color = style.text.copy(alpha = .8f))
            }
        }

        if (data.actionLabel != null && data.onAction != null) {
            TextButton(onClick = { data.onAction.invoke(); onClose() }) {
                Text(data.actionLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = style.icon)
            }
        }

        // Close button — uses KButton
        KButton(
            onClick  = onClose,
            variant  = KButtonVariant.Ghost,
            size     = KButtonSize.IconXs,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint     = style.text.copy(alpha = .7f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Toaster  (host — place once at root)
// ─────────────────────────────────────────────

/**
 * Places the toast stack in your composable tree.
 *
 * Place **once** at the root, typically overlaid on your Scaffold / NavHost.
 * Call [KToastManager.show] / [KToastManager.success] etc. from anywhere.
 *
 * Mirrors shadcn/ui `Toaster` backed by Sonner.
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     NavHost(…)
 *     Toaster()
 * }
 * ```
 */
@Composable
fun Toaster(
    modifier: Modifier = Modifier,
    maxVisible: Int = 3,
    alignment: Alignment = Alignment.BottomCenter
) {
    val toasts = remember { mutableStateListOf<KToastData>() }

    LaunchedEffect(Unit) {
        KToastManager.flow.collect { toast ->
            toasts.add(0, toast)
            if (toasts.size > maxVisible) toasts.removeLastOrNull()
        }
    }

    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            toasts.forEach { toast ->
                key(toast.id) {
                    ToastAnimatedItem(toast = toast, onClose = { toasts.remove(toast) })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  KToastManager  (global dispatcher)
// ─────────────────────────────────────────────

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
                message     = message,
                description = description,
                type        = type,
                actionLabel = actionLabel,
                onAction    = onAction,
                durationMs  = durationMs
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

// ─────────────────────────────────────────────
//  Internal animated wrapper
// ─────────────────────────────────────────────

@Composable
private fun ToastAnimatedItem(toast: KToastData, onClose: () -> Unit) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(toast.id) {
        delay(toast.durationMs)
        visible = false
        delay(300)
        onClose()
    }

    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
        exit    = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
    ) {
        Toast(
            data    = toast,
            onClose = { onClose() }
        )
    }
}