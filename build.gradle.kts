import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.compose") version Versions.kotlin apply false
    id("org.jetbrains.compose") version Versions.compose apply false
    id("org.jetbrains.dokka") apply false
    id("com.vanniktech.maven.publish") version Versions.mavenPublish apply false
}