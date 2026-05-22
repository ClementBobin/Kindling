package dev.kindling.compose

/**
 * Public API entrypoint marker for the Kindling navigation module.
 *
 * Exposed symbols:
 * - [Destination]   — base class for typed route definitions
 * - [KNavHost]      — NavHost wrapper that accepts [Destination] as start destination
 * - [KViewModel]    — base ViewModel with state / events / data-loading helpers
 * - `NavGraphBuilder.composable(Destination)` — type-safe composable registration
 * - `NavController.navigate(Destination)`     — type-safe navigation
 * - `NavController.popUpTo(Destination)`      — type-safe pop-back
 */
object KindlingNavigationApi