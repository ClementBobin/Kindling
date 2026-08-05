plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("dokka-convention")
    id("com.vanniktech.maven.publish")
}

group = Versions.group
version = Versions.libraryVersion

kotlin {
    jvmToolchain(17)

    android {
        namespace = "${Versions.group}.core"
        compileSdk { version = release(36) }
    }

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.animation)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
                implementation(kotlin("stdlib"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
            }
        }

        val androidMain by getting {
            dependencies { }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    if (System.getenv("JITPACK") == null) {
        signAllPublications()
    }

    coordinates(Versions.group, "core", Versions.libraryVersion)

    pom {
        name.set("core")
        description.set("Core module for kindling")
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
