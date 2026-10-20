package dev.kindling.android.firebase

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Converts this configuration to the [FirebaseOptions] expected by `FirebaseApp.initializeApp`. */
fun KFirebaseConfig.toFirebaseOptions(): FirebaseOptions =
    FirebaseOptions.Builder()
        .setApiKey(apiKey)
        .setApplicationId(applicationId)
        .setProjectId(projectId)
        .setGcmSenderId(gcmSenderId)
        .apply { storageBucket?.let { setStorageBucket(it) } }
        .build()

/**
 * Runtime Firebase (FCM) initialisation for **one app that serves several Firebase projects**.
 *
 * The static `google-services.json` plugin can only embed a single project. This manager builds
 * the `FirebaseApp` in code from a [KFirebaseConfig] instead, which lets each client bring its
 * own project. The configuration can come from your API ([KFirebaseApiConfigSource]) or from a
 * `google-services.json` on the device ([KFirebaseFileConfigSource]).
 *
 * Only one tenant is active at a time. Initialising another tenant replaces the previous
 * `FirebaseApp`.
 *
 * ## Lifecycle
 * A `FirebaseApp` created in code does not survive process death, so it has to be recreated at
 * every launch **and before the system delivers a message to a killed app**:
 * 1. After login, call [initialize] with a source. The configuration is persisted in the
 *    [KFirebaseConfigStore].
 * 2. In `Application.onCreate()`, call [restoreBlocking] (no network call: it reads the store).
 * 3. Send the token returned by [fetchToken] to your backend, together with the tenant id. In
 *    `FirebaseMessagingService.onNewToken` call [restoreBlocking] first, then upload the new
 *    token.
 *
 * ## Security
 * The device only ever handles the public client configuration. The service account key used to
 * *send* notifications stays on your backend, one per tenant.
 *
 * ### Example usage:
 * ```kotlin
 * class App : Application() {
 *     val firebase by lazy { KFirebaseManager(this) }
 *
 *     override fun onCreate() {
 *         super.onCreate()
 *         firebase.restoreBlocking()   // no-op before the first login
 *     }
 * }
 *
 * // After login, config downloaded from your API
 * suspend fun onLogin(clientId: String) {
 *     firebase.initialize(clientId, KFirebaseApiConfigSource.fromClient(http, "clients/$clientId/firebase"))
 *     api.registerDevice(clientId, firebase.fetchToken())
 * }
 *
 * // Or with a google-services.json already on the device
 * firebase.initialize("acme", KFirebaseFileConfigSource.fromFile(file, context.packageName))
 * ```
 *
 * @param context Any context; the application context is retained.
 * @param store Where the active configuration is persisted between launches.
 * @param appName Name of the `FirebaseApp` created by this manager. Passing
 *   `FirebaseApp.DEFAULT_APP_NAME` makes it the default app, which only works when the
 *   `google-services` Gradle plugin is **not** used.
 */
