package dev.kindling.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base ViewModel for Kindling applications.
 *
 * `KViewModel` provides a structured foundation for building
 * Compose-driven UIs with a clear separation of concerns:
 *
 * - **State management** via [StateFlow]
 * - **One-shot UI events** via [SharedFlow]
 * - **Async execution helpers** for repository and Flow integration
 * - **Lifecycle-safe coroutine scope** via [viewModelScope]
 *
 * It is designed to work seamlessly with [KScreen], which handles:
 * - state collection
 * - event dispatching
 * - navigation events
 * - back handling
 *
 * ---
 *
 * ## 🧠 Core Concepts
 *
 * ### 1. State (UI as a function of data)
 *
 * State is immutable from the UI side and updated only via [updateState].
 *
 * ```kotlin
 * data class HomeState(
 *     val isLoading: Boolean = false,
 *     val items: List<String> = emptyList()
 * )
 * ```
 *
 * ---
 *
 * ### 2. Events (one-time effects)
 *
 * Events represent UI side-effects such as:
 * - navigation
 * - snackbars
 * - dialogs
 *
 * ```kotlin
 * sealed interface HomeEvent {
 *     data class ShowMessage(val text: String) : HomeEvent
 *     data object NavigateNext : HomeEvent
 * }
 * ```
 *
 * ---
 *
 * ### 3. Data loading
 *
 * Built-in helpers simplify async work:
 *
 * - [fetchData] → single suspend call
 * - [collectData] → Flow collection
 *
 * ---
 *
 * ## 📦 Example Usage
 *
 * ```kotlin
 * class HomeViewModel(
 *     private val repository: ItemRepository
 * ) : KViewModel<HomeState, HomeEvent>(HomeState()) {
 *
 *     init {
 *         loadItems()
 *     }
 *
 *     private fun loadItems() {
 *         fetchData(
 *             source = { repository.getItems() },
 *             onResult = { result ->
 *                 result
 *                     .onSuccess { items ->
 *                         updateState { copy(items = items, isLoading = false) }
 *                     }
 *                     .onFailure { error ->
 *                         sendEvent(HomeEvent.ShowMessage(error.message ?: "Unknown error"))
 *                     }
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * ---
 *
 * @param State The UI state type. Prefer immutable data classes.
 * @param Event The UI event type. Prefer sealed interfaces for exhaustiveness.
 * @param initialState The initial state emitted to collectors immediately.
 */
abstract class KViewModel<State, Event>(
    initialState: State
) : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // STATE
    // ─────────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(initialState)

    /**
     * Observable UI state stream.
     *
     * Collect this in Compose using:
     *
     * ```kotlin
     * val state by viewModel.state.collectAsStateWithLifecycle()
     * ```
     */
    val state: StateFlow<State> = _state.asStateFlow()

    /**
     * Updates the current state atomically using a reducer function.
     *
     * ```kotlin
     * updateState { copy(isLoading = true) }
     * ```
     */
    protected fun updateState(reducer: State.() -> State) {
        _state.update(reducer)
    }

    /**
     * Replaces the entire state value.
     *
     * Use sparingly; prefer [updateState] for partial updates.
     */
    protected fun setState(newState: State) {
        _state.value = newState
    }

    // ─────────────────────────────────────────────────────────────
    // EVENTS
    // ─────────────────────────────────────────────────────────────

    private val _events = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 64
    )

    /**
     * One-time UI events stream.
     *
     * Typical usage:
     *
     * - navigation actions
     * - snackbar messages
     * - dialogs
     *
     * Collect inside [KScreen] or a `LaunchedEffect`.
     */
    val events: Flow<Event> = _events.asSharedFlow()

    /**
     * Emits a one-time event to the UI layer.
     *
     * This is safe to call from any coroutine context.
     *
     * ```kotlin
     * sendEvent(HomeEvent.ShowMessage("Saved successfully"))
     * ```
     */
    protected fun sendEvent(event: Event) {
        _events.tryEmit(event)
    }

    /**
     * Suspended variant of [sendEvent].
     *
     * Use this when ordering matters or emission must be guaranteed.
     */
    protected suspend fun sendEventSuspend(event: Event) {
        _events.emit(event)
    }

    // ─────────────────────────────────────────────────────────────
    // DATA HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Executes a suspend operation on `Dispatchers.IO`
     * and returns the result wrapped in [Result].
     *
     * The callback is always invoked on the ViewModel scope.
     *
     * ```kotlin
     * fetchData(
     *     source = { repository.getUser() },
     *     onResult = { result ->
     *         result
     *             .onSuccess { user -> updateState { copy(user = user) } }
     *             .onFailure { error -> sendEvent(HomeEvent.ShowMessage(error.message ?: "")) }
     *     }
     * )
     * ```
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
     * Collects a [Flow] inside [viewModelScope].
     *
     * Errors are forwarded to [onError] instead of crashing the scope.
     *
     * ```kotlin
     * collectData(
     *     source = { repository.observeItems() },
     *     onEach = { items -> updateState { copy(items = items) } },
     *     onError = { error -> sendEvent(HomeEvent.ShowMessage(error.message ?: "")) }
     * )
     * ```
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

    /**
     * Convenience helper combining data fetching and state reduction.
     *
     * Useful for simple “load → update state” flows.
     *
     * ```kotlin
     * fetchAndReduce(
     *     source = { repository.getItems() }
     * ) { items ->
     *     copy(items = items, isLoading = false)
     * }
     * ```
     */
    protected fun <T> fetchAndReduce(
        source: suspend () -> T,
        reducer: State.(T) -> State
    ) {
        fetchData(source) { result ->
            result.onSuccess { value ->
                updateState { reducer(value) }
            }
        }
    }
}