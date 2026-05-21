import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

group = Versions.group
version = Versions.libraryVersion

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
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
            artifactId = "kindling-utils"
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set("kindling-utils")
                description.set("Utils module for kindling")
                url.set("https://github.com/ClementBobin/Kindling")

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