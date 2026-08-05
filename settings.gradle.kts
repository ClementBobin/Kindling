pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven(url = "https://jitpack.io")
    }
    versionCatalogs { create("libs") }
}

rootProject.name = "kindling"

include(":core", ":utils", ":compose", ":android")

if (System.getenv("JITPACK") == null) {
    include(":sample")
}
