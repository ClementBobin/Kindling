package dev.kindling.core.components.ui.inputGroup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import dev.kindling.core.theme.LocalKindlingShapes
import dev.kindling.core.theme.kindlingBorder

/**
 * Shadcn/ui-style InputGroup — mirrors `input-group.tsx`.
 *
 * A single shared border wraps the field and any inline/block addons.
 * Highlights the border on focus; applies error styling when [isError] = true.
 * Respects [androidx.compose.ui.platform.LocalLayoutDirection] for RTL.
 *
 * ```kotlin
 * InputGroup {
 *     InputGroupAddon(align = KInputGroupAlign.InlineStart) {
 *         Icon(Icons.Default.Search, null)
 *     }
 *     InputGroupInput(value = query, onValueChange = { query = it }, placeholder = "Search…")
 * }
 * ```
 */
@Composable
fun InputGroup(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    content: @Composable KInputGroupScope.() -> Unit
) {
    val shapes = LocalKindlingShapes.current
    val shape = shapes.radiusXl
    val cs             = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val scope          = remember(focusRequester) { KInputGroupScope(focusRequester) }
    var focused        by remember { mutableStateOf(false) }

    val borderColor = when {
        isError -> cs.error
        focused -> cs.primary
        else    -> cs.outline
    }
    val borderWidth = if (focused || isError) shapes.borderWidthThick else shapes.borderWidth

    Column(
        modifier = modifier
            .fillMaxWidth()
            .kindlingBorder(borderWidth, borderColor, shape)
    ) {
        scope.content()
    }
}