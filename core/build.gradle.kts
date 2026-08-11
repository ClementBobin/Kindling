plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("dokka-convention")
    id("kindling-android-library")
    id("kindling-publish")
}

kotlin {
    jvm("desktop")

    android {
        namespace = "${Versions.group}.${project.name}"
        compileSdk { version = release(36) }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.animation)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
                implementation("io.coil-kt.coil3:coil-compose:${Versions.coil}")
                implementation("io.coil-kt.coil3:coil-network-okhttp:${Versions.coil}")
                implementation(kotlin("stdlib"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":utils"))
            }
        }
    }
}
