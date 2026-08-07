plugins {
    id("com.android.application")
    kotlin("plugin.compose")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "${Versions.group}.sample"
        compileSdk { version = release(36) }

        defaultConfig {
            // Fixes Manifest Merger error by matching/exceeding library requirements
            minSdk = 23
            
            // Fixes the 64K method limit (DexArchiveMergerException)
            multiDexEnabled = true
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
