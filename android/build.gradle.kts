plugins {
    id("com.android.library")
    id("dokka-android-convention")
    id("com.vanniktech.maven.publish")
}

group   = Versions.group
version = Versions.libraryVersion

android {
    namespace  = "dev.kindling.android"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(Versions.jvmTarget))
    }
}

dependencies {
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
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation(project(":utils"))

    implementation("io.ktor:ktor-client-core:${Versions.ktor}")
    implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
    implementation("io.ktor:ktor-client-logging:${Versions.ktor}")
    implementation("io.ktor:ktor-client-auth:${Versions.ktor}")
    compileOnly("io.ktor:ktor-client-mock:${Versions.ktor}")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(Versions.group, "android", Versions.libraryVersion)

    pom {
        name.set("android")
        description.set("Android platform utilities for Kindling")
        inceptionYear.set("2026")
        url.set("https://github.com/ClementBobin/Kindling")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("clementbobin")
                name.set("Clement Bobin")
                url.set("https://github.com/ClementBobin/")
            }
        }
        scm {
            url.set("https://github.com/ClementBobin/Kindling")
            connection.set("scm:git:git://github.com/ClementBobin/Kindling.git")
            developerConnection.set("scm:git:ssh://git@github.com/ClementBobin/Kindling.git")
        }
    }
}