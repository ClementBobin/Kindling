object Versions {
    const val kotlin                = "2.4.0"
    const val jvmTarget             = "17"
    const val group                 = "io.github.clementbobin.kindling"
    const val dokka                 = "2.2.0"
    const val junit5                = "6.1.0"
    const val coroutines            = "1.11.0"
    const val jetbrainCompose       = "1.7.3"
    const val compose               = "1.13.0"
    const val navigationCompose     = "2.9.8"
    const val lifecycle             = "2.10.0"
    const val koin                  = "4.2.1"
    const val ktor                  = "2.3.12"
    const val coil                  = "3.5.0"
    const val serialization         = "1.6.3"
    const val immutableCollections  = "0.3.8"
    const val playIntegrity         = "1.4.0"

    // AndroidX / Google
    const val annotationJvm          = "1.10.0"
    const val core                   = "1.15.0"
    const val fragmentKtx            = "1.6.2"
    const val camera                 = "1.6.1"
    const val playServicesLocation   = "21.3.0"
    const val biometric              = "1.1.0"
    const val desugarJdkLibs         = "2.1.4"

    // Processor
    const val ksp = "2.3.10"

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