---
title: Installation
description: Add Kindling to your project with Gradle.
section: Getting started
order: 2
---

## Requirements

- **minSdk** 26+
- **JDK** 17+
- **Kotlin** 2.2.0+
- **Compose BOM** 2025.05.00+

## Add the dependencies

Kotlin DSL:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io") // fallback
}

dependencies {
    // UI components (Compose + Material3)
    implementation("io.github.clementbobin.kindling:core:{{version}}")

    // Coroutine utilities (debounce, throttle)
    implementation("io.github.clementbobin.kindling:utils:{{version}}")

    // Typed navigation + KViewModel (Android only)
    implementation("io.github.clementbobin.kindling:compose:{{version}}")

    // Android platform helpers: device APIs, HTTP client, token storage
    implementation("io.github.clementbobin.kindling:android:{{version}}")
}
```

Groovy DSL:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'io.github.clementbobin.kindling:core:{{version}}'
    implementation 'io.github.clementbobin.kindling:utils:{{version}}'
    implementation 'io.github.clementbobin.kindling:compose:{{version}}'
    implementation 'io.github.clementbobin.kindling:android:{{version}}'
}
```

> **Note:** `:compose` and `:android` are Android-only modules. `:core` and `:utils` also target iOS, desktop and web.

## Wrap your app in the theme

```kotlin
KindlingTheme {
    // your screens
}
```

Continue with [Theming](/docs/theming) to customise radii and the chart palette.
