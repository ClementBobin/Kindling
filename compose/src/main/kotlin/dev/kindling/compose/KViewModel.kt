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
 * A base ViewModel class that provides common functionality for state management and data handling.
 *
 * This class extends [AndroidViewModel] and implements [KoinComponent] for dependency injection.
 * It provides a structured approach to managing UI state, events, and asynchronous data operations.
 *
 * @param State The type of the UI state managed by this ViewModel
 * @param initialState The initial state when the ViewModel is created
 * @param application The application context
 *
 * @property state The current UI state as a [StateFlow] for observing state changes
 * @property events A flow of one-time events that should be handled by the UI
 *
 * @see AndroidViewModel
 * @see KoinComponent
 * @see StateFlow
 * @see Channel
 *
 * @sample
 * // Example usage:
 * class MyViewModel(application: Application) : ViewModel<MyState>(
 *     initialState = MyState(),
 *     application = application
 * ) {
 *     fun loadData() {
 *         fetchData(
 *             source = { repository.getData() },
 *             onResult = { result ->
 *                 result.onSuccess { data ->
 *                     updateState { copy(data = data) }
 *                 }.onFailure { error ->
 *                     sendEvent(ErrorEvent(error.message))
 *                 }
 *             }
 *         )
 *     }
 * }
 */
open class KViewModel<State>(initialState: State, application: Application): AndroidViewModel(application),
    KoinComponent {

    /**
     * Private mutable state flow that holds the current UI state.
     */
    private val _state = MutableStateFlow(initialState)

    /**
     * Public immutable state flow that exposes the current UI state.
     * Use this to observe state changes in Composables or Activities/Fragments.
     */
    val state: StateFlow<State>
        get() = _state

    /**
     * Private channel for sending one-time events to the UI.
     */
    private val _events = Channel<Any>(Channel.BUFFERED)

    /**
     * Public flow of events that should be handled by the UI.
     * These are typically one-time events like navigation commands, toast messages, or dialogs.
     */
    val events: Flow<Any>
        get() = _events.receiveAsFlow()

    /**
     * Updates the current state using a transformation function.
     *
     * This method provides a safe way to update the state by applying a transformation
     * function to the current state. The update is performed atomically.
     *
     * @param block A lambda that takes the current state and returns a new state
     *
     * @sample
     * // Update state example:
     * updateState { copy(isLoading = true, data = emptyList()) }
     */
    protected fun updateState(block: State.() -> State) {
        _state.update { block.invoke(it) }
    }

    /**
     * Sends a one-time event to the UI layer.
     *
     * Events are useful for actions that should be handled once, such as showing
     * a snackbar, navigating to another screen, or displaying a dialog.
     *
     * @param obj The event object to send to the UI
     *
     * @sample
     * // Send event example:
     * sendEvent(NavigationEvent.Destination.Home)
     * sendEvent(MessageEvent("Data loaded successfully"))
     */
    protected fun sendEvent(obj: Any) {
        viewModelScope.launch {
            _events.send(obj)
        }
    }

    /**
     * Collects data from a flow and handles the results on the appropriate dispatchers.
     *
     * This method automatically handles the coroutine context switching:
     * - Collection happens on IO dispatcher for background work
     * - Results are delivered on Main dispatcher for UI updates
     * - Errors are caught and delivered as [Result.failure]
     *
     * @param T The type of data being collected
     * @param source A suspending function that returns a flow to collect from
     * @param onResult A callback function that receives the result of the collection
     *
     * @sample
     * // Collect data example:
     * collectData(
     *     source = { repository.getLiveData() },
     *     onResult = { result ->
     *         result.onSuccess { data ->
     *             updateState { copy(items = data) }
     *         }.onFailure { error ->
     *             sendEvent(ErrorEvent("Failed to load data: ${error.message}"))
     *         }
     *     }
     * )
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
     * Fetches data from a suspending function and handles the results on the appropriate dispatchers.
     *
     * This method automatically handles the coroutine context switching:
     * - Execution happens on IO dispatcher for background work
     * - Results are delivered on Main dispatcher for UI updates
     * - Errors are caught and delivered as [Result.failure]
     *
     * @param T The type of data being fetched
     * @param source A suspending function that returns the data to fetch
     * @param onResult A callback function that receives the result of the operation
     *
     * @sample
     * // Fetch data example:
     * fetchData(
     *     source = { repository.fetchUserData() },
     *     onResult = { result ->
     *         result.onSuccess { user ->
     *             updateState { copy(user = user, isLoading = false) }
     *         }.onFailure { error ->
     *             updateState { copy(isLoading = false) }
     *             sendEvent(ErrorEvent("User data load failed"))
     *         }
     *     }
     * )
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