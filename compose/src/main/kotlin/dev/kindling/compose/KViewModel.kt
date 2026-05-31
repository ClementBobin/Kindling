@file:Suppress("UnusedReceiverParameter")

package dev.kindling.compose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * Legacy base ViewModel for Kindling applications that require [AndroidViewModel]
 * or [KoinComponent] integration.
 *
 * `KViewModel` is the original single-type-parameter tier of the Kindling ViewModel
 * hierarchy. It provides state management, one-shot events, and async helpers while
 * remaining compatible with Koin's `by inject()` delegation and the [Application] context.
 *
 * ---
 *
 * ## When to use which ViewModel
 *
 * | Scenario | Recommended base |
 * |---|---|
 * | Needs [Application] context or Koin `by inject()` | `KViewModel` ← you are here |
 * | Simple screen, no framework coupling needed | [KSimpleViewModel][dev.kindling.compose.legacy.KSimpleViewModel] |
 * | Feature screen with a typed event contract | [KViewModel][dev.kindling.compose.KViewModel] (two-param) |
 * | Strict unidirectional MVI flow | [KMviViewModel][dev.kindling.compose.experimental.KMviViewModel] |
 *
 * ---
 *
 * ## Tradeoffs
 *
 * - ✅ Full Koin `by inject()` support via [KoinComponent]
 * - ✅ Access to [Application] context via `getApplication()`
 * - ❌ Events are typed as [Any] — no compile-time exhaustiveness checking
 * - ❌ Requires an [Application] reference at construction time
 * - ❌ Harder to unit-test due to [AndroidViewModel] dependency
 *
 * ---
 *
 * ## Basic usage
 *
 * ```kotlin
 * data class ProfileState(
 *     val isLoading: Boolean = true,
 *     val name: String = ""
 * )
 *
 * sealed interface ProfileEvent {
 *     data class ShowToast(val message: String) : ProfileEvent
 * }
 *
 * class ProfileViewModel(
 *     application: Application
 * ) : KViewModel<ProfileState>(
 *     initialState = ProfileState(),
 *     application = application
 * ) {
 *     private val repo: ProfileRepository by inject()
 *
 *     init { loadProfile() }
 *
 *     private fun loadProfile() {
 *         fetchData(
 *             source = { repo.getProfile() },
 *             onResult = { result ->
 *                 result
 *                     .onSuccess { profile ->
 *                         updateState { copy(name = profile.name, isLoading = false) }
 *                     }
 *                     .onFailure {
 *                         sendEvent(ProfileEvent.ShowToast("Failed to load profile"))
 *                     }
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * ---
 *
 * ## Live data collection
 *
 * ```kotlin
 * class FeedViewModel(
 *     application: Application
 * ) : KViewModel<FeedState>(FeedState(), application) {
 *     private val repo: FeedRepository by inject()
 *
 *     init {
 *         collectData(
 *             source = { repo.observeFeed() },
 *             onResult = { result ->
 *                 result.onSuccess { items ->
 *                     updateState { copy(items = items) }
 *                 }
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * @param State The UI state type. Prefer immutable data classes with default values.
 * @param initialState The state emitted immediately to all collectors on subscription.
 * @param application The [Application] instance, forwarded to [AndroidViewModel].
 *
 * @see AndroidViewModel
 * @see KoinComponent
 * @see KScreen
 * @see dev.kindling.compose.legacy.KSimpleViewModel
 * @see dev.kindling.compose.KViewModel
 */
open class KViewModel<State>(
    initialState: State,
    application: Application
) : AndroidViewModel(application), KoinComponent {

    // ─────────────────────────────────────────────────────────────
    // STATE
    // ─────────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(initialState)

    /**
     * Observable UI state stream.
     *
     * Always holds the latest state and replays it immediately to new collectors.
     *
     * Collect this in a [KScreen] or directly in a Composable:
     *
     * ```kotlin
     * val state by viewModel.state.collectAsStateWithLifecycle()
     * ```
     */
    val state: StateFlow<State>
        get() = _state

    /**
     * Applies a reducer function to the current state and emits the result atomically.
     *
     * The receiver of [block] is the current state, allowing `copy(...)` calls
     * directly on data classes:
     *
     * ```kotlin
     * updateState { copy(isLoading = false, items = newItems) }
     * ```
     *
     * @param block Reducer that transforms the current state into a new state.
     */
    protected fun updateState(block: State.() -> State) {
        _state.update { block.invoke(it) }
    }

    // ─────────────────────────────────────────────────────────────
    // EVENTS
    // ─────────────────────────────────────────────────────────────

    private val _events = Channel<Any>(Channel.BUFFERED)

    /**
     * One-shot UI event stream.
     *
     * Events are buffered and delivered in order. Each emission is consumed once,
     * making this suitable for side-effects such as:
     * - Snackbar messages
     * - Toast notifications
     * - Dialog triggers
     * - Navigation (via [Destination])
     *
     * Collect this inside [KScreen] or a `LaunchedEffect`:
     *
     * ```kotlin
     * LaunchedEffect(viewModel) {
     *     viewModel.events.collect { event ->
     *         when (event) {
     *             is ProfileEvent.ShowToast -> { ... }
     *             else -> Unit
     *         }
     *     }
     * }
     * ```
     *
     * > **Note:** Because events are typed as [Any], `when` expressions require
     * > an `else` branch. Prefer [dev.kindling.compose.KViewModel] (two-param) if
     * > exhaustive event handling matters for your screen.
     */
    val events: Flow<Any>
        get() = _events.receiveAsFlow()

    /**
     * Enqueues a one-time event for the UI layer to consume.
     *
     * Safe to call from any thread or coroutine context. Events are buffered
     * and delivered in emission order.
     *
     * ```kotlin
     * sendEvent(ProfileEvent.ShowToast("Profile saved"))
     *
     * // Navigation events are intercepted automatically by Screen:
     * sendEvent(Screen.Home)
     * ```
     *
     * @param obj The event object to deliver. Can be any type, including a
     *   [Destination] for automatic navigation handling in [KScreen].
     */
    protected fun sendEvent(obj: Any) {
        viewModelScope.launch {
            _events.send(obj)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ASYNC HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Collects a [Flow] returned by [source] on `Dispatchers.IO` and delivers
     * each [Result] to [onResult] on `Dispatchers.Main`.
     *
     * Errors are caught and forwarded as [Result.failure] — the ViewModel
     * scope is never cancelled by a failing source.
     *
     * ```kotlin
     * collectData(
     *     source = { repository.observeMessages() },
     *     onResult = { result ->
     *         result
     *             .onSuccess { messages ->
     *                 updateState { copy(messages = messages) }
     *             }
     *             .onFailure { error ->
     *                 sendEvent(MessagesEvent.ShowError(error.message ?: ""))
     *             }
     *     }
     * )
     * ```
     *
     * @param T The type of values emitted by the flow.
     * @param source Suspending factory returning the [Flow] to collect, executed on IO.
     * @param onResult Callback receiving a [Result] for each emission or terminal error,
     *   always invoked on the Main dispatcher.
     */
    fun <T> collectData(
        source: suspend () -> Flow<T>,
        onResult: Result<T>.() -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                source().collect { newValue ->
                    launch(Dispatchers.Main) {
                        onResult(Result.success(newValue))
                    }
                }
            } catch (ex: Throwable) {
                launch(Dispatchers.Main) {
                    onResult(Result.failure(ex))
                }
            }
        }
    }

    /**
     * Executes a suspending [source] on `Dispatchers.IO` and delivers the
     * [Result] to [onResult] on `Dispatchers.Main`.
     *
     * Errors are caught and forwarded as [Result.failure] — the ViewModel
     * scope is never cancelled by a failing source.
     *
     * ```kotlin
     * fetchData(
     *     source = { repository.getUser(userId) },
     *     onResult = { result ->
     *         result
     *             .onSuccess { user ->
     *                 updateState { copy(user = user, isLoading = false) }
     *             }
     *             .onFailure { error ->
     *                 updateState { copy(isLoading = false) }
     *                 sendEvent(ProfileEvent.ShowToast("Failed to load user"))
     *             }
     *     }
     * )
     * ```
     *
     * @param T The type of data returned by [source].
     * @param source Suspending function executed on the IO dispatcher.
     * @param onResult Callback receiving a [Result] wrapping the value or error,
     *   always invoked on the Main dispatcher.
     */
    fun <T> fetchData(
        source: suspend () -> T,
        onResult: Result<T>.() -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = source()
                launch(Dispatchers.Main) {
                    onResult(Result.success(success))
                }
            } catch (ex: Throwable) {
                launch(Dispatchers.Main) {
                    onResult(Result.failure(ex))
                }
            }
        }
    }
}