package dev.kindling.compose.legacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A lightweight, single-type-parameter ViewModel for simple or migrating screens.
 *
 * `KSimpleViewModel` is the entry-level tier of the Kindling ViewModel hierarchy.
 * It provides the same core primitives as [KViewModel][dev.kindling.compose.KViewModel]
 * — state management, one-shot events, and async helpers — without requiring a
 * dedicated sealed event type.
 *
 * This makes it well suited for:
 * - Screens with only a handful of trivial events (e.g. a single "show snackbar")
 * - Gradual migration from an untyped event system
 * - Prototyping before formalising the event contract
 *
 * ---
 *
 * ## When to use which ViewModel
 *
 * | Scenario | Recommended base |
 * |---|---|
 * | Simple screen, few or trivial events | `KSimpleViewModel` ← you are here |
 * | Feature screen with a clear event contract | [KViewModel][dev.kindling.compose.KViewModel] |
 * | Strict unidirectional MVI flow | [KMviViewModel][dev.kindling.compose.experimental.KMviViewModel] |
 *
 * ---
 *
 * ## Tradeoffs vs [KViewModel][dev.kindling.compose.KViewModel]
 *
 * - ✅ Less boilerplate — no sealed event interface required
 * - ✅ Easy to migrate existing untyped ViewModels into
 * - ❌ Events are typed as [Any] — no compile-time exhaustiveness checking
 * - ❌ `when` blocks on events must include an `else` branch
 *
 * ---
 *
 * ## Koin / dependency injection
 *
 * `KSimpleViewModel` has no Koin coupling. If you need `by inject()` in a subclass,
 * implement [org.koin.core.component.KoinComponent] directly on that class:
 *
 * ```kotlin
 * class ProfileViewModel(
 *     private val repo: ProfileRepository
 * ) : KSimpleViewModel<ProfileState>(ProfileState()),
 *     KoinComponent  // ← opt-in per subclass, not forced on all consumers
 * ```
 *
 * Prefer constructor injection wired through a Koin module for cleaner,
 * more testable ViewModels.
 *
 * ---
 *
 * ## Basic usage
 *
 * ```kotlin
 * data class CounterState(val count: Int = 0)
 *
 * sealed interface CounterEvent {
 *     data class ShowToast(val message: String) : CounterEvent
 * }
 *
 * class CounterViewModel : KSimpleViewModel<CounterState>(CounterState()) {
 *
 *     fun increment() {
 *         updateState { copy(count = count + 1) }
 *     }
 *
 *     fun reset() {
 *         updateState { copy(count = 0) }
 *         sendEvent(CounterEvent.ShowToast("Counter reset"))
 *     }
 * }
 * ```
 *
 * ---
 *
 * ## Async data loading
 *
 * ```kotlin
 * class SummaryViewModel(
 *     private val repo: SummaryRepository
 * ) : KSimpleViewModel<SummaryState>(SummaryState()) {
 *
 *     init { loadSummary() }
 *
 *     private fun loadSummary() {
 *         fetchData(
 *             source = { repo.getSummary() },
 *             onResult = { result ->
 *                 result
 *                     .onSuccess { data ->
 *                         updateState { copy(summary = data, isLoading = false) }
 *                     }
 *                     .onFailure {
 *                         sendEvent(SummaryEvent.ShowError("Failed to load"))
 *                     }
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * @param State The UI state type. Prefer immutable data classes with default values.
 * @param initialState The state emitted immediately to all collectors on subscription.
 *
 * @see dev.kindling.compose.KViewModel
 * @see dev.kindling.compose.experimental.KMviViewModel
 * @see SimpleScreen
 */
abstract class KSimpleViewModel<State>(
    initialState: State
) : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // STATE
    // ─────────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(initialState)

    /**
     * Observable UI state stream.
     *
     * Always holds the latest state and replays it immediately to new collectors.
     *
     * Collect this in a [SimpleScreen] or directly in a Composable:
     *
     * ```kotlin
     * val state by viewModel.state.collectAsStateWithLifecycle()
     * ```
     */
    val state: StateFlow<State> = _state.asStateFlow()

    /**
     * Applies a reducer function to the current state and emits the result atomically.
     *
     * The receiver of [block] is the current state, allowing `copy(...)` calls
     * directly on data classes:
     *
     * ```kotlin
     * updateState { copy(isLoading = true) }
     * ```
     *
     * @param block Reducer that transforms the current state into a new state.
     */
    protected fun updateState(block: State.() -> State) {
        _state.update(block)
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
     * - Navigation (via [dev.kindling.compose.Destination])
     *
     * Collect this inside [SimpleScreen] or a `LaunchedEffect`:
     *
     * ```kotlin
     * LaunchedEffect(viewModel) {
     *     viewModel.events.collect { event ->
     *         when (event) {
     *             is MyEvent.ShowToast -> { ... }
     *             else -> Unit
     *         }
     *     }
     * }
     * ```
     *
     * > **Note:** Because events are typed as [Any], `when` expressions require
     * > an `else` branch. Prefer [dev.kindling.compose.KViewModel] if exhaustive
     * > event handling matters for your screen.
     */
    val events: Flow<Any> = _events.receiveAsFlow()

    /**
     * Enqueues a one-time event for the UI layer to consume.
     *
     * Safe to call from any thread or coroutine context. Events are buffered
     * and delivered in emission order.
     *
     * ```kotlin
     * sendEvent(MyEvent.ShowToast("Saved"))
     *
     * // Navigation events are handled automatically by SimpleScreen:
     * sendEvent(Screen.Home)
     * ```
     *
     * @param event The event object to deliver. Can be any type, including a
     *   [dev.kindling.compose.Destination] for automatic navigation handling.
     */
    protected fun sendEvent(event: Any) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ASYNC HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Executes a suspending [source] on `Dispatchers.IO` and delivers the
     * [Result] to [onResult] on the ViewModel scope.
     *
     * Errors are caught and forwarded as [Result.failure] — the ViewModel
     * scope is never cancelled by a failing source.
     *
     * ```kotlin
     * fetchData(
     *     source = { repository.getProfile(userId) },
     *     onResult = { result ->
     *         result
     *             .onSuccess { profile ->
     *                 updateState { copy(profile = profile, isLoading = false) }
     *             }
     *             .onFailure { error ->
     *                 updateState { copy(isLoading = false) }
     *                 sendEvent(ProfileEvent.ShowError(error.message ?: "Unknown error"))
     *             }
     *     }
     * )
     * ```
     *
     * @param T The type of data returned by [source].
     * @param source Suspending function executed on the IO dispatcher.
     * @param onResult Callback receiving a [Result] wrapping the value or error.
     */
    protected fun <T> fetchData(
        source: suspend () -> T,
        onResult: (Result<T>) -> Unit
    ) {
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { source() }
            }
            onResult(result)
        }
    }

    /**
     * Collects a [Flow] returned by [source] inside the ViewModel scope.
     *
     * Each emitted value is forwarded to [onEach]. Errors are forwarded to
     * [onError] and do not cancel the scope.
     *
     * ```kotlin
     * collectData(
     *     source = { repository.observeNotifications() },
     *     onEach = { notifications ->
     *         updateState { copy(notifications = notifications) }
     *     },
     *     onError = { error ->
     *         sendEvent(NotificationEvent.ShowError(error.message ?: ""))
     *     }
     * )
     * ```
     *
     * @param T The type of values emitted by the flow.
     * @param source Factory returning the [Flow] to collect.
     * @param onEach Called for each emitted value.
     * @param onError Called when the flow terminates with an error. Defaults to a no-op.
     */
    protected fun <T> collectData(
        source: () -> Flow<T>,
        onEach: (T) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            source()
                .catch { onError(it) }
                .collect { onEach(it) }
        }
    }
}