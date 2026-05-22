// Dokka convention for Android library modules.
// Does NOT apply kotlin("jvm") — the Android plugin already provides the
// Kotlin extension; applying jvm on top causes the "extension already
// registered" error.
plugins {
    id("org.jetbrains.dokka")
}