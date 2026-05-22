import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.compose")
    id("dokka-android-convention")
    id("com.vanniktech.maven.publish")
}

group   = Versions.group
version = Versions.libraryVersion

android {
    namespace  = "dev.kindling.compose"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(Versions.jvmTarget))
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:${Versions.navigationCompose}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycle}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(Versions.group, "compose", Versions.libraryVersion)

    pom {
        name.set("compose")
        description.set("Type-safe navigation and ViewModel utilities for Kindling")
        inceptionYear.set("2026")
        url.set("https://github.com/ClementBobin/Kindling")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("clementbobin")
                name.set("Clement Bobin")
                url.set("https://github.com/ClementBobin/")
            }
        }
        scm {
            url.set("https://github.com/ClementBobin/Kindling")
            connection.set("scm:git:git://github.com/ClementBobin/Kindling.git")
            developerConnection.set("scm:git:ssh://git@github.com/ClementBobin/Kindling.git")
        }
    }
}