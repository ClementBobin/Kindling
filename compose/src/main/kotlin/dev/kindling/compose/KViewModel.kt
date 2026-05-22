package dev.kindling.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
//  KViewModel
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Base ViewModel for Kindling apps.
 *
 * Provides a structured, opinionated approach to:
 * - **State** — a [StateFlow] that drives Compose UI via `collectAsStateWithLifecycle`.
 * - **Events** — a buffered [Channel] for one-time UI side-effects (navigation,
 *   snackbars, dialogs).
 * - **Data loading** — [fetchData] and [collectData] helpers that run work on
 *   `IO`, deliver results on `Main`, and wrap outcomes in [Result].
 *
 * ### Defining a ViewModel
 * ```kotlin
 * data class HomeState(
 *     val isLoading: Boolean = false,
 *     val items: List<String> = emptyList()
 * )
 *
 * sealed interface HomeEvent {
 *     data class ShowError(val message: String) : HomeEvent
 *     object NavigateToDetail : HomeEvent
 * }
 *
 * class HomeViewModel(
 *     private val repository: ItemRepository
 * ) : KViewModel<HomeState, HomeEvent>(HomeState()) {
 *
 *     init { loadItems() }
 *
 *     private fun loadItems() {
 *         fetchData(
 *             source   = { repository.getItems() },
 *             onResult = { result ->
 *                 result
 *                     .onSuccess { items -> updateState { copy(items = items, isLoading = false) } }
 *                     .onFailure { e   -> sendEvent(HomeEvent.ShowError(e.message ?: "Unknown error")) }
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * ### Consuming in a composable
 * ```kotlin
 * @Composable
 * fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
 *     val state by viewModel.state.collectAsStateWithLifecycle()
 *
 *     LaunchedEffect(Unit) {
 *         viewModel.events.collect { event ->
 *             when (event) {
 *                 is HomeEvent.ShowError       -> snackbarHostState.showSnackbar(event.message)
 *                 is HomeEvent.NavigateToDetail -> navController.navigate(Screen.Detail)
 *             }
 *         }
 *     }
 *     // … render state …
 * }
 * ```
 *
 * @param State        The UI state type. Use a data class so `copy()` is available.
 * @param Event        The event type. Use a sealed interface for exhaustive `when`.
 * @param initialState The state emitted immediately on subscription.
 */
abstract class KViewModel<State, Event>(
    initialState: State
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(initialState)

    /**
     * The current UI state. Collect this in your composable with
     * `collectAsStateWithLifecycle()`.
     */
    val state: StateFlow<State> = _state.asStateFlow()

    /**
     * Atomically applies [block] to the current state and emits the result.
     *
     * ```kotlin
     * updateState { copy(isLoading = true) }
     * ```
     */
    protected fun updateState(block: State.() -> State) {
        _state.update { block(it) }
    }

    // ── Events ────────────────────────────────────────────────────────────────

    private val _events = Channel<Event>(Channel.BUFFERED)

    /**
     * One-time UI side-effects. Collect in a `LaunchedEffect` and handle each
     * event exactly once (navigation, toasts, dialogs, etc.).
     */
    val events: Flow<Event> = _events.receiveAsFlow()

    /**
     * Enqueues a one-time [event] for the UI to handle.
     *
     * ```kotlin
     * sendEvent(HomeEvent.ShowError("Something went wrong"))
     * ```
     */
    protected fun sendEvent(event: Event) {
        viewModelScope.launch { _events.send(event) }
    }

    // ── Data helpers ──────────────────────────────────────────────────────────

    /**
     * Collects a [Flow] produced by [source] on `Dispatchers.IO` and delivers
     * each emission to [onResult] on `Dispatchers.Main`.
     *
     * Catches any [Throwable] and delivers it as [Result.failure].
     *
     * ```kotlin
     * collectData(
     *     source   = { repository.observeItems() },
     *     onResult = { result ->
     *         result
     *             .onSuccess { items -> updateState { copy(items = items) } }
     *             .onFailure { e    -> sendEvent(HomeEvent.ShowError(e.message ?: "")) }
     *     }
     * )
     * ```
     */
    protected fun <T> collectData(
        source: suspend () -> Flow<T>,
        onResult: suspend (Result<T>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                source().collect { value ->
                    launch(Dispatchers.Main) { onResult(Result.success(value)) }
                }
            } catch (ex: Throwable) {
                launch(Dispatchers.Main) { onResult(Result.failure(ex)) }
            }
        }
    }

    /**
     * Executes a suspending [source] function on `Dispatchers.IO` and delivers
     * the result to [onResult] on `Dispatchers.Main`.
     *
     * Catches any [Throwable] and delivers it as [Result.failure].
     *
     * ```kotlin
     * fetchData(
     *     source   = { repository.getUser(userId) },
     *     onResult = { result ->
     *         result
     *             .onSuccess { user -> updateState { copy(user = user, isLoading = false) } }
     *             .onFailure { e   -> sendEvent(HomeEvent.ShowError(e.message ?: "")) }
     *     }
     * )
     * ```
     */
    protected fun <T> fetchData(
        source: suspend () -> T,
        onResult: suspend (Result<T>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val value = source()
                launch(Dispatchers.Main) { onResult(Result.success(value)) }
            } catch (ex: Throwable) {
                launch(Dispatchers.Main) { onResult(Result.failure(ex)) }
            }
        }
    }
}