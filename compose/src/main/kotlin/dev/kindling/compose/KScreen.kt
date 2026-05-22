package dev.kindling.compose

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

/**
 * Generic screen wrapper for ViewModel-driven Compose screens.
 *
 * `KScreen` centralizes common screen responsibilities:
 *
 * - Lifecycle-aware state collection
 * - One-shot event handling
 * - Explicit navigation processing
 * - Back press handling
 * - Focus clearing on navigation/back actions
 *
 * This keeps feature screens lightweight and consistent across projects.
 *
 * ## Example
 *
 * ```kotlin
 * KScreen(
 *     viewModel = homeViewModel,
 *     navController = navController,
 *     onEvent = { _, _, event ->
 *         when(event) {
 *             is HomeEvent.ShowSnackbar -> {
 *                 snackbarHostState.showSnackbar(event.message)
 *             }
 *         }
 *     }
 * ) { state, viewModel ->
 *
 *     HomeScreenContent(
 *         state = state,
 *         onRefresh = viewModel::refresh
 *     )
 * }
 * ```
 *
 * @param State UI state type.
 * @param Event One-shot event type.
 * @param VM ViewModel implementation.
 * @param viewModel ViewModel driving the screen.
 * @param navController Compose navigation controller.
 * @param onBack Optional back press callback.
 * @param onEvent Optional custom event handler.
 * @param content Main screen content.
 */
@Composable
fun <State, Event, VM : KViewModel<State, Event>> KScreen(
    viewModel: VM,
    navController: NavController,
    onBack: ((state: State, viewModel: VM) -> Unit)? = null,
    onEvent: suspend (state: State, viewModel: VM, event: Event) -> Unit = { _, _, _ -> },
    content: @Composable (state: State, viewModel: VM) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentState by rememberUpdatedState(state)

    val focusManager = LocalFocusManager.current

    // Handle back press.
    if (onBack != null) {
        BackHandler {
            focusManager.clearFocus()
            onBack(currentState, viewModel)
        }
    }

    // Collect events.
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->

            when (event) {
                is NavigationEvent -> {
                    navController.navigate(
                        destination = event.destination,
                        navOptions = event.navOptions,
                        navigatorExtras = event.navigatorExtras
                    )
                }

                else -> {
                    onEvent(currentState, viewModel, event)
                }
            }
        }
    }

    content(currentState, viewModel)
}