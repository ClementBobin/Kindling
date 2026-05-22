object Versions {
    const val kotlin            = "2.2.0"
    const val jvmTarget         = "17"
    const val group             = "io.github.clementbobin.kindling"
    const val dokka             = "2.2.0"
    const val junit5            = "5.11.4"
    const val coroutines        = "1.10.2"
    const val compose           = "1.7.3"
    const val mavenPublish      = "0.36.0"
    const val agp               = "8.10.1"
    const val navigationCompose = "2.9.0"
    const val lifecycle         = "2.9.0"

    /**
     * Resolved at configuration time from the RELEASE_VERSION environment
     * variable (set by the publish workflow from the Git tag), or falls back
     * to a local SNAPSHOT so regular builds always work without it.
     *
     * Tag format expected: "0.2.0" or "v0.2.0" (the leading "v" is stripped).
     */
    val libraryVersion: String
        get() = System.getenv("RELEASE_VERSION")
            ?.removePrefix("v")
            ?.takeIf { it.isNotBlank() }
            ?: "SNAPSHOT"
}