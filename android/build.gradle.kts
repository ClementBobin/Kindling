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
    implementation("androidx.annotation:annotation-jvm:1.10.0")
    implementation("androidx.core:core:1.15.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.camera:camera-core:1.6.1")
    implementation("androidx.camera:camera-view:1.6.1")
    implementation("androidx.camera:camera-lifecycle:1.6.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.biometric:biometric:1.1.0")

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