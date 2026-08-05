/**
 * Convention plugin: kindling-android-library
 *
 * Reacts to whichever Android plugin the module applied — no need to pick
 * a separate convention plugin per module type:
 *   - com.android.library                      → pure Android library
 *   - com.android.kotlin.multiplatform.library → KMP library
 *
 * namespace is always derived from project.name:
 *   :android → dev.kindling.android
 *   :core    → dev.kindling.core
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.android.build.api.dsl.LibraryExtension

fun Project.configureSharedKotlin() {
    kotlin {
        jvmToolchain(17)
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

fun Project.configureAndroidDefaults() {
    extensions.configure<LibraryExtension> {
        namespace  = "${Versions.group}.${project.name}"
        compileSdk = 36
        defaultConfig {
            minSdk = 21
        }
    }
}

pluginManager.withPlugin("com.android.library") {
    configureAndroidDefaults()
    extensions.configure<LibraryExtension> {
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
    configureSharedKotlin()
}

pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
    configureAndroidDefaults()
    configureSharedKotlin()
}