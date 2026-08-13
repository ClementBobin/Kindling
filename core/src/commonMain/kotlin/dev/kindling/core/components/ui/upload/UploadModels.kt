package dev.kindling.core.components.ui.upload

/**
 * Represents the current status of an individual file in the uploader.
 */
sealed interface UploadStatus {
    data object Idle : UploadStatus
    data object Uploading : UploadStatus
    data object Success : UploadStatus
    data class Error(val message: String) : UploadStatus
}

/**
 * State model for a file managed by the uploader.
 */
data class UploadFile(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String? = null,
    val status: UploadStatus = UploadStatus.Idle,
    val progress: Float = 0f, // 0.0f .. 1.0f
)