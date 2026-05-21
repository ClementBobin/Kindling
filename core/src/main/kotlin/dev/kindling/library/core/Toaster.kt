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
//  Data model
// ─────────────────────────────────────────────

enum class KToastType { Default, Success, Error, Warning, Info }

data class KToastData(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val description: String? = null,
    val type: KToastType = KToastType.Default,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val durationMs: Long = 4_000L
)

// ─────────────────────────────────────────────
//  Toaster singleton
// ─────────────────────────────────────────────

/**
 * Global toast dispatcher.
 *
 * Call from anywhere; [KToasterHost] must be present once in the composable tree.
 *
 * ```kotlin
 * KToaster.success("Saved!")
 * KToaster.error("Upload failed", "Please try again.")
 * KToaster.show("Event created", actionLabel = "Undo") { /* undo */ }
 * ```
 */
object KToaster {
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

    fun success(message: String, description: String? = null) = show(message, description, KToastType.Success)
    fun error(message: String, description: String? = null)   = show(message, description, KToastType.Error)
    fun warning(message: String, description: String? = null) = show(message, description, KToastType.Warning)
    fun info(message: String, description: String? = null)    = show(message, description, KToastType.Info)
}

// ─────────────────────────────────────────────
//  Host — place once at root
// ─────────────────────────────────────────────

/**
 * Place **once** at the root of your composable tree.
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     // … your NavHost / Scaffold …
 *     KToasterHost()
 * }
 * ```
 */
@Composable
fun KToasterHost(
    modifier: Modifier = Modifier,
    maxVisible: Int = 3
) {
    val toasts = remember { mutableStateListOf<KToastData>() }

    LaunchedEffect(Unit) {
        KToaster.flow.collect { toast ->
            toasts.add(0, toast)
            if (toasts.size > maxVisible) toasts.removeLastOrNull()
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            toasts.forEach { toast ->
                key(toast.id) {
                    ToastItem(toast = toast, onClose = { toasts.remove(toast) })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Internal items
// ─────────────────────────────────────────────

@Composable
private fun ToastItem(toast: KToastData, onClose: () -> Unit) {
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
        ToastCard(toast = toast, onClose = {
            visible = false
            onClose()
        })
    }
}

@Composable
private fun ToastCard(toast: KToastData, onClose: () -> Unit) {
    val cs = MaterialTheme.colorScheme

    // Resolve semantic colours — fall back to Material3 roles for full theme compat.
    // Success and Warning intentionally use hard-coded green/amber as M3 doesn't
    // expose those roles; callers can override via a custom KToastData extension if needed.
    data class ToastStyle(val bg: Color, val icon: Color, val text: Color)

    val style = when (toast.type) {
        KToastType.Success -> ToastStyle(
            bg   = Color(0xFF166534),
            icon = Color(0xFF4ADE80),
            text = Color(0xFFDCFCE7)
        )
        KToastType.Error   -> ToastStyle(cs.errorContainer, cs.error, cs.onErrorContainer)
        KToastType.Warning -> ToastStyle(
            bg   = Color(0xFF78350F),
            icon = Color(0xFFFBBF24),
            text = Color(0xFFFEF3C7)
        )
        KToastType.Info    -> ToastStyle(cs.primaryContainer, cs.primary, cs.onPrimaryContainer)
        KToastType.Default -> ToastStyle(cs.surface, cs.onSurface, cs.onSurface)
    }

    val icon = when (toast.type) {
        KToastType.Success -> Icons.Default.Check
        KToastType.Error   -> Icons.Default.Close
        KToastType.Warning -> Icons.Default.Warning
        KToastType.Info    -> Icons.Default.Info
        KToastType.Default -> null
    }

    Row(
        modifier = Modifier
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
            Text(toast.message, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = style.text)
            if (toast.description != null) {
                Text(toast.description, fontSize = 12.sp, color = style.text.copy(alpha = 0.8f))
            }
        }

        if (toast.actionLabel != null && toast.onAction != null) {
            TextButton(onClick = { toast.onAction.invoke(); onClose() }) {
                Text(toast.actionLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = style.icon)
            }
        }

        IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint     = style.text.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
