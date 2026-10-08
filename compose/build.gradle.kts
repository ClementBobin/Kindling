import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
    id("kindling-android-library")
    id("kindling-publish")
}

extra["pomDescription"] = "Type-safe navigation and ViewModel utilities for Kindling"

kotlin {
    androidLibrary {
        namespace = "${KindlingProperties.group}.${project.name}"
        compileSdk = 36
        minSdk = 26
    }
    jvm("desktop")

    // iosX64 is not offered: Compose Multiplatform 1.11 dropped Apple x86_64 targets.
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    js {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)

            // Part of the public API (NavController, ViewModel), so exposed as `api`.
            api(libs.navigation.compose)
            api(libs.lifecycle.viewmodel.compose)

            // Multiplatform back handling (replaces androidx.activity BackHandler).
            implementation(libs.navigationevent.compose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            // Android-only KViewModel (AndroidViewModel + Koin) and the @KPreview annotation.
            api(libs.koin.core)
            implementation(libs.androidx.compose.ui.tooling.preview)
        }
    }
}
