package dev.kindling.android.firebase

/**
 * The **public**, client-side identifiers of a Firebase project, as a plain DTO.
 *
 * Fill it from wherever your app gets it (your API response, a file, a QR code...) and pass it to
 * [KFirebaseManager.initialize]. These are the same values found in `google-services.json`; they
 * contain no secret and are safe to keep on the device. Never put the backend *service account*
 * key (`private_key`, `client_email`) here: it must stay on your server.
 *
 * ### Example usage:
 * ```kotlin
 * // Map your own API DTO to the Firebase one
 * val config = KFirebaseConfig(
 *     apiKey = dto.apiKey,
 *     applicationId = dto.applicationId,
 *     projectId = dto.projectId,
 *     gcmSenderId = dto.messagingSenderId,
 * )
 * firebaseManager.initialize(tenantId = clientId, config = config)
 * ```
 *
 * @param apiKey Public API key of the Firebase project (`current_key` in `google-services.json`).
 * @param applicationId Firebase app id (`mobilesdk_app_id`, like `1:123456789012:android:abc...`).
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

    /** Never prints the API key. */
    override fun toString(): String =
        "KFirebaseConfig(projectId=$projectId, applicationId=$applicationId, gcmSenderId=$gcmSenderId)"
}
