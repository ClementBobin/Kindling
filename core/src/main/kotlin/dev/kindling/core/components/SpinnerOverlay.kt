package dev.kindling.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.internal.KindlingPreviewSurface

/**
 * Render a centred spinner that fills its parent.
 *
 * ```kotlin
 * if (isLoading) KSpinnerOverlay()
 * ```
 *
 * @param modifier Applied to the outermost layout element.
 * @param size Size preset for the spinner.
 * @param label Optional label shown beneath the spinner.
 */
@Composable
fun KSpinnerOverlay(
    modifier: Modifier = Modifier.fillMaxSize(),
    size: KSpinnerSize = KSpinnerSize.Lg,
    label: String? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        KSpinner(size = size, label = label)
    }
}

@Preview(name = "KSpinnerOverlay — light", showBackground = true, widthDp = 360)
@Preview(
    name = "KSpinnerOverlay — dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewKSpinnerOverlay() {
    KindlingPreviewSurface {
        Surface(modifier = Modifier.size(200.dp)) {
            KSpinnerOverlay()
        }
    }
}
