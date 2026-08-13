# Kindling

[![CI](https://github.com/ClementBobin/Kindling/actions/workflows/ci.yml/badge.svg)](https://github.com/ClementBobin/Kindling/actions/workflows/ci.yml)
[![](https://jitpack.io/v/ClementBobin/Kindling.svg)](https://jitpack.io/#ClementBobin/Kindling)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.clementbobin.kindling/core)](https://central.sonatype.com/search?q=io.github.clementbobin.kindling)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![API Docs](https://img.shields.io/badge/API%20docs-Dokka-orange)](https://clementbobin.github.io/Kindling/)

A production-ready Kotlin multi-module component library for Jetpack Compose — shadcn/ui-inspired UI components, typed navigation, a structured ViewModel base, and coroutine utilities, all fully theme-aware via Material3.

📖 **API reference:** https://clementbobin.github.io/Kindling/

---

## Modules

| Module | Artifact | Description | Distribution |
|--------|----------|-------------|--------------|
| `core` | `io.github.clementbobin.kindling:core` | Shadcn/ui-style Compose components (Button, Input, Dialog, Carousel, DataTable, DatePicker, Stepper, Toaster, Skeleton, Spinner, Combobox, Empty state, Pagination…) | Maven Central · JitPack |
| `utils` | `io.github.clementbobin.kindling:utils` | Coroutine utilities: `Debouncer<T>`, `Throttler<T>`, `debounceLeading`, `throttleFirst` Flow extensions | Maven Central · JitPack |
| `compose` | `io.github.clementbobin.kindling:compose` | Typed navigation (`Destination`, `KNavHost`), `KViewModel` base with state/events/data-loading helpers | Maven Central · JitPack |
| `android` | `io.github.clementbobin.kindling:android` | Android platform helpers: 30+ native device helpers (Camera, Biometric, Bluetooth, Location, NFC…), `KHttpClient` (Ktor-based with token refresh & caching), and session/token storage utilities | Maven Central · JitPack |

---

## Installation

### Gradle Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io") // fallback
}

dependencies {
    // UI components (Compose + Material3)
    implementation("io.github.clementbobin.kindling:core:0.3.0")

    // Coroutine utilities (debounce, throttle)
    implementation("io.github.clementbobin.kindling:utils:0.3.0")

    // Typed navigation + KViewModel (Android only)
    implementation("io.github.clementbobin.kindling:compose:0.3.0")

    // Android platform helpers: device APIs, HTTP client, token storage
    implementation("io.github.clementbobin.kindling:android:0.3.0")
}
```

### Gradle Groovy DSL

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'io.github.clementbobin.kindling:core:0.3.0'
    implementation 'io.github.clementbobin.kindling:utils:0.3.0'
    implementation 'io.github.clementbobin.kindling:compose:0.3.0'
    implementation 'io.github.clementbobin.kindling:android:0.3.0'
}
```

> **Note:** `:compose` and `:android` are Android-only modules.

### Requirements

- **minSdk** 26+
- **JDK** 17+
- **Kotlin** 2.2.0+
- **Compose BOM** 2025.05.00+

---

## Components — `core`

All components read colours exclusively from `MaterialTheme.colorScheme`, so they work with any light/dark scheme out of the box.

---

## Build & test

```bash
./gradlew build
./gradlew test
./gradlew :utils:test   # utils tests only
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

Branch naming: `feat/<short-description>` · `fix/<short-description>` · `chore/<short-description>`

---

## License

Licensed under the [Apache License 2.0](LICENSE).