plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.google.devtools.ksp")
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
            //kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.animation)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")
                implementation("io.coil-kt.coil3:coil-compose:${Versions.coil}")
                implementation("io.coil-kt.coil3:coil-network-okhttp:${Versions.coil}")
                implementation("io.insert-koin:koin-core:${Versions.koin}")
                implementation("io.insert-koin:koin-compose:${Versions.koin}")
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:${Versions.immutableCollections}")
                implementation(kotlin("stdlib"))
                implementation(project(":utils"))
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":android"))
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":processor"))
}

tasks.matching { it.name == "androidSourcesJar" || it.name == "sourcesJar" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    // Make sure Detekt tasks depend on KSP metadata generation if present
    if (name.contains("Metadata", ignoreCase = true)) {
        val kspTask = project.tasks.findByName("kspCommonMainKotlinMetadata")
        if (kspTask != null) {
            dependsOn(kspTask)
        }
    }
}