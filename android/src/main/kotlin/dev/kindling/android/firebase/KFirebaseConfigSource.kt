package dev.kindling.android.firebase

import android.content.Context
import android.net.Uri
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Where a [KFirebaseConfig] comes from.
 *
 * Kindling ships two sources, which cover the two ways a client can provide its Firebase
 * project:
 * - [KFirebaseApiConfigSource]: the configuration is downloaded from your backend.
 * - [KFirebaseFileConfigSource]: the configuration is a `google-services.json` that is already
 *   on the device (app files, assets or a file picked by the user).
 *
 * Implement this interface for any other origin (remote config, QR code, deep link, ...).
 */
fun interface KFirebaseConfigSource {

    /**
     * Loads and validates the configuration.
     *
     * @throws KFirebaseConfigException If the configuration is missing or invalid.
     */
    suspend fun load(): KFirebaseConfig
}

/**
 * Loads the Firebase configuration of the current client from your backend.
 *
 * The backend answers with the **public** client configuration only, either as a flat object
 * (`apiKey`, `applicationId`, `projectId`, `messagingSenderId`) or as a full `google-services.json`
 * (see [KFirebaseConfig.fromJson]). Never return the service account key: it is rejected.
 *
 * ### Example usage:
 * ```kotlin
 * // With the Kindling HTTP client (token refresh and error mapping included)
 * val source = KFirebaseApiConfigSource.fromClient(
 *     client = httpClient,
 *     path = "clients/$clientId/firebase",
 *     packageName = context.packageName,
 * )
 *
 * // With any other networking stack
 * val source = KFirebaseApiConfigSource { api.getFirebaseConfig(clientId).string() }
 *
 * val app = firebaseManager.initialize(tenantId = clientId, source = source)
 * ```
 *
 * @param packageName Package name expected when the response is a `google-services.json`.
 * @param fetchJson Returns the raw JSON body of the response.
 */
class KFirebaseApiConfigSource(
    private val packageName: String? = null,
    private val fetchJson: suspend () -> String,
) : KFirebaseConfigSource {

    override suspend fun load(): KFirebaseConfig =
        KFirebaseConfig.fromJson(fetchJson(), packageName)

    companion object {

        /**
         * Creates a source that sends a `GET` with a Ktor [HttpClient], typically the one built by
         * [dev.kindling.android.http.buildKHttpClient].
         *
         * @param client Client used for the request. Its base URL, auth and error handling apply.
         * @param path Path (or absolute URL) of the endpoint that returns the configuration.
         * @param packageName Package name expected when the response is a `google-services.json`.
         * @param request Optional request customisation (query, headers).
         */
        fun fromClient(
            client: HttpClient,
            path: String,
            packageName: String? = null,
            request: HttpRequestBuilder.() -> Unit = {},
        ): KFirebaseApiConfigSource = KFirebaseApiConfigSource(packageName) {
            client.get(path, request).bodyAsText()
        }
    }
}

/**
 * Loads the Firebase configuration from a `google-services.json` stored on the device.
 *
 * Use it when the client already has the file: copied into the app's private storage, bundled in
 * the assets, or chosen with the system file picker. Only the **client** file is accepted; a
 * service account key is rejected with a [KFirebaseConfigException].
 *
 * The `package_name` found in the file must equal [packageName] (by default the package name of
 * your app), because the Firebase project must have an Android app registered with it.
 *
 * ### Example usage:
 * ```kotlin
 * // File in the app's private storage
 * val source = KFirebaseFileConfigSource.fromFile(File(context.filesDir, "google-services.json"), context.packageName)
 *
 * // File chosen by the user with ActivityResultContracts.OpenDocument
 * val source = KFirebaseFileConfigSource.fromUri(context, pickedUri)
 *
 * // File bundled in src/main/assets
 * val source = KFirebaseFileConfigSource.fromAsset(context, "clients/acme/google-services.json")
 *
 * val app = firebaseManager.initialize(tenantId = "acme", source = source)
 * ```
 *
 * @param packageName Package name expected in the file, usually `context.packageName`. `null`
 *   disables the check and uses the first client entry.
 * @param readJson Reads the raw text of the file. It is called on [Dispatchers.IO].
 */
class KFirebaseFileConfigSource(
    private val packageName: String?,
    private val readJson: () -> String,
) : KFirebaseConfigSource {

    override suspend fun load(): KFirebaseConfig {
        val text = try {
            withContext(Dispatchers.IO) { readJson() }
        } catch (e: java.io.IOException) {
            throw KFirebaseConfigException("Cannot read the Firebase configuration file", e)
        }
        return KFirebaseConfig.fromJson(text, packageName)
    }

    companion object {

        /**
         * Reads a file from the file system.
         *
         * @param file The `google-services.json` file.
         * @param packageName Package name expected in the file, usually `context.packageName`.
         */
        fun fromFile(file: File, packageName: String?): KFirebaseFileConfigSource =
            KFirebaseFileConfigSource(packageName) { file.readText() }

        /**
         * Reads a document chosen by the user (Storage Access Framework or any `content://` Uri).
         *
         * @param context Used to resolve the Uri. The package name check uses `context.packageName`.
         * @param uri Uri of the `google-services.json` document.
         */
        fun fromUri(context: Context, uri: Uri): KFirebaseFileConfigSource {
            val appContext = context.applicationContext
            return KFirebaseFileConfigSource(appContext.packageName) {
                appContext.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: throw java.io.IOException("Cannot open $uri")
            }
        }

        /**
         * Reads a file bundled in the app's `assets`.
         *
         * @param context Used to open the asset. The package name check uses `context.packageName`.
         * @param assetPath Path of the file inside `assets`.
         */
        fun fromAsset(context: Context, assetPath: String): KFirebaseFileConfigSource {
            val appContext = context.applicationContext
            return KFirebaseFileConfigSource(appContext.packageName) {
                appContext.assets.open(assetPath).bufferedReader().use { it.readText() }
            }
        }
    }
}
