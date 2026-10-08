import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

// Live component showcase: a Compose Multiplatform (Wasm) app that the docs website embeds in <iframe>s.
// It is NOT published to Maven. Build it with:
//
//   ./gradlew :showcase:wasmJsBrowserDistribution
//
// and copy build/dist/wasmJs/productionExecutable/* into website/public/showcase/
// (CI does this in .github/workflows/docs.yml).
plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("showcase")
        browser {
            commonWebpackConfig {
                outputFileName = "showcase.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3.core)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.animation)
        }
    }
}
