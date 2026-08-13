# Package docs.start
# Module kindling

Getting started with Kindling — a Kotlin multi-module component library for Jetpack Compose.

## Prerequisites

- JDK 17+
- Gradle 8+
- minSdk 26+ (Android)

## Add the dependency

Use Maven Central first, then JitPack as a fallback for `core` and `utils`.

### Gradle Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io") // fallback for core and utils
}

dependencies {
    // UI components (Compose + Material3)
    implementation("io.github.clementbobin.kindling:core:0.3.0")

    // Coroutine utilities (debounce, throttle, sorting, formatting)
    implementation("io.github.clementbobin.kindling:utils:0.3.0")

    // Typed navigation + KViewModel (Android only, Maven Central)
    implementation("io.github.clementbobin.kindling:compose:0.3.0")

    // Android platform helpers: device APIs, HTTP client, token storage (Maven Central)
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

> `:compose` and `:android` are Android-only modules published to Maven Central only.
> `:core` and `:utils` are available on both Maven Central and JitPack.

## Build and test

```bash
./gradlew build
./gradlew test
./gradlew :utils:test   # single module
```