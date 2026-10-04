import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    // 1. Apply the Android library and custom Android convention plugin first
    id("com.android.library")
    id("kindling-android-library")

    // 2. Apply Kotlin Multiplatform and compiler plugins
    kotlin("multiplatform")
    kotlin("plugin.compose")
    // KWidgetModel is @Serializable. Web targets have no reflection fallback,
    // so the compiler plugin is mandatory for JSON (de)serialization to work.
    kotlin("plugin.serialization")

    // 3. Apply KSP last among the core build plugins (after Android extension exists)
    id("com.google.devtools.ksp")

    // 4. Remaining UI, documentation, and publishing plugins
    id("org.jetbrains.compose")
    id("dokka-convention")
    id("kindling-publish")
}

android {
    namespace = "dev.kindling.core"
    compileSdk = 36
    defaultConfig {
        minSdk = 21
    }
}

kotlin {
    // Mobile
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    // Desktop
    jvm("desktop")

    // Web
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    js {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            // No explicit kotlin("stdlib"): the Kotlin Gradle plugin adds the right
            // per-target stdlib (klib for js/wasmJs) at the plugin's own version.
            implementation("org.jetbrains.compose.runtime:runtime:1.12.1")
            implementation("org.jetbrains.compose.foundation:foundation:1.12.1")
            implementation("org.jetbrains.compose.material3:material3:1.9.0")
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation("org.jetbrains.compose.ui:ui:1.12.1")
            implementation("org.jetbrains.compose.animation:animation:1.12.1")
            implementation("org.jetbrains.compose.animation:animation")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.datetime}")
            implementation("io.coil-kt.coil3:coil-compose:${Versions.coil}")
            implementation("io.insert-koin:koin-core:${Versions.koin}")
            implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:${Versions.immutableCollections}")
            implementation(project(":utils"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation("io.coil-kt.coil3:coil-network-okhttp:${Versions.coil}")
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        // Coil needs a network engine per platform to load URL images
        // (KLogoItem, avatars). Android uses OkHttp above; on the web it is Ktor's JS engine.
        jsMain.dependencies {
            implementation("io.coil-kt.coil3:coil-network-ktor3:${Versions.coil}")
            implementation("io.ktor:ktor-client-js:${Versions.ktor3}")
        }
        wasmJsMain.dependencies {
            implementation("io.coil-kt.coil3:coil-network-ktor3:${Versions.coil}")
            implementation("io.ktor:ktor-client-js:${Versions.ktor3}")
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":processor"))
}