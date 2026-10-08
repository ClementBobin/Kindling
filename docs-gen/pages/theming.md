---
title: Theming
description: Wrap your app in KindlingTheme to customise shapes, colours and typography.
section: Getting started
order: 3
---

`KindlingTheme` wraps `MaterialTheme` and provides two extra composition locals, `KindlingShapes` and `KindlingColors`, so every Kindling component can read them without parameter threading. Call it once at the top of your composition, exactly like `MaterialTheme { … }`.

```kotlin
// Minimal: library defaults (base radius = 10.dp, M3-derived chart colours)
KindlingTheme {
    Scaffold { /* … */ }
}

// Custom corner radius + branded chart palette
KindlingTheme(
    colorScheme = myDarkColorScheme,
    shapes = KindlingShapes(base = 4.dp),
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
{{props:shapes|KindlingShapes|KindlingShapes() // base = 10.dp|Kindling shape tokens.}}
{{props:colors|KindlingColors|KindlingColors.fromMaterial3()|Five-slot chart palette. Dark/light mode is handled automatically when omitted.}}
{{props:content*|@Composable () -> Unit||The composition subtree.}}

## Reading tokens in your own code

```kotlin
val shapes = MaterialTheme.kindlingShapes
val colors = MaterialTheme.kindlingColors
```
