@file:Suppress("UnusedReceiverParameter", "TooGenericExceptionCaught")

package dev.kindling.compose.legacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kindling.compose.KScreen
import dev.kindling.compose.KStateHolder
import dev.kindling.compose.internal.KViewModelCore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Multiplatform base ViewModel with state, one-shot events and async helpers.
 *
 * Same programming model as the Android `KViewModel`, minus the platform coupling:
 * no `Application`, no Koin. It runs on Android, desktop, iOS and web.
 *
 * ```kotlin
 * class ProfileViewModel(private val repo: UserRepository) :
 *     KSimpleViewModel<ProfileState>(ProfileState()) {
 *
 *     fun load() = fetchData(
 *         source = { repo.getUser() },
 *         onResult = { result ->
 *             result
 *                 .onSuccess { user -> updateState { copy(user = user, isLoading = false) } }
 *                 .onFailure { sendEvent(ProfileEvent.ShowToast("Failed to load profile")) }
 *         }
 *     )
 * }
 * ```
 *
 * Events that implement `KDestination` or `NavigationEvent` are turned into navigation
 * automatically by [KScreen].
 *
 * `fetchData` / `collectData` run the source on an IO dispatcher (`Dispatchers.IO`,
 * or `Dispatchers.Default` on the web) and deliver results on `Dispatchers.Main`.
 *
 * @param State The UI state type. Prefer immutable data classes.
 * @param initialState State emitted immediately to every collector.
 */
open class KSimpleViewModel<State>(
    initialState: State
) : ViewModel(), KStateHolder<State> {

    private val core = KViewModelCore(initialState, viewModelScope)

    override val state: StateFlow<State>
        get() = core.state

    override val events: Flow<Any>
        get() = core.events

    /** Applies a reducer to the current state atomically: `updateState { copy(isLoading = false) }`. */
    protected fun updateState(block: State.() -> State) = core.updateState(block)

    /** Enqueues a one-shot event for the UI layer. Safe to call from any coroutine context. */
    protected fun sendEvent(obj: Any) = core.sendEvent(obj)

    /**
     * Collects the [Flow] returned by [source] on the IO dispatcher and delivers each
     * [Result] to [onResult] on `Dispatchers.Main`. Errors become [Result.failure].
     */
    fun <T> collectData(
        source: suspend () -> Flow<T>,
        onResult: Result<T>.() -> Unit
    ) = core.collectData(source, onResult)

    /**
     * Runs [source] on the IO dispatcher and delivers the [Result] to [onResult] on
     * `Dispatchers.Main`. Errors become [Result.failure].
     */
    fun <T> fetchData(
        source: suspend () -> T,
        onResult: Result<T>.() -> Unit
    ) = core.fetchData(source, onResult)
}
