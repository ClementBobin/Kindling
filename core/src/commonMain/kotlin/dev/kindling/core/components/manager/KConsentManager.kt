package dev.kindling.core.components.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// ─── State Holder ─────────────────────────────────────────────────────────────

@Stable
class KConsentManager internal constructor(
    val preferences: KConsentPreferences?,
    val categories: ImmutableList<KConsentCategory>,
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
    onConsentChange: (KConsentPreferences) -> Unit = {},
    content: @Composable () -> Unit
) {
    var preferencesState by remember { mutableStateOf(initialPreferences) }
    val immutableCategories = remember(categories) { categories.toImmutableList() }

    val manager = remember(preferencesState, immutableCategories) {
        KConsentManager(
            preferences = preferencesState,
            categories = immutableCategories,
            onUpdate = { newPrefs ->
                preferencesState = newPrefs
                onConsentChange(newPrefs)
            }
        )
    }

    CompositionLocalProvider(LocalKConsentManager provides manager) {
        content()
    }
}