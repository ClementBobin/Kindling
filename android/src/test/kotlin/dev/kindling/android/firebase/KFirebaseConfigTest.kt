package dev.kindling.android.firebase

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KFirebaseConfigTest {

    // JUnit 5 requires test methods to return Unit, so the result of runBlocking is dropped.
    private fun <T> blocking(block: suspend () -> T) {
        runBlocking { block() }
    }

    private val googleServices = """
        {
          "project_info": {
            "project_number": "123456789012",
            "project_id": "acme-doors",
            "storage_bucket": "acme-doors.appspot.com"
          },
          "client": [
            {
              "client_info": {
                "mobilesdk_app_id": "1:123456789012:android:other",
                "android_client_info": { "package_name": "com.other.app" }
              },
              "api_key": [ { "current_key": "other-key" } ]
            },
            {
              "client_info": {
                "mobilesdk_app_id": "1:123456789012:android:abc",
                "android_client_info": { "package_name": "com.kindling.doors" }
              },
              "api_key": [ { "current_key": "AIzaSyExample" } ]
            }
          ],
          "configuration_version": "1"
        }
    """.trimIndent()

    @Test
    fun `parses a flat API payload`() {
        val config = KFirebaseConfig.fromJson(
            """{"apiKey":"k","applicationId":"1:1:android:a","projectId":"p","messagingSenderId":"1"}"""
        )
        assertEquals(KFirebaseConfig("k", "1:1:android:a", "p", "1"), config)
    }

    @Test
    fun `accepts snake case aliases and project number as sender id`() {
        val config = KFirebaseConfig.fromJson(
            """{"api_key":"k","app_id":"1:1:android:a","project_id":"p","project_number":"1","storage_bucket":"b"}"""
        )
        assertEquals("1", config.gcmSenderId)
        assertEquals("b", config.storageBucket)
    }

    @Test
    fun `parses google-services json and picks the client matching the package name`() {
        val config = KFirebaseConfig.fromJson(googleServices, packageName = "com.kindling.doors")
        assertEquals("AIzaSyExample", config.apiKey)
        assertEquals("1:123456789012:android:abc", config.applicationId)
        assertEquals("acme-doors", config.projectId)
        assertEquals("123456789012", config.gcmSenderId)
        assertEquals("acme-doors.appspot.com", config.storageBucket)
    }

    @Test
    fun `uses the first client when no package name is given`() {
        assertEquals("other-key", KFirebaseConfig.fromJson(googleServices).apiKey)
    }

    @Test
    fun `fails when no client matches the package name`() {
        val error = assertFailsWith<KFirebaseConfigException> {
            KFirebaseConfig.fromJson(googleServices, packageName = "com.missing")
        }
        assertTrue("com.missing" in error.message.orEmpty())
    }

    @Test
    fun `rejects a service account key`() {
        val serviceAccount = """
            {"type":"service_account","project_id":"p","private_key_id":"x",
             "private_key":"-----BEGIN PRIVATE KEY-----\nabc\n-----END PRIVATE KEY-----\n",
             "client_email":"a@p.iam.gserviceaccount.com"}
        """.trimIndent()
        val error = assertFailsWith<KFirebaseConfigException> { KFirebaseConfig.fromJson(serviceAccount) }
        assertFalse("BEGIN PRIVATE KEY" in error.message.orEmpty())
    }

    @Test
    fun `reports every missing field`() {
        val error = assertFailsWith<KFirebaseConfigException> {
            KFirebaseConfig.fromJson("""{"projectId":"p"}""")
        }
        val message = error.message.orEmpty()
        assertTrue("apiKey" in message && "applicationId" in message && "messagingSenderId" in message)
    }

    @Test
    fun `rejects malformed json and non objects`() {
        assertFailsWith<KFirebaseConfigException> { KFirebaseConfig.fromJson("not json") }
        assertFailsWith<KFirebaseConfigException> { KFirebaseConfig.fromJson("[]") }
    }

    @Test
    fun `round trips through toJson`() {
        val config = KFirebaseConfig("k", "1:1:android:a", "p", "1", storageBucket = "b")
        assertEquals(config, KFirebaseConfig.fromJson(config.toJson()))
    }

    @Test
    fun `toString does not leak the api key`() {
        assertFalse("secret-key" in KFirebaseConfig("secret-key", "a", "p", "1").toString())
    }

    @Test
    fun `api source parses the fetched body`() = blocking {
        val source = KFirebaseApiConfigSource {
            """{"apiKey":"k","applicationId":"a","projectId":"p","messagingSenderId":"1"}"""
        }
        assertEquals("p", source.load().projectId)
    }

    @Test
    fun `file source parses the file content and checks the package name`() = blocking {
        val ok = KFirebaseFileConfigSource("com.kindling.doors") { googleServices }
        assertEquals("acme-doors", ok.load().projectId)

        val wrong = KFirebaseFileConfigSource("com.missing") { googleServices }
        assertFailsWith<KFirebaseConfigException> { wrong.load() }
    }

    @Test
    fun `file source wraps IO errors`() = blocking {
        val source = KFirebaseFileConfigSource(null) { throw java.io.FileNotFoundException("gone") }
        assertFailsWith<KFirebaseConfigException> { source.load() }
    }

    @Test
    fun `in memory store saves and clears the tenant`() = blocking {
        val store = KInMemoryFirebaseConfigStore()
        assertNull(store.get())
        val tenant = KStoredFirebaseTenant("acme", KFirebaseConfig("k", "a", "p", "1"))
        store.save(tenant)
        assertEquals(tenant, store.get())
        store.clear()
        assertNull(store.get())
    }
}
