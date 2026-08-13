package dev.kindling.core.components.ui.upload

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Complete, ready-to-use upload area preset with file list and drag-drop cues.
 */
@Composable
fun KUploadArea(
    files: List<UploadFile>,
    onSelectFiles: () -> Unit,
    onRemoveFile: (UploadFile) -> Unit,
    modifier: Modifier = Modifier,
    onRetryFile: ((UploadFile) -> Unit)? = null,
    titleText: String = "Click to upload or drag & drop",
    maxFileSizeText: String? = "Max file size: 10MB",
    acceptedTypesText: String? = null,
    enabled: Boolean = true,
    isDragging: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KDropzone(
            onClick = onSelectFiles,
            enabled = enabled,
            isHoveredOrDragging = isDragging
        ) {
            KDropzoneHeader {
                KDropzoneMedia {
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                KDropzoneTitle(text = titleText)

                val subtitle = listOfNotNull(acceptedTypesText, maxFileSizeText)
                    .joinToString(" • ")
                if (subtitle.isNotEmpty()) {
                    KDropzoneDescription(text = subtitle)
                }
            }
        }

        AnimatedVisibility(
            visible = files.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            KFileList(
                files = files,
                onRemoveFile = onRemoveFile,
                onRetryFile = onRetryFile
            )
        }
    }
}