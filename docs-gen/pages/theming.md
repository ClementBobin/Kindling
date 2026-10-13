---
title: Theming
description: Wrap your app in KindlingTheme to customise corner radius, border width, colours and typography.
section: Getting started
order: 3
---

`KindlingTheme` wraps `MaterialTheme` and provides two extra composition locals, `KindlingShapes` and `KindlingColors`, so every Kindling component can read them without parameter threading. Call it once at the top of your composition, exactly like `MaterialTheme { … }`.

```kotlin
// Minimal: library defaults (base radius = 10.dp, border width = 1.dp, M3-derived chart colours)
KindlingTheme {
    Scaffold { /* … */ }
}

// Custom corner radius + border width + branded chart palette
KindlingTheme(
    colorScheme = myDarkColorScheme,
    shapes = KindlingShapes(base = 4.dp, borderWidth = 2.dp),
    colors = KindlingColors(
        chart1 = Color(0xFFFF6B35),
        chart2 = Color(0xFF004E89),
        chart3 = Color(0xFF1A936F),
        chart4 = Color(0xFFC6AC8F),
        chart5 = Color(0xFF5C4742),
    ),
) { /* … */ }
```

## Parameters

{{props:colorScheme|ColorScheme|MaterialTheme.colorScheme|Material3 color scheme. Defaults to the enclosing MaterialTheme, so you can nest KindlingTheme inside one.}}
{{props:typography|Typography|MaterialTheme.typography|Material3 typography.}}
{{props:shapes|KindlingShapes|KindlingShapes() // base = 10.dp, borderWidth = 1.dp|Kindling shape tokens: corner radius and border width.}}
{{props:colors|KindlingColors|KindlingColors.fromMaterial3()|Five-slot chart palette. Dark/light mode is handled automatically when omitted.}}
{{props:content*|@Composable () -> Unit||The composition subtree.}}

## Shape tokens

`KindlingShapes` holds the two tokens that restyle every component's outline, the same way `--radius` and the `border` utility do in shadcn/ui.

{{props:base|Dp|10.dp|Global corner radius. Every rounded slot (`radiusSm`, `radiusXl`, …) is derived from it.}}
{{props:borderWidth|Dp|1.dp|Global border width for every component outline (buttons, inputs, badges, tables, popovers, dropzones…). `0.dp` turns borders and border-like dividers off.}}

```kotlin
// Heavier borders everywhere
KindlingTheme(shapes = KindlingShapes(borderWidth = 2.dp)) { /* … */ }

// Flat, borderless UI
KindlingTheme(shapes = KindlingShapes(base = 12.dp, borderWidth = 0.dp)) { /* … */ }
```

Components never hardcode a border thickness. Besides `borderWidth` they read two widths derived from it, so the visual hierarchy survives any value:

- `borderWidth` (1×): resting outlines on buttons, inputs, badges, tables, popovers and dropzones.
- `borderWidthThin` (0.5×): subtle separators such as the card and dialog footer rules.
- `borderWidthThick` (2×): focus, active and error emphasis, such as a focused input, the active OTP slot, a combobox in error or a dropzone being dragged over.

Pure decoration is left alone: the background-coloured rings that separate overlapping avatars, the stepper connector line, and chart strokes.

> Material's own `RadioButton`, `Checkbox` and `Switch` (used inside the consent and questionnaire components) draw their own outlines, so they don't follow `borderWidth`.

## Reading tokens in your own code

```kotlin
val shapes = MaterialTheme.kindlingShapes
val colors = MaterialTheme.kindlingColors

// Borders that follow the theme (kindlingBorder draws nothing when the width is 0.dp)
Box(
    Modifier.kindlingBorder(shapes.borderWidth, MaterialTheme.colorScheme.outline, shapes.radius)
)
Surface(border = shapes.borderStroke(MaterialTheme.colorScheme.outline)) { /* … */ }
```
