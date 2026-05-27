# Getting Started

This guide helps you start using Kindling quickly.

## Prerequisites

- JDK 17+
- Gradle 8+
- minSdk 26+ (Android)

## Add the dependency

Use Maven Central first, then JitPack as a fallback if needed.

### Gradle Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io") // fallback for core and utils
}

dependencies {
    implementation("io.github.clementbobin.kindling:core:0.3.0")
    implementation("io.github.clementbobin.kindling:utils:0.3.0")
    implementation("io.github.clementbobin.kindling:compose:0.3.0")
}
```

## Build and test

```bash
./gradlew build
./gradlew test
```
