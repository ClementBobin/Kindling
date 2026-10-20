package dev.kindling.android.firebase

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class KFirebaseConfigTest {

    // JUnit 5 requires test methods to return Unit, so the result of runBlocking is dropped.
    private fun <T> blocking(block: suspend () -> T) {
        runBlocking { block() }
    }

    private val config = KFirebaseConfig("k", "1:1:android:a", "p", "1")

    @Test
    fun `rejects blank fields`() {
        assertFailsWith<IllegalArgumentException> { config.copy(apiKey = " ") }
        assertFailsWith<IllegalArgumentException> { config.copy(applicationId = "") }
        assertFailsWith<IllegalArgumentException> { config.copy(projectId = "") }
        assertFailsWith<IllegalArgumentException> { config.copy(gcmSenderId = "") }
    }

    @Test
    fun `storage bucket is optional`() {
        assertNull(config.storageBucket)
    }

    @Test
    fun `toString does not leak the api key`() {
        assertFalse("secret-key" in config.copy(apiKey = "secret-key").toString())
    }

    @Test
    fun `in memory store saves and clears the tenant`() = blocking {
        val store = KInMemoryFirebaseConfigStore()
        assertNull(store.get())
        val tenant = KStoredFirebaseTenant("acme", config)
        store.save(tenant)
        assertEquals(tenant, store.get())
        store.clear()
        assertNull(store.get())
    }
}
