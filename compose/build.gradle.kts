plugins {
    id("com.android.library")
    id("kindling-android-library")
    kotlin("plugin.compose")
    kotlin("android")
    id("dokka-convention")
    id("kindling-publish")
}

extra["pomDescription"] = "Type-safe navigation and ViewModel utilities for Kindling"

android {
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.compose.ui:ui:${Versions.composeBom}")
    implementation("androidx.compose.runtime:runtime:${Versions.composeBom}")
    implementation("androidx.compose.foundation:foundation:${Versions.composeBom}")
    implementation("androidx.compose.material3:material3:${Versions.material3}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Versions.composeBom}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Versions.composeBom}")
    implementation("androidx.activity:activity-compose:${Versions.compose}")
    implementation("androidx.navigation:navigation-compose:${Versions.navigationCompose}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycle}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("io.insert-koin:koin-androidx-compose:${Versions.koin}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
}