import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

group = Versions.group
version = Versions.libraryVersion

dependencies {
    // Compose Multiplatform — resolved from JetBrains space repo
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.animation)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
}

sourceSets {
    named("main")
    named("test")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(Versions.jvmTarget))
    }
}

tasks.test {
    useJUnitPlatform()
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "kindling-core"
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set("kindling-core")
                description.set("Core module for kindling-core")
                url.set("https://github.com/clementbobin/kindling")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("clementbobin")
                        name.set("Clement Bobin")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ClementBobin/Kindling.git")
                    developerConnection.set("scm:git:ssh://github.com:ClementBobin/Kindling.git")
                    url.set("https://github.com/ClementBobin/Kindling")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT")) {
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                } else {
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                }
            )
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKeyId = System.getenv("GPG_KEY_ID")?.takeIf { it.isNotBlank() }
    val signingKey = System.getenv("GPG_KEY")?.takeIf { it.isNotBlank() }
    val signingPassphrase = System.getenv("GPG_PASSPHRASE")?.takeIf { it.isNotBlank() }

    if (signingKeyId != null && signingKey != null && signingPassphrase != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassphrase)
        sign(publishing.publications)
    }
}