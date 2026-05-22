package dev.kindling.compose

import dev.kindling.compose.experimental.Effect
import dev.kindling.compose.experimental.Intent
import dev.kindling.compose.experimental.KMviViewModel
import dev.kindling.compose.experimental.MviViewModel

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

/**
 * Public API entrypoint for the Kindling Compose module.
 *
 * This object defines a stable boundary for the framework.
 *
 * It intentionally contains no logic.
 * It only exists to group and expose public-facing primitives.
 *
 * Prefer importing from this API instead of internal packages
 * like `experimental`.
 */
object KindlingComposeApi

// ─────────────────────────────────────────────────────────────
// MVI CORE API
// ─────────────────────────────────────────────────────────────

/**
 * Base ViewModel type used in Kindling MVI architecture.
 *
 * Preferred public alias for [KMviViewModel].
 */
typealias KViewModel<State, I, E> = KMviViewModel<State, I, E>

/**
 * Base MVI contract for ViewModels.
 */
typealias Mvi<State, I, E> = MviViewModel<State, I, E>

/**
 * Marker type for user actions (intents).
 */
typealias Intent = dev.kindling.compose.experimental.Intent

/**
 * Marker type for one-time side effects.
 */
typealias Effect = dev.kindling.compose.experimental.Effect

// ─────────────────────────────────────────────────────────────
// NAVIGATION API
// ─────────────────────────────────────────────────────────────

/**
 * Navigation destination abstraction.
 */
typealias Destination = dev.kindling.compose.Destination

/**
 * Navigation event contract.
 */
typealias NavigationEvent = dev.kindling.compose.NavigationEvent

/**
 * Typed navigation host wrapper.
 */
typealias KNavHost = dev.kindling.compose.KNavHost

/**
 * Navigate using a typed destination.
 */
fun NavController.navigate(
    destination: Destination,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) = this.navigate(
    route = destination.route,
    navOptions = navOptions,
    navigatorExtras = navigatorExtras
)

/**
 * Pops back stack to a typed destination.
 */
fun NavController.popBackTo(
    destination: Destination,
    inclusive: Boolean = false
): Boolean = popBackStack(
    route = destination.route,
    inclusive = inclusive
)

// ─────────────────────────────────────────────────────────────
// COMPOSE UI API
// ─────────────────────────────────────────────────────────────

/**
 * Generic screen wrapper for Kindling applications.
 */
typealias KScreen = dev.kindling.compose.KScreen