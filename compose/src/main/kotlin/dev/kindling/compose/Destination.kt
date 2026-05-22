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
 * Base class for all navigation destinations in a Kindling app.
 *
 * Subclass this (typically as a sealed class) to define every route in your
 * navigation graph.  Each destination carries its own typed route string and
 * an optional list of [NamedNavArgument]s so arguments are declared once and
 * reused everywhere.
 *
 * ### Defining destinations
 * ```kotlin
 * sealed class Screen(route: String, arguments: List<NamedNavArgument> = emptyList())
 *     : Destination(route, arguments) {
 *
 *     object Splash : Screen("splash")
 *     object Home   : Screen("home")
 *     object Login  : Screen("login")
 *
 *     // With a typed argument
 *     object Profile : Screen(
 *         route     = "profile/{userId}",
 *         arguments = listOf(navArgument("userId") { type = NavType.StringType })
 *     )
 * }
 * ```
 *
 * @property route     The unique route string for this destination.
 * @property arguments [NamedNavArgument]s declared for this destination.
 */
abstract class Destination(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────────
//  NavGraphBuilder extensions
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Adds a [Destination] to the navigation graph as a composable screen.
 *
 * ```kotlin
 * NavHost(navController, startDestination = Screen.Splash) {
 *     composable(Screen.Splash) { SplashScreen(navController) }
 *     composable(Screen.Home)  { HomeScreen(navController)  }
 *     composable(Screen.Profile) { backStack ->
 *         val userId = backStack.arguments?.getString("userId") ?: ""
 *         ProfileScreen(userId, navController)
 *     }
 * }
 * ```
 */
fun NavGraphBuilder.composable(
    destination: Destination,
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) = composable(
    route     = destination.route,
    arguments = destination.arguments,
    deepLinks = deepLinks,
    content   = content
)

// ─────────────────────────────────────────────────────────────────────────────
//  NavController extensions
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Navigates to a [Destination] without needing to reference its raw route string.
 *
 * ```kotlin
 * navController.navigate(Screen.Home)
 *
 * // With pop-up behaviour
 * navController.navigate(
 *     destination = Screen.Home,
 *     navOptions  = navOptions {
 *         popUpTo(Screen.Splash.route) { inclusive = true }
 *     }
 * )
 * ```
 */
fun NavController.navigate(
    destination: Destination,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) = navigate(
    route            = destination.route,
    navOptions       = navOptions,
    navigatorExtras  = navigatorExtras
)

/**
 * Pops back to a [Destination] inclusively or exclusively.
 *
 * ```kotlin
 * navController.popUpTo(Screen.Login, inclusive = true)
 * ```
 */
fun NavController.popUpTo(destination: Destination, inclusive: Boolean = false) {
    popBackStack(route = destination.route, inclusive = inclusive)
}

// ─────────────────────────────────────────────────────────────────────────────
//  KNavHost
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A thin wrapper around [NavHost] that accepts a [Destination] as the start
 * destination, keeping your call-sites free of raw route strings.
 *
 * ```kotlin
 * sealed class Screen(route: String) : Destination(route) {
 *     object Splash : Screen("splash")
 *     object Home   : Screen("home")
 *     object Login  : Screen("login")
 * }
 *
 * @Composable
 * fun AppNavHost(navController: NavHostController) {
 *     KNavHost(navController, startDestination = Screen.Splash) {
 *         composable(Screen.Splash) { SplashScreen(navController) }
 *         composable(Screen.Home)  { HomeScreen(navController)  }
 *         composable(Screen.Login) { LoginScreen(navController) }
 *     }
 * }
 * ```
 *
 * @param navController    The controller that manages navigation state.
 * @param startDestination The initial [Destination] shown when the host first renders.
 * @param modifier         Optional [Modifier] applied to the [NavHost].
 * @param builder          NavGraphBuilder lambda where all composable destinations are declared.
 */
@Composable
fun KNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination.route,
        modifier         = modifier,
        builder          = builder
    )
}