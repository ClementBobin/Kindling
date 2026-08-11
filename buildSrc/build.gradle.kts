plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.2.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
    implementation("com.android.tools.build:gradle:9.0.0")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.36.0")
    implementation("dev.composedoctor:dev.composedoctor.gradle.plugin:0.1.0")
}