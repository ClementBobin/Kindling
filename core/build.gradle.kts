import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("dokka-convention")
    id("com.vanniktech.maven.publish")
}

group = Versions.group
version = Versions.libraryVersion

val debugImplementation by configurations.creating

dependencies {
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.animation)
    debugImplementation(compose.uiTooling)
    implementation(compose.preview)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(Versions.jvmTarget))
    }
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

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
