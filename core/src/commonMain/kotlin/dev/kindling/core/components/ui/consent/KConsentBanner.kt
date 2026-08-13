package dev.kindling.core.components.ui.consent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.theme.kindlingShapes

/**
 * Complete, ready-to-use c15t-style Privacy & Consent Banner / Card component.
 */
@Composable
fun KConsentBanner(
    onAcceptAll: () -> Unit,
    onRejectOptional: () -> Unit,
    onSaveCustomPreferences: (KConsentPreferences) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "We respect your privacy",
    description: String = "We use cookies and similar technologies to enhance navigation, analyze site traffic, and personalize content. You can manage your choices at any time.",
    categories: List<KConsentCategory> = KDefaultConsentCategories,
    initialPreferences: KConsentPreferences = KConsentPreferences(),
    shape: Shape = MaterialTheme.kindlingShapes.radiusLg,
    elevation: Dp = 4.dp
) {
    var showPreferencesDialog by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = cs.surface,
        tonalElevation = elevation,
        shadowElevation = elevation,
        border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.kindlingShapes.radiusSm)
                        .background(cs.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = cs.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface
                    )
                    Text(
                        text = description,
                        fontSize = 12.5.sp,
                        color = cs.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showPreferencesDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.kindlingShapes.radiusMd
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Customize", fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                OutlinedButton(
                    onClick = onRejectOptional,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.kindlingShapes.radiusMd
                ) {
                    Text("Decline Optional", fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Button(
                    onClick = onAcceptAll,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.kindlingShapes.radiusMd
                ) {
                    Text("Accept All", fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    if (showPreferencesDialog) {
        KConsentPreferencesDialog(
            categories = categories,
            currentPreferences = initialPreferences,
            onSavePreferences = { selectedPrefs ->
                showPreferencesDialog = false
                onSaveCustomPreferences(selectedPrefs)
            },
            onDismiss = { showPreferencesDialog = false }
        )
    }
}