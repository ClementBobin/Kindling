package dev.kindling.core.components.ui.consent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector

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