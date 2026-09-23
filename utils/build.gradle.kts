import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kindling-android-library")
    id("dokka-convention")
    id("kindling-publish")
}

android {
    namespace = "${Versions.group}.${project.name}"
    compileSdk = 36
    defaultConfig { minSdk = 21 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget()
    jvm("desktop")

    iosX64()
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
            implementation(kotlin("stdlib"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
        }

        androidMain.dependencies {
            implementation("androidx.annotation:annotation-jvm:1.10.0")
            implementation("org.bouncycastle:bcpkix-jdk18on:${Versions.castle}")
            implementation("org.bouncycastle:bcprov-jdk18on:${Versions.castle}")
        }

        val desktopMain by getting {
            dependencies {
                implementation("org.bouncycastle:bcpkix-jdk18on:${Versions.castle}")
                implementation("org.bouncycastle:bcprov-jdk18on:${Versions.castle}")
            }
        }
    }
}