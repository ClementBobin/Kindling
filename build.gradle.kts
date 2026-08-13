plugins {
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    kotlin("plugin.compose") version Versions.kotlin apply false
    id("org.jetbrains.compose") version Versions.jetbrainCompose apply false
    id("com.android.library") apply false
    id("org.jetbrains.dokka") version Versions.dokka apply false
    id("com.android.kotlin.multiplatform.library") apply false
    id("com.google.devtools.ksp") version Versions.ksp apply false
}

// ── Dokka multi-module aggregation ───────────────────────────────────────────
dokka {
    dokkaPublications.html {
        moduleName.set("Kindling")
        outputDirectory.set(layout.buildDirectory.dir("docs/html"))
        includes.from("README.md")
    }
}

dependencies {
    dokka(project(":core"))
    dokka(project(":utils"))
    dokka(project(":android"))
    dokka(project(":compose"))
    // :processor is internal (KSP), not part of the public API
}