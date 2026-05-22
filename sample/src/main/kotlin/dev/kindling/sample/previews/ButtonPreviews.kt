package dev.kindling.sample.previews

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.*

@Preview(name = "Button — variants (light)", showBackground = true)
@Composable
private fun ButtonVariantsLight() = PreviewSurface {
    KButton(text = "Default",     onClick = {})
    KButton(text = "Secondary",   onClick = {}, variant = KButtonVariant.Secondary)
    KButton(text = "Outline",     onClick = {}, variant = KButtonVariant.Outline)
    KButton(text = "Ghost",       onClick = {}, variant = KButtonVariant.Ghost)
    KButton(text = "Destructive", onClick = {}, variant = KButtonVariant.Destructive)
    KButton(text = "Link",        onClick = {}, variant = KButtonVariant.Link)
}

@Preview(name = "Button — variants (dark)", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ButtonVariantsDark() = PreviewSurface(dark = true) {
    KButton(text = "Default",     onClick = {})
    KButton(text = "Secondary",   onClick = {}, variant = KButtonVariant.Secondary)
    KButton(text = "Outline",     onClick = {}, variant = KButtonVariant.Outline)
    KButton(text = "Destructive", onClick = {}, variant = KButtonVariant.Destructive)
}

@Preview(name = "Button — sizes", showBackground = true)
@Composable
private fun ButtonSizes() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KButton(text = "Sm",    onClick = {}, size = KButtonSize.Sm)
        KButton(text = "Default", onClick = {})
        KButton(text = "Lg",    onClick = {}, size = KButtonSize.Lg)
    }
}

@Preview(name = "Button — states", showBackground = true)
@Composable
private fun ButtonStates() = PreviewSurface {
    KButton(text = "Enabled",  onClick = {})
    KButton(text = "Disabled", onClick = {}, enabled = false)
    KButton(text = "Loading",  onClick = {}, isLoading = true)
}