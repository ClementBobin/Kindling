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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.4.20")
    implementation("com.android.tools.build:gradle:8.9.2")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.31.0")
    implementation("dev.composedoctor:dev.composedoctor.gradle.plugin:0.1.0")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.2.0-2.0.2")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.8.2")
}