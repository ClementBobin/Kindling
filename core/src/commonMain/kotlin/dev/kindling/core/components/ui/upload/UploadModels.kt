package dev.kindling.core.components.ui.upload

/**
 * Represents the current status of an individual file in the uploader.
 */
sealed interface KUploadStatus {
    data object Idle : KUploadStatus
    data object Uploading : KUploadStatus
    data object Success : KUploadStatus
    data class Error(val message: String) : KUploadStatus
}

/**
 * State model for a file managed by the uploader.
 */
data class KUploadFile(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String? = null,
    val status: KUploadStatus = KUploadStatus.Idle,
    val progress: Float = 0f, // 0.0f .. 1.0f
)