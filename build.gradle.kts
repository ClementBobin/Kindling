import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlin apply false
    kotlin("plugin.compose") version Versions.kotlin apply false
    id("org.jetbrains.compose") version Versions.compose apply false
    id("org.jetbrains.dokka") version Versions.dokka apply false
    id("com.vanniktech.maven.publish") version Versions.mavenPublish apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")

    group = Versions.group
    version = Versions.libraryVersion

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(Versions.jvmTarget))
        }
    }

    tasks.withType<com.vanniktech.maven.publish.tasks.PublishToMavenCentral>().configureEach {
        deploymentName = Versions.group
    }
}