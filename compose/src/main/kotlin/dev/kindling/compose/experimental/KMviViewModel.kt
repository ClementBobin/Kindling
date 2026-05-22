package dev.kindling.compose.experimental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────
// MVI CONTRACT
// ─────────────────────────────────────────────────────────────

/**
 * Marker interface for UI actions coming from the view layer.
 *
 * These represent user intentions such as clicks, input changes,
 * or lifecycle triggers.
 *
 * ## Example
 *
 * ```kotlin
 * sealed interface HomeIntent : Intent {
 *     data object LoadItems : HomeIntent
 *     data class Search(val query: String) : HomeIntent
 * }
 * ```
 */
interface Intent

/**
 * One-time side effects emitted by the ViewModel.
 *
 * Effects are consumed once by the UI layer and are not part of state.
 *
 * Typical uses:
 * - navigation
 * - snackbar messages
 * - dialogs
 * - external actions (analytics, deep links)
 *
 * ## Example
 *
 * ```kotlin
 * sealed interface HomeEffect : Effect {
 *     data class ShowSnackbar(val message: String) : HomeEffect
 *     data class Navigate(val route: String) : HomeEffect
 * }
 * ```
 */
interface Effect

/**
 * Core contract for MVI ViewModels in Kindling.
 *
 * Defines a strict unidirectional flow:
 *
 * ```
 * Intent → ViewModel → State + Effect → UI
 * ```
 *
 * ## Example
 *
 * ```kotlin
 * class HomeViewModel : KMviViewModel<HomeState, HomeIntent, HomeEffect>(
 *     HomeState()
 * ) {
 *     override suspend fun handleIntent(intent: HomeIntent) {
 *         when (intent) {
 *             HomeIntent.LoadItems -> loadItems()
 *         }
 *     }
 * }
 * ```
 */
interface MviViewModel<State, I, E> {

    val state: StateFlow<State>
    val effects: Flow<E>

    fun dispatch(intent: I)
}

// ─────────────────────────────────────────────────────────────
// BASE IMPLEMENTATION
// ─────────────────────────────────────────────────────────────

/**
 * Base implementation of a strict MVI ViewModel.
 *
 * Provides:
 * - State management via StateFlow
 * - One-time effects via SharedFlow
 * - Coroutine-safe intent handling
 * - IO-safe async helpers
 *
 * Designed to work seamlessly with `KScreen`.
 *
 * ## Architecture Flow
 *
 * ```
 * UI → Intent → ViewModel → State / Effect → UI
 * ```
 *
 * ## Example
 *
 * ```kotlin
 * class HomeViewModel(
 *     private val repo: ItemRepository
 * ) : KMviViewModel<HomeState, HomeIntent, HomeEffect>(
 *     HomeState()
 * ) {
 *
 *     override suspend fun handleIntent(intent: HomeIntent) {
 *         when (intent) {
 *             HomeIntent.LoadItems -> loadItems()
 *         }
 *     }
 *
 *     private fun loadItems() {
 *         async(
 *             block = { repo.getItems() }
 *         ) { result ->
 *             result
 *                 .onSuccess { items ->
 *                     copy(items = items, isLoading = false)
 *                 }
 *                 .onFailure {
 *                     emitEffect(HomeEffect.ShowSnackbar("Error loading items"))
 *                     this
 *                 }
 *         }
 *     }
 * }
 * ```
 */
abstract class KMviViewModel<State, I, E>(
    initialState: State
) : ViewModel(), MviViewModel<State, I, E> {

    // ─────────────────────────────────────────────
    // STATE
    // ─────────────────────────────────────────────

    private val _state = MutableStateFlow(initialState)

    override val state: StateFlow<State> = _state.asStateFlow()

    /**
     * Updates the current state using a reducer function.
     *
     * ## Example
     *
     * ```kotlin
     * setState {
     *     copy(isLoading = true)
     * }
     * ```
     */
    protected fun setState(reducer: State.() -> State) {
        _state.update(reducer)
    }

    // ─────────────────────────────────────────────
    // EFFECTS
    // ─────────────────────────────────────────────

    private val _effects = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val effects: Flow<E> = _effects.asSharedFlow()

    /**
     * Emits a one-time effect to the UI layer.
     *
     * ## Example
     *
     * ```kotlin
     * emitEffect(HomeEffect.ShowSnackbar("Saved successfully"))
     * ```
     */
    protected fun emitEffect(effect: E) {
        val emitted = _effects.tryEmit(effect)
        if (!emitted) {
            viewModelScope.launch {
                _effects.emit(effect)
            }
        }
    }

    /**
     * Suspended variant of [emitEffect].
     *
     * Ensures guaranteed delivery of the effect.
     */
    protected suspend fun emitEffectSuspend(effect: E) {
        _effects.emit(effect)
    }

    // ─────────────────────────────────────────────
    // INTENT HANDLING
    // ─────────────────────────────────────────────

    override fun dispatch(intent: I) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }

    /**
     * Handles incoming UI intents.
     *
     * This is the single entry point for all user actions.
     */
    protected abstract suspend fun handleIntent(intent: I)

    // ─────────────────────────────────────────────
    // ASYNC HELPERS
    // ─────────────────────────────────────────────

    /**
     * Executes a suspend operation on IO and reduces the result into state.
     *
     * ## Example
     *
     * ```kotlin
     * async(
     *     block = { repository.getItems() }
     * ) { result ->
     *     result.onSuccess { items ->
     *         copy(items = items)
     *     }
     * }
     * ```
     */
    protected fun <T> async(
        block: suspend () -> T,
        reducer: State.(Result<T>) -> State
    ) {
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { block() }
            }

            setState { reducer(result) }
        }
    }

    /**
     * Collects a Flow inside the ViewModel scope.
     *
     * Each emission is reduced into state.
     *
     * ## Example
     *
     * ```kotlin
     * collectFlow(
     *     source = { repository.observeItems() }
     * ) { items ->
     *     copy(items = items)
     * }
     * ```
     */
    protected fun <T> collectFlow(
        source: () -> Flow<T>,
        reducer: State.(T) -> State
    ) {
        viewModelScope.launch {
            source()
                .catch { /* optional: emitEffect(...) */ }
                .collect { value ->
                    setState { reducer(value) }
                }
        }
    }
}