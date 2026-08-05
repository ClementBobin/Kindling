plugins {
    id("kindling-android-library")
    kotlin("plugin.compose")
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
    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:${Versions.compose}")
    implementation("androidx.navigation:navigation-compose:${Versions.navigationCompose}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycle}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("io.insert-koin:koin-androidx-compose:${Versions.koin}")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
}