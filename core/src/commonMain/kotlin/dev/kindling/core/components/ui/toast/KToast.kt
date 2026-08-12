package dev.kindling.core.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class ToastStyle(val bg: Color, val icon: Color, val text: Color)

/**
 * Renders a single toast notification.
 *
 * Mirrors the individual toast card from shadcn/ui `sonner.tsx`.
 * Typically not used directly — prefer [Toaster] + [KToastManager].
 *
 * ```kotlin
 * Toast(
 *     data    = KToastData(message = "Saved!", type = KToastType.Success),
 *     onClose = { }
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