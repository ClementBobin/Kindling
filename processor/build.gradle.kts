plugins {
    kotlin("jvm")
    id("kindling-publish")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.ksp.api)
}