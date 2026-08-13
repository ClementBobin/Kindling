package dev.kindling.core.components.ui.inputGroup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.KButton
import dev.kindling.core.components.ui.KButtonSize
import dev.kindling.core.components.ui.KButtonVariant

/**
 * Addon container placed inline or as a block above/below the input.
 */
@Composable
fun KInputGroupScope.InputGroupAddon(
    modifier: Modifier = Modifier,
    align: KInputGroupAlign = KInputGroupAlign.InlineStart,
    content: @Composable () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val padding = when (align) {
        KInputGroupAlign.InlineStart -> PaddingValues(start = 8.dp)
        KInputGroupAlign.InlineEnd   -> PaddingValues(end = 8.dp)
        KInputGroupAlign.BlockStart  -> PaddingValues(start = 10.dp, top = 8.dp, bottom = 4.dp)
        KInputGroupAlign.BlockEnd    -> PaddingValues(start = 10.dp, top = 4.dp, bottom = 8.dp)
    }
    Box(
        modifier         = modifier.padding(padding),
        contentAlignment = Alignment.Center
    ) {
        ProvideTextStyle(
            LocalTextStyle.current.copy(fontSize = 14.sp, color = cs.onSurfaceVariant)
        ) { content() }
    }
}

/**
 * Ghost-style icon button sized to sit flush inside an [InputGroup] addon.
 * Uses [dev.kindling.core.components.ui.KButton] with [dev.kindling.core.components.ui.KButtonVariant.Ghost].
 */
@Composable
fun KInputGroupScope.InputGroupButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KButtonVariant = KButtonVariant.Ghost,
    size: KButtonSize = KButtonSize.Xs,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    KButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        content = content
    )
}

/** Plain muted text shown inside an [InputGroup] addon. */
@Composable
fun KInputGroupScope.InputGroupText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = text,
        fontSize = 14.sp,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}