plugins {
    id("com.android.library")
    id("kindling-android-library")
    id("dokka-convention")
    id("kindling-publish")
}

extra["pomDescription"] = "Android platform utilities for Kindling"

dependencies {
    implementation(project(":utils"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("androidx.annotation:annotation-jvm:${Versions.annotationJvm}")
    implementation("androidx.core:core:${Versions.core}")
    implementation("androidx.fragment:fragment-ktx:${Versions.fragmentKtx}")
    implementation("androidx.camera:camera-core:${Versions.camera}")
    implementation("androidx.camera:camera-view:${Versions.camera}")
    implementation("androidx.camera:camera-lifecycle:${Versions.camera}")
    implementation("com.google.android.gms:play-services-location:${Versions.playServicesLocation}")
    implementation("androidx.biometric:biometric:${Versions.biometric}")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("io.ktor:ktor-client-core:${Versions.ktor}")
    implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
    implementation("io.ktor:ktor-client-logging:${Versions.ktor}")
    implementation("io.ktor:ktor-client-auth:${Versions.ktor}")
    implementation("com.google.android.play:integrity:${Versions.playIntegrity}")
    compileOnly("io.ktor:ktor-client-mock:${Versions.ktor}")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
}