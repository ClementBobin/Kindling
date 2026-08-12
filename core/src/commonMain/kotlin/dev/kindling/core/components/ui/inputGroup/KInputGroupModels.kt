package dev.kindling.core.components.ui

import androidx.compose.ui.focus.FocusRequester

enum class KInputGroupAlign { InlineStart, InlineEnd, BlockStart, BlockEnd }

open class KInputGroupScope internal constructor(
    internal val focusRequester: FocusRequester
)