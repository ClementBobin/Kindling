# my-kotlin-library

[![CI](https://github.com/ClementBobin/Kindling/actions/workflows/ci.yml/badge.svg)](https://github.com/ClementBobin/Kindling/actions/workflows/ci.yml) ![Maven Central Version](https://img.shields.io/maven-central/v/io.github.clementbobin.kindling/utils) ![JitPack Version](https://img.shields.io/jitpack/v/github/dev/kindling) ![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

## Overview

`my-kotlin-library` is a production-ready Kotlin multi-module component library scaffold with publishing support for Maven Central and JitPack fallback.

## Installation

### Gradle Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("io.github.clementbobin.kindling:core:0.1.0")
    implementation("io.github.clementbobin.kindling:utils:0.1.0")
}
```

### Gradle Groovy DSL

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'dev.kindling:my-kotlin-library-core:0.1.0-SNAPSHOT' // TODO: replace with your actual value
}
```

## Quick start

```kotlin
fun main() {
    // TODO: add usage example
    println("Hello from my-kotlin-library")
}
```

## Modules

| Module | Artifact ID | Description |
| --- | --- | --- |
| `core` | `dev.kindling:my-kotlin-library-core` | Core library functionality. |
| `utils` | `dev.kindling:my-kotlin-library-utils` | Optional utility helpers. |

<!-- TODO: replace with your actual value -->

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE).

## Publishing secrets

The `publish` workflow requires these repository secrets:

- `OSSRH_USERNAME`
- `OSSRH_PASSWORD`
- `GPG_KEY_ID`
- `GPG_KEY`
- `GPG_PASSPHRASE`
