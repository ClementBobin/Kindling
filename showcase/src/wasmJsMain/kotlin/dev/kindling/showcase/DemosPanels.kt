package dev.kindling.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonSize
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.core.components.ui.consent.KConsentBanner
import dev.kindling.core.components.ui.consent.KConsentPreferences
import dev.kindling.core.components.ui.dashboard.KDashboardGrid
import dev.kindling.core.components.ui.direction.KDirectionProvider
import dev.kindling.core.components.ui.direction.isRtl
import dev.kindling.core.components.ui.layout.KCenteredBox
import dev.kindling.core.components.ui.layout.KCenteredColumn
import dev.kindling.core.components.ui.layout.KLargeSpacer
import dev.kindling.core.components.ui.layout.KMediumSpacer
import dev.kindling.core.components.ui.layout.KSmallSpacer
import dev.kindling.core.components.ui.layout.dashedBorder
import dev.kindling.core.components.ui.upload.KUploadArea
import dev.kindling.core.components.ui.upload.KUploadFile
import dev.kindling.core.components.ui.upload.KUploadStatus
import kotlinx.coroutines.delay

// ── Dashboard ────────────────────────────────────────────────────────────────

@Composable
fun DashboardDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Drag the tiles to rearrange them; the action tile is clickable.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        var clicks by remember { mutableStateOf(0) }
        KDashboardGrid(cellSize = 72.dp) {
            item(id = "revenue", initialColumn = 0, initialRow = 0, widthCells = 2, heightCells = 1) {
                DashboardDemoTile("Revenue", "$12.4k")
            }
            item(id = "users", initialColumn = 2, initialRow = 0, widthCells = 2, heightCells = 1) {
                DashboardDemoTile("Users", "1,284")
            }
            item(id = "orders", initialColumn = 0, initialRow = 1, widthCells = 1, heightCells = 1) {
                DashboardDemoTile("Orders", "96")
            }
            actionItem(
                id = "add",
                initialColumn = 1,
                initialRow = 1,
                widthCells = 2,
                heightCells = 1,
                onClick = { clicks++ }
            ) {
                DashboardDemoTile("Add widget", "Clicked $clicks")
            }
        }
    }
}

@Composable
private fun DashboardDemoTile(title: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── Consent ──────────────────────────────────────────────────────────────────

@Composable
fun ConsentDemo() {
    var result by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (result == null) {
            KConsentBanner(
                onAcceptAll = { result = "All cookies accepted" },
                onRejectOptional = { result = "Only necessary cookies" },
                onSaveCustomPreferences = { prefs: KConsentPreferences ->
                    val n = prefs.selections.count { it.value }
                    result = "Custom preferences saved ($n enabled)"
                }
            )
        } else {
            Text(result ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            KButton(
                text = "Reset",
                onClick = { result = null },
                variant = KButtonVariant.Outline,
                size = KButtonSize.Sm
            )
        }
    }
}

// ── Layout ───────────────────────────────────────────────────────────────────

@Composable
fun LayoutDemo() {
    val cs = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("KCenteredBox", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        KSmallSpacer()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .dashedBorder(cs.outline, RoundedCornerShape(8.dp))
        ) {
            KCenteredBox { Text("Centered", color = cs.onSurface) }
        }
        KMediumSpacer()
        Text("KCenteredColumn", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        KSmallSpacer()
        KCenteredColumn(
            modifier = Modifier
                .dashedBorder(cs.outline, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text("First", color = cs.onSurface)
            KSmallSpacer()
            Text("Second", color = cs.onSurface)
            KLargeSpacer()
            Text("Third (after a large spacer)", color = cs.onSurface)
        }
    }
}

// ── Direction ────────────────────────────────────────────────────────────────

@Composable
fun DirectionDemo() {
    var rtl by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KButton(
            text = if (rtl) "Switch to LTR" else "Switch to RTL",
            onClick = { rtl = !rtl },
            variant = KButtonVariant.Outline,
            size = KButtonSize.Sm
        )
        KDirectionProvider(direction = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            DirectionDemoPanel()
        }
    }
}

@Composable
private fun DirectionDemoPanel() {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cs.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (isRtl()) "Direction: RTL" else "Direction: LTR",
            style = MaterialTheme.typography.titleSmall,
            color = cs.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(32.dp).background(cs.primary, RoundedCornerShape(6.dp)))
            Text("Leading item", color = cs.onSurface, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = cs.onSurfaceVariant)
        }
        Text(
            "Rows start from the leading edge and the arrow icon mirrors automatically.",
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant
        )
    }
}

// ── Upload ───────────────────────────────────────────────────────────────────

@Composable
fun UploadDemo() {
    var files by remember { mutableStateOf(listOf<KUploadFile>()) }
    var counter by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(250)
            if (files.any { it.status == KUploadStatus.Uploading }) {
                files = files.map { f ->
                    if (f.status == KUploadStatus.Uploading) {
                        val p = f.progress + 0.2f
                        if (p >= 1f) f.copy(progress = 1f, status = KUploadStatus.Success)
                        else f.copy(progress = p)
                    } else f
                }
            }
        }
    }

    KUploadArea(
        files = files,
        onSelectFiles = {
            counter++
            files = files + KUploadFile(
                id = "file-$counter",
                name = "photo-$counter.png",
                sizeBytes = 240_000L + counter * 1_000L,
                mimeType = "image/png",
                status = KUploadStatus.Uploading,
                progress = 0f
            )
        },
        onRemoveFile = { f -> files = files.filter { it.id != f.id } },
        acceptedTypesText = "Simulated: each click adds a fake PNG"
    )
}
