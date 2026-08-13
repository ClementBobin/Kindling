plugins {
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    kotlin("plugin.compose") version Versions.kotlin apply false
    id("org.jetbrains.compose") version Versions.jetbrainCompose apply false
    id("com.android.library") apply false
    id("org.jetbrains.dokka")
    id("com.android.kotlin.multiplatform.library") apply false
    id("com.google.devtools.ksp") version Versions.ksp apply false
}

// ── Dokka multi-module aggregation ───────────────────────────────────────────
dokka {
    dokkaPublications.html {
        moduleName.set("Kindling")
        outputDirectory.set(layout.buildDirectory.dir("docs/html"))
        // README.md gives the module landing page its intro text.
        // docs/*.md files must start with `# Module kindling` to be picked up.
        includes.from(
            "README.md",
            "docs/getting-started.md",
            "docs/contributing-docs.md",
        )
    }

    // ── Versioning plugin ─────────────────────────────────────────────────
    // Adds a version dropdown to the docs site.
    // olderVersionsDir points to a folder structured as:
    //   .ci-docs-history/
    //     0.2.0/ ← previous Dokka output
    //     0.3.0/ ← previous Dokka output
    // The GitHub Actions workflow downloads, populates, and archives this dir.
    pluginsConfiguration {
        versioning {
            version.set(Versions.libraryVersion)
            olderVersionsDir.set(rootDir.resolve(".ci-docs-history"))
            renderVersionsNavigationOnAllPages.set(true)
        }
    }
}

dependencies {
    dokka(project(":core"))
    dokka(project(":utils"))
    dokka(project(":android"))
    dokka(project(":compose"))
    // :processor is internal (KSP), not part of the public API

    // Dokka plugins — applied at doc-generation time only
    dokkaPlugin("org.jetbrains.dokka:versioning-plugin:${Versions.dokka}")
    dokkaPlugin("com.glureau:html-mermaid-dokka-plugin:0.6.0")
}