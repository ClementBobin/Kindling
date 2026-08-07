plugins {
    kotlin("jvm")
    id("dokka-convention")
    id("kindling-publish")
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
}

tasks.test { useJUnitPlatform() }