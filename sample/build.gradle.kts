plugins {
    id("com.android.application")
    kotlin("plugin.compose")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "dev.kindling.sample"
        compileSdk { version = release(36) }
        defaultConfig {
            minSdk = 23
        }
    }
}

dependencies {
    implementation(project(":core"))

    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.13.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}