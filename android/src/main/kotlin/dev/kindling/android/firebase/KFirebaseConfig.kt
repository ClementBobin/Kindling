package dev.kindling.android.firebase

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/**
 * The **public**, client-side identifiers of a Firebase project.
 *
 * This is exactly the information stored in `google-services.json`. It contains no secret
 * (the API key only identifies the project) and is safe to keep on the device. It is **not**
 * the backend *service account* key (`private_key`, `client_email`, ...): that file must stay on
 * your server and is rejected by every parser in this class.
 *
 * Use it to initialise Firebase at runtime, so one single app can talk to a different Firebase
 * project per client instead of embedding one static `google-services.json`:
 * - [fromJson] parses a payload returned by your API **or** a `google-services.json` file read
 *   from the device.
 * - [KFirebaseManager] turns it into a running `FirebaseApp` and an FCM token.
 *
 * ### Example usage:
 * ```kotlin
 * // From your API: {"apiKey":"...","applicationId":"...","projectId":"...","messagingSenderId":"..."}
 * val fromApi = KFirebaseConfig.fromJson(apiResponseBody)
 *
 * // From a google-services.json stored on the device (package name is checked)
 * val fromFile = KFirebaseConfig.fromJson(fileText, packageName = context.packageName)
 * ```
 *
 * @param apiKey Public Web/Android API key of the Firebase project (`current_key`).
 * @param applicationId Firebase app id (`mobilesdk_app_id`, for example `1:123456789012:android:abc...`).
 * @param projectId Firebase project id.
 * @param gcmSenderId FCM sender id (the project number, `messagingSenderId`).
 * @param storageBucket Optional Cloud Storage bucket.
 */
