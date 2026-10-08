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
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3.core)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.animation)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coil.compose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.collections.immutable)
            implementation(project(":utils"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.coil.network.okhttp)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        // Coil needs a network engine per platform to load URL images
        // (KLogoItem, avatars). Android uses OkHttp above; on the web it is Ktor's JS engine.
        jsMain.dependencies {
            implementation(libs.coil.network.ktor3)
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.coil.network.ktor3)
            implementation(libs.ktor.client.js)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":processor"))
}