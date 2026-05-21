package dev.kindling.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Provide an alias to [LayoutDirection] for Kindling APIs.
 */
typealias KTextDirection = LayoutDirection

/**
 * Override [LocalLayoutDirection] for the subtree.
 *
 * ```kotlin
 * KDirectionProvider(LayoutDirection.Rtl) { MyScreen() }
 * ```
 *
 * @param direction Direction applied to the subtree.
 * @param content Composable content that uses the provided direction.
 */
@Composable
fun KDirectionProvider(
    direction: LayoutDirection = LayoutDirection.Ltr,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
}

/**
 * Read the current [LayoutDirection] from composition.
 *
 * @return The layout direction from [LocalLayoutDirection].
 */
@Composable
fun useKDirection(): LayoutDirection = LocalLayoutDirection.current

/**
 * Manage LTR/RTL layout direction at runtime.
 *
 * ```kotlin
 * KDirectionProvider(direction = KDirectionManager.direction) { NavHost(…) }
 * // Elsewhere:
 * KButton("Toggle RTL", onClick = { KDirectionManager.toggle() })
 * ```
 */
object KDirectionManager {
    /**
     * Current global layout direction.
     */
    var direction by mutableStateOf<LayoutDirection>(LayoutDirection.Ltr)
        private set

    /**
     * Toggle between LTR and RTL layout directions.
     */
    fun toggle() {
        direction = if (direction == LayoutDirection.Ltr) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    /**
     * Set the global layout direction.
     *
     * @param dir Direction to apply.
     */
    fun set(dir: LayoutDirection) { direction = dir }

    /**
     * Set the direction based on the provided locale.
     *
     * @param locale Locale used to infer RTL or LTR.
     */
    fun setFromLocale(locale: java.util.Locale) { direction = if (isKRtlLocale(locale)) LayoutDirection.Rtl else LayoutDirection.Ltr }

    /**
     * Return `true` when the current direction is RTL.
     */
    val isRtl: Boolean get() = direction == LayoutDirection.Rtl

    /**
     * Return `true` when the current direction is LTR.
     */
    val isLtr: Boolean get() = direction == LayoutDirection.Ltr
}

/**
 * Determine if a locale is considered right-to-left.
 *
 * @param locale Locale to evaluate.
 * @return `true` when the locale is treated as RTL.
 */
fun isKRtlLocale(locale: java.util.Locale = java.util.Locale.getDefault()): Boolean {
    return locale.language.lowercase() in setOf("ar", "he", "iw", "fa", "ur", "ps", "sd", "ku", "yi", "dv")
}

@Preview(name = "KDirection — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KDirection — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKDirection() {
    KindlingPreviewSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DirectionCard(label = "LTR card", direction = LayoutDirection.Ltr, modifier = Modifier.weight(1f))
            DirectionCard(label = "RTL card", direction = LayoutDirection.Rtl, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DirectionCard(
    label: String,
    direction: LayoutDirection,
    modifier: Modifier = Modifier
) {
    KDirectionProvider(direction = direction) {
        Surface(
            modifier = modifier
                .heightIn(min = 72.dp)
                .widthIn(min = 140.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
