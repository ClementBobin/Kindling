plugins {
    kotlin("jvm")
    id("dokka-convention")
    id("kindling-publish")
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation("androidx.annotation:annotation-jvm:1.10.0")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("org.bouncycastle:bcpkix-jdk18on:${Versions.castle}")
    implementation("org.bouncycastle:bcprov-jdk18on:${Versions.castle}")
    implementation("org.bouncycastle:bcpqc-jdk18on:${Versions.castle}")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
}

tasks.test { useJUnitPlatform() }