data class KFirebaseConfig(
    val apiKey: String,
    val applicationId: String,
    val projectId: String,
    val gcmSenderId: String,
    val storageBucket: String? = null,
) {
    init {
        require(apiKey.isNotBlank()) { "Firebase apiKey must not be blank" }
        require(applicationId.isNotBlank()) { "Firebase applicationId must not be blank" }
        require(projectId.isNotBlank()) { "Firebase projectId must not be blank" }
        require(gcmSenderId.isNotBlank()) { "Firebase gcmSenderId must not be blank" }
    }

    /** Serialises this configuration to a flat JSON object that [fromJson] can read back. */
    fun toJson(): String = buildJsonObject {
        put("apiKey", apiKey)
        put("applicationId", applicationId)
        put("projectId", projectId)
        put("messagingSenderId", gcmSenderId)
        storageBucket?.let { put("storageBucket", it) }
    }.toString()

    /** Never prints the API key. */
    override fun toString(): String =
        "KFirebaseConfig(projectId=$projectId, applicationId=$applicationId, gcmSenderId=$gcmSenderId)"

    companion object {

        private val json = Json { isLenient = true }

        /**
         * Parses a Firebase client configuration. Two shapes are accepted and detected
         * automatically:
         *
         * 1. **Flat object** (typical API response). Accepted keys, first match wins:
         *    `apiKey` / `api_key`, `applicationId` / `appId` / `app_id` / `mobilesdk_app_id`,
         *    `projectId` / `project_id`, `messagingSenderId` / `gcmSenderId` / `senderId` /
         *    `messaging_sender_id` / `project_number`, and optionally `storageBucket` /
         *    `storage_bucket`.
         * 2. **`google-services.json`** (an object with `project_info` and `client`). When
         *    [packageName] is given, the `client` entry with that exact package name is used and a
         *    mismatch is an error (the tenant must register an Android app with *your* package
         *    name). Without [packageName] the first client entry is used.
         *
         * A service account key (`"type": "service_account"` or a `private_key` field) is always
         * rejected: it is a secret that must never be shipped to a device.
         *
         * @param jsonText The JSON document.
         * @param packageName Package name expected in `google-services.json`, usually
         *   `context.packageName`.
         * @throws KFirebaseConfigException If the JSON is malformed, is a service account, or
         *   misses a required field.
         */
        fun fromJson(jsonText: String, packageName: String? = null): KFirebaseConfig {
            val root = try {
                json.parseToJsonElement(jsonText)
            } catch (e: Exception) {
                throw KFirebaseConfigException("Firebase configuration is not valid JSON", e)
            }
            val obj = root as? JsonObject
                ?: throw KFirebaseConfigException("Firebase configuration must be a JSON object")

            rejectServiceAccount(obj)

            return if ("project_info" in obj || "client" in obj) {
                fromGoogleServices(obj, packageName)
            } else {
                fromFlat(obj)
            }
        }

        private fun rejectServiceAccount(obj: JsonObject) {
            val type = obj["type"].text()
            if (type == "service_account" || "private_key" in obj || "private_key_id" in obj) {
                throw KFirebaseConfigException(
                    "This looks like a Firebase service account key. It is a secret for your " +
                        "backend and must not be used on a device. Use google-services.json " +
                        "(or the public client config) instead."
                )
            }
        }

        private fun fromFlat(obj: JsonObject): KFirebaseConfig = build(
            apiKey = obj.firstText("apiKey", "api_key"),
            applicationId = obj.firstText("applicationId", "appId", "app_id", "mobilesdk_app_id"),
            projectId = obj.firstText("projectId", "project_id"),
            gcmSenderId = obj.firstText(
                "messagingSenderId", "gcmSenderId", "senderId", "messaging_sender_id", "project_number"
            ),
            storageBucket = obj.firstText("storageBucket", "storage_bucket"),
        )

        private fun fromGoogleServices(obj: JsonObject, packageName: String?): KFirebaseConfig {
            val info = obj["project_info"] as? JsonObject
                ?: throw KFirebaseConfigException("google-services.json has no project_info")
            val clients = (obj["client"] as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty()
            if (clients.isEmpty()) {
                throw KFirebaseConfigException("google-services.json has no client entry")
            }

            fun JsonObject.packageOf(): String? =
                (this["client_info"] as? JsonObject)
                    ?.let { it["android_client_info"] as? JsonObject }
                    ?.get("package_name").text()

            val client = if (packageName == null) {
                clients.first()
            } else {
                clients.firstOrNull { it.packageOf() == packageName }
                    ?: throw KFirebaseConfigException(
                        "google-services.json has no Android app with package name " +
                            "'$packageName' (found: ${clients.mapNotNull { it.packageOf() }})"
                    )
            }

            val appId = (client["client_info"] as? JsonObject)?.get("mobilesdk_app_id").text()
            val apiKey = (client["api_key"] as? JsonArray)
                ?.firstNotNullOfOrNull { (it as? JsonObject)?.get("current_key").text() }

            return build(
                apiKey = apiKey,
                applicationId = appId,
                projectId = info["project_id"].text(),
                gcmSenderId = info["project_number"].text(),
                storageBucket = info["storage_bucket"].text(),
            )
        }

        private fun build(
            apiKey: String?,
            applicationId: String?,
            projectId: String?,
            gcmSenderId: String?,
            storageBucket: String?,
        ): KFirebaseConfig {
            val missing = buildList {
                if (apiKey.isNullOrBlank()) add("apiKey")
                if (applicationId.isNullOrBlank()) add("applicationId")
                if (projectId.isNullOrBlank()) add("projectId")
                if (gcmSenderId.isNullOrBlank()) add("messagingSenderId")
            }
            if (missing.isNotEmpty()) {
                throw KFirebaseConfigException("Firebase configuration is missing: ${missing.joinToString()}")
            }
            return KFirebaseConfig(
                apiKey = apiKey!!.trim(),
                applicationId = applicationId!!.trim(),
                projectId = projectId!!.trim(),
                gcmSenderId = gcmSenderId!!.trim(),
                storageBucket = storageBucket?.trim()?.takeIf { it.isNotEmpty() },
            )
        }

        private fun JsonElement?.text(): String? = (this as? JsonPrimitive)?.contentOrNull

        private fun JsonObject.firstText(vararg keys: String): String? =
            keys.firstNotNullOfOrNull { key -> this[key].text()?.takeIf { it.isNotBlank() } }
    }
}

/**
 * Thrown when a Firebase configuration cannot be read, parsed or validated.
 *
 * The message is safe to log: it never contains the content of the JSON that was parsed.
 */
class KFirebaseConfigException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)
