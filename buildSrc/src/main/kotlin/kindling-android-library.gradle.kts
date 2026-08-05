/**
 * Convention plugin: kindling-android-library
 *
 * Single source of truth for all Android library modules.
 * Namespace is derived automatically from the module name:
 *   :android  → dev.kindling.android
 *   :compose  → dev.kindling.compose
 *
 * Modules only override what genuinely differs (e.g. minSdk, buildFeatures).
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
}

android {
    namespace  = "dev.kindling.${project.name}"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}