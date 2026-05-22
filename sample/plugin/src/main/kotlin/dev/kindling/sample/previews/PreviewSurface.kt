package dev.kindling.sample.previews

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Wraps preview content in a themed [Surface] with consistent padding and spacing.
 * Use this in every `@Preview` composable in the sample module.
 *
 * @param dark When true, applies [darkColorScheme].
 */
@Composable
internal fun PreviewSurface(
    dark: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}