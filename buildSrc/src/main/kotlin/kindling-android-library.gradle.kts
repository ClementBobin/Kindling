/**
 * Convention plugin: kindling-android-library
 *
 * Reacts to whichever Android plugin the module applied:
 *   - com.android.library                      → pure Android library
 *   - com.android.kotlin.multiplatform.library → KMP library
 *
 * namespace is always derived from project.name:
 *   :android → dev.kindling.android
 *   :core    → dev.kindling.core
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

fun Project.configureSharedKotlin() {
    extensions.findByType<KotlinProjectExtension>()?.jvmToolchain(17)
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

// Pure Android library: com.android.library exposes LibraryExtension
pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension> {
        namespace  = "${Versions.group}.${project.name}"
        compileSdk = 36
        defaultConfig { minSdk = 21 }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
            isCoreLibraryDesugaringEnabled = true
        }
    }
    dependencies {
        add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:${Versions.desugarJdkLibs}")
    }
    configureSharedKotlin()
}

// KMP library: com.android.kotlin.multiplatform.library uses its own DSL
// namespace/compileSdk are set inside kotlin { android { } } in the module itself
pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
    configureSharedKotlin()
}