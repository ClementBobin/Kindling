package dev.kindling.android.firebase

import android.content.Context
import android.util.Log
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
 * own project. You give it a [KFirebaseConfig] DTO, filled from your API, a file or anything else.
 *
 * Only one tenant is active at a time. Initialising another tenant replaces the previous
 * `FirebaseApp`. Use [switchTenant] to run the whole switch sequence in one call.
 *
 * ## Setup
 * - Do **not** apply the `google-services` Gradle plugin and remove any `google-services.json`,
 *   otherwise a default `FirebaseApp` exists and can collide with the tenant app.
 * - Kindling's manifest already sets `firebase_messaging_auto_init_enabled=false`, so FCM does not
 *   start on its own before a tenant is initialised.
 *
 * ## Lifecycle
 * A `FirebaseApp` created in code does not survive process death, so it has to be recreated at
 * every launch **and before the system delivers a message to a killed app**:
 * 1. After login, call [initialize] (or [switchTenant]) with the DTO. The configuration is
 *    persisted in the [KFirebaseConfigStore].
 * 2. In `Application.onCreate()`, call [restoreBlocking] (no network call: it reads the store).
 * 3. Send the token returned by [fetchToken] to your backend, together with the tenant id.
 *
 * ## Token rotation
 * `FirebaseMessagingService.onNewToken` is documented as being called for the **default**
 * `FirebaseApp` only. In the default named-app mode ([DEFAULT_TENANT_APP_NAME]) you should
 * therefore also re-check the token at launch / on foreground with [fetchTokenIfChanged]. If you
 * pass `FirebaseApp.DEFAULT_APP_NAME` as `appName`, `onNewToken` fires normally: call
 * [restoreBlocking] first, then upload the new token.
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
 * // After login or on tenant switch
 * suspend fun onTenantSelected(clientId: String) {
 *     firebase.switchTenant(
 *         tenantId = clientId,
 *         configProvider = { api.getFirebaseConfig(clientId).toKConfig() },
 *         onTokenReady = { id, token, previousTenantId ->
 *             api.registerDevice(id, token)
 *             previousTenantId?.let { api.markDeviceTokenStale(it) }
 *         },
 *     )
 * }
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
     * Initialises Firebase from a [config] DTO and persists it.
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
     * Switches the active tenant in one call: fetches its config, (re)initialises Firebase, gets
     * a fresh token and hands it to [onTokenReady], together with the tenant that was replaced so
     * your backend can mark the old tenant's token as stale.
     *
     * The previous tenant is read from the manager's own state, so after a process restart it is
     * the tenant restored by [restore] / [restoreBlocking].
     *
     * @param tenantId Identifier of the new tenant, as known by your backend.
     * @param configProvider Fetches the new tenant's public config (usually from your API).
     * @param onTokenReady Upload the token here. `previousTenantId` is `null` on first login or
     *   when the tenant did not change.
     */
    suspend fun switchTenant(
        tenantId: String,
        configProvider: suspend () -> KFirebaseConfig,
        onTokenReady: suspend (tenantId: String, token: String, previousTenantId: String?) -> Unit,
    ) {
        val previous = currentTenantId
        val config = configProvider()
        initialize(tenantId, config)
        val token = fetchToken()
        onTokenReady(tenantId, token, previous?.takeIf { it != tenantId })
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
     * Returns the current FCM token only if it differs from [lastUploaded], otherwise `null`.
     *
     * Call it at launch / on foreground (and optionally from a periodic job) to catch token
     * rotations, since `onNewToken` is only guaranteed for the default `FirebaseApp`.
     *
     * ```kotlin
     * firebase.fetchTokenIfChanged(prefs.lastToken)?.let { token ->
     *     api.registerDevice(firebase.currentTenantId!!, token)
     *     prefs.lastToken = token
     * }
     * ```
     *
     * @param lastUploaded The last token your backend received for this tenant, if any.
     * @throws IllegalStateException If no tenant is initialised.
     */
    suspend fun fetchTokenIfChanged(lastUploaded: String?): String? =
        fetchToken().takeIf { it != lastUploaded }

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
        warnIfDefaultAppCollides()

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

    /**
     * Logs a warning when a default `FirebaseApp` already exists next to our named app, which
     * usually means the `google-services` plugin or a `google-services.json` is still in the app.
     */
    private fun warnIfDefaultAppCollides() {
        if (appName == FirebaseApp.DEFAULT_APP_NAME) return
        val hasDefault = FirebaseApp.getApps(appContext)
            .any { it.name == FirebaseApp.DEFAULT_APP_NAME }
        if (hasDefault) {
            Log.w(
                TAG,
                "A default FirebaseApp already exists (google-services plugin or " +
                    "google-services.json?). It may collide with the tenant app. " +
                    "Remove the plugin for multi-tenant mode.",
            )
        }
    }

    private fun FirebaseOptions.matches(config: KFirebaseConfig): Boolean =
        apiKey == config.apiKey &&
            applicationId == config.applicationId &&
            projectId == config.projectId &&
            gcmSenderId == config.gcmSenderId

    companion object {
        private const val TAG = "KFirebaseManager"

        /** Default name of the `FirebaseApp` created for the active tenant. */
        const val DEFAULT_TENANT_APP_NAME: String = "kindling-tenant"
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result -> cont.resume(result) }
    addOnFailureListener { e -> cont.resumeWithException(e) }
    addOnCanceledListener { cont.cancel() }
}
