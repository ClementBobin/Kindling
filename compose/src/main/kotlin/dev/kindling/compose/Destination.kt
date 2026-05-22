package dev.kindling.compose

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// ─────────────────────────────────────────────────────────────────────────────
//  Destination
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Base contract for all navigation destinations.
 */
/**
 * Base contract for all navigation destinations in a Kindling application.
 *
 * A destination represents a unique route inside a Compose navigation graph.
 *
 * Implement this interface (typically through a sealed class or sealed interface)
 * to centralize route definitions and optional navigation arguments.
 *
 * ## Example
 *
 * ```kotlin
 * sealed class Screen(
 *     override val route: String
 * ) : Destination {
 *
 *     data object Splash : Screen("splash")
 *     data object Home : Screen("home")
 *
 *     data object Profile : Screen("profile/{userId}") {
 *         override val arguments = listOf(
 *             navArgument("userId") {
 *                 type = NavType.StringType
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * @property route The unique navigation route.
 * @property arguments Optional typed navigation arguments.
 */
interface Destination {
    val route: String
    val arguments: List<NamedNavArgument>
        get() = emptyList()
}

// ─────────────────────────────────────────────────────────────────────────────
//  Navigation events
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Explicit navigation event contract.
 *
 * This avoids implicit magic behaviour based on runtime type checks.
 */
/**
 * Explicit contract for navigation-related events.
 *
 * This interface allows ViewModels to emit navigation requests without
 * introducing implicit runtime behaviour.
 *
 * Instead of checking arbitrary event types at runtime, Kindling only performs
 * navigation for events explicitly implementing [NavigationEvent].
 *
 * ## Example
 *
 * ```kotlin
 * sealed interface HomeEvent {
 *
 *     data class Navigate(
 *         override val destination: Destination
 *     ) : HomeEvent, NavigationEvent
 * }
 * ```
 *
 * @property destination The destination to navigate to.
 * @property navOptions Optional navigation configuration.
 * @property navigatorExtras Optional navigator-specific extras.
 */
interface NavigationEvent {
    val destination: Destination
    val navOptions: NavOptions?
        get() = null

    val navigatorExtras: Navigator.Extras?
        get() = null
}

// ─────────────────────────────────────────────────────────────────────────────
 *
 * ## Example
 *
 * ```kotlin
 * navController.navigate(Screen.Home)
 * ```
 *
 * @param destination The destination to navigate to.
 * @param navOptions Optional navigation options.
 * @param navigatorExtras Optional navigator-specific extras.
 */
fun NavController.navigate(
    destination: Destination,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) = navigate(
    route = destination.route,
    navOptions = navOptions,
    navigatorExtras = navigatorExtras
)

/**
 * Pops the back stack to a [Destination].
 */
/**
 * Pops the navigation back stack to the specified [Destination].
 *
 * ## Example
 *
 * ```kotlin
 * navController.popBackTo(Screen.Login)
 *
 * navController.popBackTo(
 *     destination = Screen.Splash,
 *     inclusive = true
 * )
 * ```
 *
 * @param destination The destination to pop back to.
 * @param inclusive Whether the destination itself should also be removed.
 *
 * @return `true` if the back stack was popped successfully.
 */
fun NavController.popBackTo(
    destination: Destination,
    inclusive: Boolean = false
): Boolean = popBackStack(
    route = destination.route,
    inclusive = inclusive
)

// ─────────────────────────────────────────────────────────────────────────────
//  KNavHost
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Typed wrapper around [NavHost].
 */
/**
 * Typed wrapper around Compose [NavHost].
 *
 * This variant accepts a typed [Destination] as the start destination,
 * avoiding raw route strings at call sites.
 *
 * ## Example
 *
 * ```kotlin
 * @Composable
 * fun AppNavigation(navController: NavHostController) {
 *     KNavHost(
 *         navController = navController,
 *         startDestination = Screen.Splash
 *     ) {
 *
 *         composable(Screen.Splash) {
 *             SplashScreen(navController)
 *         }
 *
 *         composable(Screen.Home) {
 *             HomeScreen(navController)
 *         }
 *     }
 * }
 * ```
 *
 * @param navController Controller managing the navigation state.
 * @param startDestination Initial destination shown when the graph starts.
 * @param modifier Optional host modifier.
 * @param builder Navigation graph builder.
 */
@Composable
fun KNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
        builder = builder
    )
}