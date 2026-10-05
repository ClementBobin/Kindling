plugins {
    id("com.android.library")
    id("kindling-android-library")
    kotlin("android")
    id("dokka-convention")
    id("kindling-publish")
}

extra["pomDescription"] = "Android platform utilities for Kindling"

dependencies {
    implementation(project(":utils"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.annotation.jvm)
    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.play.services.location)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    implementation(libs.play.integrity)
    compileOnly(libs.ktor.client.mock)
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}