package dev.kindling.showcase

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// shadcn/ui "neutral" palette, so the live previews match the docs site.

val ShadcnLight = lightColorScheme(
    primary = Color(0xFF171717),
    onPrimary = Color(0xFFFAFAFA),
    secondary = Color(0xFFF5F5F5),
    onSecondary = Color(0xFF171717),
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = Color(0xFF171717),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0A0A0A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF737373),
    outline = Color(0xFFE5E5E5),
    outlineVariant = Color(0xFFE5E5E5),
    error = Color(0xFFE7000B),
    onError = Color(0xFFFAFAFA),
)

val ShadcnDark = darkColorScheme(
    primary = Color(0xFFE5E5E5),
    onPrimary = Color(0xFF171717),
    secondary = Color(0xFF262626),
    onSecondary = Color(0xFFFAFAFA),
    secondaryContainer = Color(0xFF262626),
    onSecondaryContainer = Color(0xFFFAFAFA),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFFAFAFA),
    surface = Color(0xFF171717),
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFFA1A1A1),
    outline = Color(0x1AFFFFFF),
    outlineVariant = Color(0x26FFFFFF),
    error = Color(0xFFFF6467),
    onError = Color(0xFF0A0A0A),
)
