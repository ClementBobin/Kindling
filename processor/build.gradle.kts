plugins {
    kotlin("jvm")
    id("kindling-publish")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:${Versions.ksp}")
}