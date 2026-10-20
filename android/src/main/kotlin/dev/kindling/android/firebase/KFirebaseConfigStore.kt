package dev.kindling.android.firebase

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The tenant whose Firebase project is currently active, together with its configuration.
 *
 * @param tenantId Identifier of the client (tenant) as known by your backend.
 * @param config Public Firebase configuration of that client.
 */
data class KStoredFirebaseTenant(
    val tenantId: String,
    val config: KFirebaseConfig,
)

/**
 * Persists the active tenant's [KFirebaseConfig] so it can be restored at each launch (and in
 * the background, when the system starts your `FirebaseMessagingService`) without calling your
 * API again.
 *
 * A dynamically created `FirebaseApp` only lives as long as the process, so this store is what
 * makes it survive process death. The configuration is public, so it is not encrypted by
 * default.
 *
 * Kindling provides [KSharedPrefsFirebaseConfigStore] (default) and [KInMemoryFirebaseConfigStore]
 * (tests). Implement this interface for DataStore, Room or anything else.
 */
interface KFirebaseConfigStore {

    /** Returns the stored tenant, or `null` if nothing (or something unreadable) is stored. */
    suspend fun get(): KStoredFirebaseTenant?

    /** Replaces the stored tenant. */
    suspend fun save(tenant: KStoredFirebaseTenant)

    /** Removes the stored tenant. */
    suspend fun clear()
}

/**
 * [KFirebaseConfigStore] backed by [SharedPreferences].
 *
 * Reads are synchronous and cheap, which lets [KFirebaseManager.restoreBlocking] be called from
 * `Application.onCreate()`.
 *
 * ### Example usage:
 * ```kotlin
 * val store = KSharedPrefsFirebaseConfigStore(context)
 * val manager = KFirebaseManager(context, store)
 * ```
 *
 * @param context Any context; the application context is retained.
 * @param name Name of the preferences file.
 */
class KSharedPrefsFirebaseConfigStore(
    context: Context,
    name: String = "kindling_firebase_tenant",
) : KFirebaseConfigStore {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)

    override suspend fun get(): KStoredFirebaseTenant? {
        val tenantId = prefs.getString(KEY_TENANT, null) ?: return null
        return try {
            KStoredFirebaseTenant(
                tenantId,
                KFirebaseConfig(
                    apiKey = prefs.getString(KEY_API_KEY, null).orEmpty(),
                    applicationId = prefs.getString(KEY_APP_ID, null).orEmpty(),
                    projectId = prefs.getString(KEY_PROJECT_ID, null).orEmpty(),
                    gcmSenderId = prefs.getString(KEY_SENDER_ID, null).orEmpty(),
                    storageBucket = prefs.getString(KEY_BUCKET, null),
                ),
            )
        } catch (_: IllegalArgumentException) {
            null // incomplete or corrupted entry
        }
    }

    // commit() (not apply()) so the value is on disk before a killed app is woken by a push.
    override suspend fun save(tenant: KStoredFirebaseTenant) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString(KEY_TENANT, tenant.tenantId)
                .putString(KEY_API_KEY, tenant.config.apiKey)
                .putString(KEY_APP_ID, tenant.config.applicationId)
                .putString(KEY_PROJECT_ID, tenant.config.projectId)
                .putString(KEY_SENDER_ID, tenant.config.gcmSenderId)
                .putString(KEY_BUCKET, tenant.config.storageBucket)
                .commit()
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            prefs.edit().clear().commit()
        }
    }

    private companion object {
        const val KEY_TENANT = "tenant_id"
        const val KEY_API_KEY = "api_key"
        const val KEY_APP_ID = "application_id"
        const val KEY_PROJECT_ID = "project_id"
        const val KEY_SENDER_ID = "gcm_sender_id"
        const val KEY_BUCKET = "storage_bucket"
    }
}

/** [KFirebaseConfigStore] that keeps the tenant in memory only. Intended for tests. */
class KInMemoryFirebaseConfigStore(
    private var tenant: KStoredFirebaseTenant? = null,
) : KFirebaseConfigStore {

    override suspend fun get(): KStoredFirebaseTenant? = tenant

    override suspend fun save(tenant: KStoredFirebaseTenant) {
        this.tenant = tenant
    }

    override suspend fun clear() {
        tenant = null
    }
}
