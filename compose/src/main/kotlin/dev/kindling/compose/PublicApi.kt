package dev.kindling.compose

import dev.kindling.compose.experimental.Effect
import dev.kindling.compose.experimental.Intent
import dev.kindling.compose.experimental.KMviViewModel
import dev.kindling.compose.experimental.MviViewModel

/**
 * Public API entrypoint for the Kindling Compose module.
 *
 * This object defines a stable boundary for the framework.
 *
 * It intentionally contains no logic.
 * It only exists to group and expose public-facing primitives.
 *
 * Prefer importing from this file instead of internal packages
 * like `experimental`.
 *
 * The primary public surface — [Destination], [NavigationEvent],
 * [KNavHost], [KViewModel], [KScreen] and their NavController
 * extensions — is declared directly in this package and does not
 * need re-exporting here.
 */
object KindlingComposeApi

// ─────────────────────────────────────────────
// MVI EXPERIMENTAL API
// ─────────────────────────────────────────────

/**
 * Base MVI ViewModel from the experimental package.
 *
 * Use this when you want the strict Intent/Effect/State split.
 * For simpler State/Event ViewModels use [KViewModel] directly.
 */
typealias KMvi<State, I, E> = KMviViewModel<State, I, E>

/**
 * Base MVI contract interface from the experimental package.
 */
typealias Mvi<State, I, E> = MviViewModel<State, I, E>

/**
 * Marker type for user actions (intents) in the experimental MVI pattern.
 */
typealias KIntent = Intent

/**
 * Marker type for one-time side effects in the experimental MVI pattern.
 */
typealias KEffect = Effect