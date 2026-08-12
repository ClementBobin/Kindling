package dev.kindling.core.components.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.kindling.core.theme.kindlingShapes

/**
 * Modal dialog for granular consent customization (c15t preference modal).
 */
@Composable
fun KConsentPreferencesDialog(
    categories: List<KConsentCategory>,
    currentPreferences: KConsentPreferences,
    onSavePreferences: (KConsentPreferences) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var workingPrefs by remember(currentPreferences) { mutableStateOf(currentPreferences) }
    val cs = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 560.dp)
                .padding(vertical = 24.dp),
            shape = MaterialTheme.kindlingShapes.radiusLg,
            color = cs.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cookie,
                            contentDescription = null,
                            tint = cs.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Privacy Preferences",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = cs.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = cs.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Manage your consent settings below. Essential services are required for security and basic app operation.",
                    fontSize = 13.sp,
                    color = cs.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.5f))

                // Scrollable List of Categories
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        KConsentCategoryRow(
                            category = category,
                            isEnabled = workingPrefs.isEnabled(category.type),
                            onToggle = { enabled ->
                                workingPrefs = workingPrefs.withCategory(category.type, enabled)
                            }
                        )
                    }
                }

                HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.5f))

                // Action Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onSavePreferences(KConsentPreferences.onlyNecessary(categories)) },
                        shape = MaterialTheme.kindlingShapes.radiusMd
                    ) {
                        Text("Reject Optional", fontSize = 13.sp)
                    }

                    Button(
                        onClick = { onSavePreferences(workingPrefs) },
                        shape = MaterialTheme.kindlingShapes.radiusMd
                    ) {
                        Text("Save Choices", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}