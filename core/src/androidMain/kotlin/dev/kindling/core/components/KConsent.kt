package dev.kindling.core.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.kindling.core.theme.kindlingShapes

// ─── Models ───────────────────────────────────────────────────────────────────
 
/**
 * Standard cookie & privacy consent categories.
 */
enum class KConsentCategoryType {
    NECESSARY,
    ANALYTICS,
    MARKETING,
    FUNCTIONAL
}

/**
 * Describes a single consent category with metadata.
 */
data class KConsentCategory(
    val type: KConsentCategoryType,
    val title: String,
    val description: String,
    val isRequired: Boolean = false,
    val isEnabledByDefault: Boolean = false,
    val icon: ImageVector = Icons.Outlined.Shield
)

/**
 * Holds user choices for consent categories.
 */
data class KConsentPreferences(
    val selections: Map<KConsentCategoryType, Boolean> = mapOf(
        KConsentCategoryType.NECESSARY to true,
        KConsentCategoryType.ANALYTICS to false,
        KConsentCategoryType.MARKETING to false,
        KConsentCategoryType.FUNCTIONAL to false
    )
) {
    fun isEnabled(category: KConsentCategoryType): Boolean = selections[category] ?: false

    fun withCategory(category: KConsentCategoryType, enabled: Boolean): KConsentPreferences {
        if (category == KConsentCategoryType.NECESSARY) return this // Mandatory
        return copy(selections = selections + (category to enabled))
    }

    companion object {
        fun allAccepted(categories: List<KConsentCategory>): KConsentPreferences =
            KConsentPreferences(categories.associate { it.type to true })

        fun onlyNecessary(categories: List<KConsentCategory>): KConsentPreferences =
            KConsentPreferences(categories.associate { it.type to (it.isRequired) })
    }
}

// ─── Default Category Presets ────────────────────────────────────────────────

val KDefaultConsentCategories = listOf(
    KConsentCategory(
        type = KConsentCategoryType.NECESSARY,
        title = "Essential & Security",
        description = "Required for basic site navigation, authentication, and core functionality. Cannot be turned off.",
        isRequired = true,
        isEnabledByDefault = true,
        icon = Icons.Outlined.Lock
    ),
    KConsentCategory(
        type = KConsentCategoryType.ANALYTICS,
        title = "Performance & Analytics",
        description = "Helps us understand how visitors interact with the app by collecting and reporting anonymized information.",
        isRequired = false,
        isEnabledByDefault = false,
        icon = Icons.Outlined.Insights
    ),
    KConsentCategory(
        type = KConsentCategoryType.FUNCTIONAL,
        title = "Functional Preferences",
        description = "Enables enhanced features such as remembering your region, theme settings, and saved choices.",
        isRequired = false,
        isEnabledByDefault = true,
        icon = Icons.Outlined.Tune
    ),
    KConsentCategory(
        type = KConsentCategoryType.MARKETING,
        title = "Marketing & Personalization",
        description = "Used to deliver relevant advertisements and track marketing campaign effectiveness across channels.",
        isRequired = false,
        isEnabledByDefault = false,
        icon = Icons.Outlined.Campaign
    )
)

// ─── Subcomponents ────────────────────────────────────────────────────────────

@Composable
fun KConsentCategoryRow(
    category: KConsentCategory,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.kindlingShapes.radiusMd
) {
    val cs = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = cs.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.kindlingShapes.radiusSm)
                    .background(cs.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = category.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurface
                    )
                    if (category.isRequired) {
                        Surface(
                            shape = MaterialTheme.kindlingShapes.radiusSm,
                            color = cs.outlineVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Always Active",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = category.description,
                    fontSize = 12.sp,
                    color = cs.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Switch(
                checked = category.isRequired || isEnabled,
                onCheckedChange = if (category.isRequired) null else onToggle,
                enabled = !category.isRequired
            )
        }
    }
}

// ─── KConsentPreferencesDialog ───────────────────────────────────────────────

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

// ─── High-Level Preset: KConsentBanner ────────────────────────────────────────

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
        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.6f))
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