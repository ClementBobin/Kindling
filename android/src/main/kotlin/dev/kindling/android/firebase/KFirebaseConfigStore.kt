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
        val json = prefs.getString(KEY_CONFIG, null) ?: return null
        return try {
            KStoredFirebaseTenant(tenantId, KFirebaseConfig.fromJson(json))
        } catch (_: KFirebaseConfigException) {
            null
        }
    }

    // commit() (not apply()) so the value is on disk before a killed app is woken by a push.
    override suspend fun save(tenant: KStoredFirebaseTenant) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString(KEY_TENANT, tenant.tenantId)
                .putString(KEY_CONFIG, tenant.config.toJson())
                .commit()
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            prefs.edit().remove(KEY_TENANT).remove(KEY_CONFIG).commit()
        }
    }

    private companion object {
        const val KEY_TENANT = "tenant_id"
        const val KEY_CONFIG = "config"
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
