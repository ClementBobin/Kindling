package dev.kindling.core.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.kindlingShapes
import dev.kindling.utils.method.format.system.bytesToHuman

// ─── Models ───────────────────────────────────────────────────────────────────

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

// ─── Dashed Border Modifier ──────────────────────────────────────────────────

/**
 * Draws a dashed border around a composable using custom stroke parameters.
 */
fun Modifier.dashedBorder(
    color: Color,
    shape: Shape,
    strokeWidth: Dp = 1.dp,
    dashWidth: Dp = 6.dp,
    gapWidth: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()
    val strokeWidthPx = strokeWidth.toPx()
    val dashWidthPx = dashWidth.toPx()
    val gapWidthPx = gapWidth.toPx()

    val outline = shape.createOutline(size, layoutDirection, this)
    val path = androidx.compose.ui.graphics.Path().apply {
        when (outline) {
            is androidx.compose.ui.graphics.Outline.Rectangle -> addRect(outline.rect)
            is androidx.compose.ui.graphics.Outline.Rounded -> addRoundRect(outline.roundRect)
            is androidx.compose.ui.graphics.Outline.Generic -> addPath(outline.path)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashWidthPx, gapWidthPx),
                0f
            )
        )
    )
}

// ─── KDropzone (Container) ───────────────────────────────────────────────────

/**
 * Base dropzone container component inspired by Better Upload / shadcn.
 */
@Composable
fun KDropzone(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHoveredOrDragging: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.kindlingShapes.radiusLg,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }

    val borderColor = when {
        !enabled -> cs.outlineVariant.copy(alpha = 0.5f)
        isHoveredOrDragging -> cs.primary
        else -> cs.outline.copy(alpha = 0.5f)
    }

    val backgroundColor = when {
        isHoveredOrDragging -> cs.primary.copy(alpha = 0.04f)
        else -> cs.surfaceVariant.copy(alpha = 0.25f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .dashedBorder(
                color = borderColor,
                shape = shape,
                strokeWidth = if (isHoveredOrDragging) 2.dp else 1.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}

// ─── KDropzone Subcomponents ─────────────────────────────────────────────────

@Composable
fun KDropzoneHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun KDropzoneMedia(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun KDropzoneTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )
}

@Composable
fun KDropzoneDescription(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 16.sp
    )
}

// ─── Icon Helper ─────────────────────────────────────────────────────────────

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

// ─── KFileItem Component ─────────────────────────────────────────────────────

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
        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.5f))
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
                            is UploadStatus.Error -> (file.status as UploadStatus.Error).message
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

// ─── KFileList Container ─────────────────────────────────────────────────────

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

// ─── High-Level Preset: KUploadArea ──────────────────────────────────────────

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