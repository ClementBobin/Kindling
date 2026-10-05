package dev.kindling.compose.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State / one-shot events / async helpers shared by `KViewModel` (Android) and
 * `KSimpleViewModel` (all platforms), so the logic exists once.
 */
internal class KViewModelCore<State>(
    initialState: State,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> get() = _state

    fun updateState(block: State.() -> State) {
        _state.update { block.invoke(it) }
    }

    private val _events = Channel<Any>(Channel.BUFFERED)
    val events: Flow<Any> get() = _events.receiveAsFlow()

    fun sendEvent(obj: Any) {
        scope.launch { _events.send(obj) }
    }

    fun <T> collectData(
        source: suspend () -> Flow<T>,
        onResult: Result<T>.() -> Unit
    ) {
        scope.launch(kIoDispatcher) {
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

    fun <T> fetchData(
        source: suspend () -> T,
        onResult: Result<T>.() -> Unit
    ) {
        scope.launch(kIoDispatcher) {
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
