package dev.kindling.compose.experimental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kindling.compose.KScreen
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
 * Intents represent user actions such as button clicks, text input changes,
 * or lifecycle events like `OnStart`. They are the only way to trigger
 * state changes or side effects in an MVI architecture.
 *
 * ### Example usage:
 * ```kotlin
 * sealed interface HomeIntent : Intent {
 *     data object LoadData : HomeIntent
 *     data class Search(val query: String) : HomeIntent
 *     data class ToggleFavorite(val itemId: String) : HomeIntent
 * }
 * ```
 */
interface Intent

/**
 * One-time side effects emitted by the ViewModel to the UI layer.
 *
 * Effects are consumed exactly once and are not part of the persistent UI state.
 * Typical examples include navigation, showing snackbars, or triggering Haptic feedback.
 *
 * ### Example usage:
 * ```kotlin
 * sealed interface HomeEffect : Effect {
 *     data class ShowToast(val message: String) : HomeEffect
 *     data class NavigateToDetails(val id: String) : HomeEffect
 *     data object CloseApp : HomeEffect
 * }
 * ```
 */
interface Effect

/**
 * Core contract for MVI ViewModels in Kindling.
 *
 * Defines a strict unidirectional flow where:
 * 1. The UI dispatches an [Intent].
 * 2. The ViewModel processes the Intent and updates the [state] or emits an [Effect].
 * 3. The UI observes the [state] and reacts to [effects].
 *
 * ### Architecture overview:
 * ```
 * UI (Composable) --[Intent]--> ViewModel --[State / Effect]--> UI (Composable)
 * ```
 *
 * @param State The UI state type.
 * @param I The Intent type.
 * @param E The Effect type.
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
 * Base implementation of a strict MVI (Model-View-Intent) ViewModel.
 *
 * This class provides the foundational logic for managing state and side effects
 * in a thread-safe and lifecycle-aware manner. It is designed to work seamlessly
 * with [KScreen].
 *
 * ### Key Features:
 * - **State Management**: Uses a [StateFlow] to hold and emit immutable state objects.
 * - **Side Effects**: Uses a buffered [SharedFlow] for one-shot effects (e.g., navigation).
 * - **Unidirectional Flow**: Enforces the [Intent] -> [State] pattern.
 * - **Async Helpers**: Built-in [async] and [collectFlow] for coroutine-safe data fetching.
 *
 * ### Implementation Example:
 * ```kotlin
 * data class ProfileState(val name: String = "", val isLoading: Boolean = false)
 * sealed interface ProfileIntent : Intent { data object Load : ProfileIntent }
 * sealed interface ProfileEffect : Effect { data class ShowError(val msg: String) : ProfileEffect }
 *
 * class ProfileViewModel(val repo: Repo) : KMviViewModel<ProfileState, ProfileIntent, ProfileEffect>(ProfileState()) {
 *     override suspend fun handleIntent(intent: ProfileIntent) {
 *         when (intent) {
 *             ProfileIntent.Load -> {
 *                 setState { copy(isLoading = true) }
 *                 async({ repo.getName() }) { result ->
 *                     result.onSuccess { copy(name = it, isLoading = false) }
 *                           .onFailure { 
 *                               emitEffect(ProfileEffect.ShowError("Failed"))
 *                               copy(isLoading = false) 
 *                           }
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param State The immutable UI state data class.
 * @param I The Intent type representing user actions.
 * @param E The Effect type for one-shot side effects.
 * @param initialState The state emitted immediately on subscription.
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