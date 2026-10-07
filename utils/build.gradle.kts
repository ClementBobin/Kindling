import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    kotlin("multiplatform")
    // Same Android plugin as :core. With `com.android.library` the kindling-publish
    // convention would switch to single-variant Android publishing and the
    // iOS/desktop/web artifacts would never be published.
    id("com.android.kotlin.multiplatform.library")
    id("kindling-android-library")
    id("kindling-publish")
}

kotlin {
    androidLibrary {
        namespace = "${KindlingProperties.group}.${project.name}"
        compileSdk = 36
        minSdk = 21
    }
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

    // Default hierarchy + one extra shared source set for code that needs the JCA /
    // Bouncy Castle (KEncrypt): it is shared by Android and desktop only.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmShared") {
                withCompilations {
                    val platform = it.target.platformType
                    platform == KotlinPlatformType.jvm || platform == KotlinPlatformType.androidJvm
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        // The jvmShared source set is created by the hierarchy template above, so it is
        // configured lazily by name (it may not exist yet when this block runs).
        configureEach {
            if (name == "jvmSharedMain") {
                dependencies {
                    implementation(libs.bouncycastle.bcpkix)
                    implementation(libs.bouncycastle.bcprov)
                }
            }
        }
    }
}
