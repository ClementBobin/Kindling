plugins {
    kotlin("jvm")
    id("kindling-publish")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:${Versions.ksp}")
}