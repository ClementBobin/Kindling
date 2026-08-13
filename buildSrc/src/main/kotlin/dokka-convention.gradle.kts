plugins {
    id("org.jetbrains.dokka")
}

dokka {
    dokkaSourceSets.configureEach {
        // Link every symbol back to its source on GitHub
        sourceLink {
            localDirectory.set(file("src"))
            remoteUrl.set(java.net.URI("https://github.com/ClementBobin/Kindling/tree/main/${project.name}/src"))
            remoteLineSuffix.set("#L")
        }

        // Document public and protected API only
        documentedVisibilities.set(
            setOf(
                org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Public,
                org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Protected,
            )
        )

        // Don't document generated KSP sources
        perPackageOption {
            matchingRegex.set(".*\\.generated\\..*")
            suppress.set(true)
        }
    }
}