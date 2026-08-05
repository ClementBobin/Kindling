/**
 * Convention plugin: kindling-publish
 *
 * Handles all Maven Central publishing boilerplate.
 * Everything is derived from project.name automatically:
 *   :utils  → group=io.github.clementbobin.kindling, artifact=utils, name="utils"
 *
 * group, version, and coordinates are all set here — modules don't need to declare them.
 *
 * Only override needed per module (optional):
 *   extra["pomDescription"] = "…"
 * Falls back to "${project.name} module for Kindling" if not set.
 */
plugins {
    id("com.vanniktech.maven.publish")
}

group   = Versions.group
version = Versions.libraryVersion

afterEvaluate {
    val pomDescription = project.extra
        .takeIf { it.has("pomDescription") }
        ?.get("pomDescription") as? String
        ?: "${project.name} module for Kindling"

    mavenPublishing {
        publishToMavenCentral()

        if (System.getenv("JITPACK") == null) {
            signAllPublications()
        }

        coordinates(Versions.group, project.name, Versions.libraryVersion)

        pom {
            name.set(project.name)
            description.set(pomDescription)
            inceptionYear.set("2026")
            url.set("https://github.com/ClementBobin/Kindling")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("clementbobin")
                    name.set("Clement Bobin")
                    url.set("https://github.com/ClementBobin/")
                }
            }

            scm {
                url.set("https://github.com/ClementBobin/Kindling")
                connection.set("scm:git:git://github.com/ClementBobin/Kindling.git")
                developerConnection.set("scm:git:ssh://git@github.com/ClementBobin/Kindling.git")
            }
        }
    }
}