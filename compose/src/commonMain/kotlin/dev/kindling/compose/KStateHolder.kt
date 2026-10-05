package dev.kindling.compose

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Minimal read-only contract that [KScreen] needs from a ViewModel.
 *
 * Implemented by every Kindling state-holding ViewModel:
 * - [dev.kindling.compose.legacy.KSimpleViewModel] on all platforms,
 * - `KViewModel` on Android (when an `Application` or Koin is needed).
 *
 * @param State UI state type.
 */
interface KStateHolder<State> {
    /** Observable UI state; always holds the latest value. */
    val state: StateFlow<State>

    /** One-shot events (navigation, toasts…), each delivered once and in order. */
    val events: Flow<Any>
}
