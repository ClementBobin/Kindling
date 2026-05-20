rootProject.name = "my-kotlin-library"

include(":core", ":utils")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}
