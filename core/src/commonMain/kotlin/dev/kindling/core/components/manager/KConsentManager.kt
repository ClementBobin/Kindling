package dev.kindling.core.components.manager

import androidx.compose.runtime.*
import dev.kindling.core.components.ui.consent.KConsentCategory
import dev.kindling.core.components.ui.consent.KConsentCategoryType
import dev.kindling.core.components.ui.consent.KConsentPreferences
import dev.kindling.core.components.ui.consent.KDefaultConsentCategories

// ─── State Holder ─────────────────────────────────────────────────────────────

@Stable
class KConsentManager internal constructor(
    val preferences: KConsentPreferences?,
    val categories: List<KConsentCategory>,
    private val onUpdate: (KConsentPreferences) -> Unit
) {
    /** True if the user hasn't made a choice yet */
    val isConsentPending: Boolean get() = preferences == null

    fun isEnabled(category: KConsentCategoryType): Boolean =
        preferences?.isEnabled(category) ?: false

    fun acceptAll() {
        onUpdate(KConsentPreferences.allAccepted(categories))
    }

    fun rejectOptional() {
        onUpdate(KConsentPreferences.onlyNecessary(categories))
    }

    fun update(newPreferences: KConsentPreferences) {
        onUpdate(newPreferences)
    }
}

// ─── CompositionLocal (Context) ───────────────────────────────────────────────

val LocalKConsentManager = staticCompositionLocalOf<KConsentManager> {
    error("LocalKConsentManager not provided! Wrap your app tree in KConsentProvider.")
}

/**
 * Custom Hook: Access consent state and actions anywhere in the Compose hierarchy.
 */
@Composable
fun rememberConsent(): KConsentManager = LocalKConsentManager.current
 
// ─── Provider Component ───────────────────────────────────────────────────────

/**
 * Top-level Provider that manages state and exposes it down the tree.
 */
@Composable
fun KConsentProvider(
    initialPreferences: KConsentPreferences? = null,
    categories: List<KConsentCategory> = KDefaultConsentCategories,
    onConsentChanged: (KConsentPreferences) -> Unit = {},
    content: @Composable () -> Unit
) {
    var preferencesState by remember { mutableStateOf(initialPreferences) }

    val manager = remember(preferencesState, categories) {
        KConsentManager(
            preferences = preferencesState,
            categories = categories,
            onUpdate = { newPrefs ->
                preferencesState = newPrefs
                onConsentChanged(newPrefs)
            }
        )
    }

    CompositionLocalProvider(LocalKConsentManager provides manager) {
        content()
    }
}