class KFirebaseManager(
    context: Context,
    private val store: KFirebaseConfigStore = KSharedPrefsFirebaseConfigStore(context),
    private val appName: String = DEFAULT_TENANT_APP_NAME,
) {

    private val appContext: Context = context.applicationContext
    private val mutex = Mutex()

    @Volatile
    private var activeTenantId: String? = null

    /** Identifier of the tenant that is currently initialised, or `null`. */
    val currentTenantId: String? get() = activeTenantId

    /** The `FirebaseApp` created by this manager, or `null` if none is initialised. */
    val currentApp: FirebaseApp?
        get() = FirebaseApp.getApps(appContext).firstOrNull { it.name == appName }

    /**
     * Loads the configuration from [source], initialises Firebase with it and persists it.
     *
     * @param tenantId Identifier of the client, as known by your backend.
     * @param source Where the configuration comes from.
     * @param persist Save the configuration so [restore] can recreate the app at next launch.
     * @return The initialised `FirebaseApp`.
     * @throws KFirebaseConfigException If the configuration is missing or invalid.
     */
    suspend fun initialize(
        tenantId: String,
        source: KFirebaseConfigSource,
        persist: Boolean = true,
    ): FirebaseApp = initialize(tenantId, source.load(), persist)

    /**
     * Initialises Firebase with an already loaded [config].
     *
     * Calling it again with the same tenant and configuration returns the existing app. A
     * different configuration deletes the previous app (and its FCM token) first.
     *
     * @param tenantId Identifier of the client, as known by your backend.
     * @param config Public Firebase configuration of that client.
     * @param persist Save the configuration so [restore] can recreate the app at next launch.
     * @return The initialised `FirebaseApp`.
     */
    suspend fun initialize(
        tenantId: String,
        config: KFirebaseConfig,
        persist: Boolean = true,
    ): FirebaseApp = mutex.withLock {
        val app = ensureApp(config)
        activeTenantId = tenantId
        if (persist) store.save(KStoredFirebaseTenant(tenantId, config))
        app
    }

    /**
     * Recreates the `FirebaseApp` from the persisted configuration, without any network call.
     *
     * @return The `FirebaseApp`, or `null` if no tenant has been stored yet.
     */
    suspend fun restore(): FirebaseApp? = mutex.withLock {
        val stored = store.get() ?: return@withLock null
        val app = ensureApp(stored.config)
        activeTenantId = stored.tenantId
        app
    }

    /**
     * Blocking variant of [restore] for `Application.onCreate()` and `FirebaseMessagingService`,
     * where it must complete before the first message is handled. It is fast with the default
     * [KSharedPrefsFirebaseConfigStore]. Do not use it with a slow store on the main thread.
     */
    fun restoreBlocking(): FirebaseApp? = runBlocking { restore() }

    /**
     * Returns the FCM registration token of the active tenant's project.
     *
     * @throws IllegalStateException If no tenant is initialised (call [initialize] or [restore]).
     */
    suspend fun fetchToken(): String {
        val app = currentApp
            ?: throw IllegalStateException("No Firebase tenant is initialised: call initialize() or restore() first")
        return app.get(FirebaseMessaging::class.java).token.await()
    }

    /**
     * Forgets the active tenant, for example on logout.
     *
     * @param deleteToken Also invalidate the FCM token, so the old project stops reaching this device.
     */
    suspend fun clear(deleteToken: Boolean = true) = mutex.withLock {
        val app = currentApp
        if (app != null) {
            if (deleteToken) {
                runCatching { app.get(FirebaseMessaging::class.java).deleteToken().await() }
            }
            app.delete()
        }
        activeTenantId = null
        store.clear()
    }

    /** Creates the app, or reuses/replaces the existing one. Must be called with [mutex] held. */
    private suspend fun ensureApp(config: KFirebaseConfig): FirebaseApp {
        val existing = currentApp
        if (existing != null) {
            if (existing.options.matches(config)) return existing
            runCatching { existing.get(FirebaseMessaging::class.java).deleteToken().await() }
            existing.delete()
        }
        val options = config.toFirebaseOptions()
        return if (appName == FirebaseApp.DEFAULT_APP_NAME) {
            FirebaseApp.initializeApp(appContext, options)
        } else {
            FirebaseApp.initializeApp(appContext, options, appName)
        }
    }

    private fun FirebaseOptions.matches(config: KFirebaseConfig): Boolean =
        apiKey == config.apiKey &&
            applicationId == config.applicationId &&
            projectId == config.projectId &&
            gcmSenderId == config.gcmSenderId

    companion object {
        /** Default name of the `FirebaseApp` created for the active tenant. */
        const val DEFAULT_TENANT_APP_NAME: String = "kindling-tenant"
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result -> cont.resume(result) }
    addOnFailureListener { e -> cont.resumeWithException(e) }
    addOnCanceledListener { cont.cancel() }
}
