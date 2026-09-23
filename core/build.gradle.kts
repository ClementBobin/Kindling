import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.google.devtools.ksp")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("dokka-convention")
    id("kindling-publish")
}

android {
    namespace = "${Versions.group}.${project.name}"
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
    // Mobile
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Desktop
    jvm("desktop")

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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                implementation("io.coil-kt.coil3:coil-compose:${Versions.coil}")
                implementation("io.insert-koin:koin-core:${Versions.koin}")
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:${Versions.immutableCollections}")
                implementation(project(":utils"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        // Android-only deps (OkHttp, etc.)
        val androidMain by getting {
            dependencies {
                implementation("io.coil-kt.coil3:coil-network-okhttp:${Versions.coil}")
            }
        }

        // Desktop
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":processor"))
}

tasks.matching {
    it.name == "androidSourcesJar" || it.name == "sourcesJar"
}.configureEach {
    val kspTask = project.tasks.findByName("kspCommonMainKotlinMetadata")
    if (kspTask != null) dependsOn(kspTask)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        val kspTask = project.tasks.findByName("kspCommonMainKotlinMetadata")
        if (kspTask != null) dependsOn(kspTask)
    }
}

tasks.withType<Detekt>().configureEach {
    if (name.contains("Metadata", ignoreCase = true)) {
        val kspTask = project.tasks.findByName("kspCommonMainKotlinMetadata")
        if (kspTask != null) {
            dependsOn(kspTask)
        }
    }
}
