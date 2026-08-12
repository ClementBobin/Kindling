package dev.kindling.core.components.ui.avatar

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class KAvatarSize {
    Sm, Default, Lg
}

val KAvatarSize.sizeDp: Dp get() = when (this) {
    KAvatarSize.Sm      -> 24.dp
    KAvatarSize.Default -> 32.dp
    KAvatarSize.Lg      -> 40.dp
}

val KAvatarSize.fontSizeSp: Float get() = when (this) {
    KAvatarSize.Sm      -> 10f
    KAvatarSize.Default -> 12f
    KAvatarSize.Lg      -> 14f
}

val KAvatarSize.badgeSizeDp: Dp get() = when (this) {
    KAvatarSize.Sm      -> 8.dp
    KAvatarSize.Default -> 10.dp
    KAvatarSize.Lg      -> 12f.dp // or 12.dp
}