package dev.kindling.core.components.ui.consent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Tune

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