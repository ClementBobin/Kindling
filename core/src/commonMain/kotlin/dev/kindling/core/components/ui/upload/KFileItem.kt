package dev.kindling.core.components.ui.upload

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.kindlingShapes
import dev.kindling.utils.method.format.system.bytesToHuman

/**
 * Resolves an appropriate icon based on file extension or mime type.
 */
private fun resolveFileIcon(fileName: String, mimeType: String?): ImageVector {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    val type = mimeType?.lowercase() ?: ""

    return when {
        type.startsWith("image/") || ext in listOf("jpg", "jpeg", "png", "webp", "gif", "svg") -> Icons.Outlined.Image
        type.startsWith("video/") || ext in listOf("mp4", "mkv", "mov", "avi") -> Icons.Outlined.Movie
        type.startsWith("audio/") || ext in listOf("mp3", "wav", "aac", "flac") -> Icons.Outlined.AudioFile
        type.contains("pdf") || ext == "pdf" -> Icons.Outlined.PictureAsPdf
        type.contains("zip") || type.contains("compressed") || ext in listOf("zip", "tar", "gz", "7z", "rar") -> Icons.Outlined.FolderZip
        ext in listOf("kt", "java", "ts", "js", "html", "css", "json", "xml") -> Icons.Outlined.Code
        else -> Icons.Outlined.Description
    }
}

/**
 * Displays a single file item with its status, progress, and actions.
 */
@Composable
fun KFileItem(
    file: UploadFile,
    onRemove: (UploadFile) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: ((UploadFile) -> Unit)? = null,
    shape: Shape = MaterialTheme.kindlingShapes.radiusMd
) {
    val cs = MaterialTheme.colorScheme
    val animatedProgress by animateFloatAsState(targetValue = file.progress, label = "UploadProgress")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = cs.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // File Type Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(MaterialTheme.kindlingShapes.radiusSm)
                        .background(cs.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = resolveFileIcon(file.name, file.mimeType),
                        contentDescription = null,
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // File Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = when (file.status) {
                            is UploadStatus.Uploading -> "${(animatedProgress * 100).toInt()}% • ${file.sizeBytes.bytesToHuman()}"
                            is UploadStatus.Error -> (file.status).message
                            is UploadStatus.Success -> "Completed • ${file.sizeBytes.bytesToHuman()}"
                            is UploadStatus.Idle -> file.sizeBytes.bytesToHuman()
                        },
                        fontSize = 11.sp,
                        color = if (file.status is UploadStatus.Error) cs.error else cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status Indicator or Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when (file.status) {
                        is UploadStatus.Success -> {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Completed",
                                tint = cs.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        is UploadStatus.Error -> {
                            if (onRetry != null) {
                                IconButton(
                                    onClick = { onRetry(file) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Retry",
                                        tint = cs.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        else -> {}
                    }

                    IconButton(
                        onClick = { onRemove(file) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Remove file",
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Progress Bar (when uploading)
            AnimatedVisibility(
                visible = file.status is UploadStatus.Uploading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = cs.primary,
                    trackColor = cs.surfaceVariant
                )
            }
        }
    }
}

/**
 * Animated list container for managing file items.
 */
@Composable
fun KFileList(
    files: List<UploadFile>,
    onRemoveFile: (UploadFile) -> Unit,
    modifier: Modifier = Modifier,
    onRetryFile: ((UploadFile) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        files.forEach { file ->
            key(file.id) {
                KFileItem(
                    file = file,
                    onRemove = onRemoveFile,
                    onRetry = onRetryFile
                )
            }
        }
    }
}