object KindlingProperties {
    const val group = "io.github.clementbobin.kindling"

    val libraryVersion: String
        get() = System.getenv("RELEASE_VERSION")
            ?.removePrefix("v")
            ?.takeIf { it.isNotBlank() }
            ?: "SNAPSHOT"
}
