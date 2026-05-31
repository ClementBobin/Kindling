plugins {
    kotlin("jvm")
    id("dokka-convention")
    id("com.vanniktech.maven.publish")
}

group = Versions.group
version = Versions.libraryVersion

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(Versions.group, "utils", Versions.libraryVersion)

    pom {
        name.set("utils")
        description.set("Utils module for kindling")
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
