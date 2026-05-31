package dev.kindling.compose

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * A generic screen composable that handles common screen functionality for ViewModel-based screens.
 *
 * This composable provides a standardized way to handle state observation, event collection,
 * back navigation, and focus management across all screens in the application.
 *
 * @param State The type of UI state managed by the ViewModel
 * @param VM The type of ViewModel that extends [KViewModel]<State>
 * @param viewModel The ViewModel instance that manages the screen's state and logic
 * @param navController The NavController for handling navigation between screens
 * @param onBack Optional callback for handling back button presses. If null, back press is disabled.
 * @param onEvent Callback for handling custom events emitted by the ViewModel
 * @param content The main composable content of the screen that receives state and ViewModel
 *
 * @see KViewModel
 * @see NavController
 * @see BackHandler
 * @see LaunchedEffect
 *
 * @sample
 * Screen(
 *     viewModel = myViewModel,
 *     navController = navController
 * ) { state, viewModel ->
 *     MyScreenContent(
 *         state = state,
 *         onAction = { viewModel.handleAction(it) }
 *     )
 * }
 *
 */
@Composable
fun <State, VM: KViewModel<State>> Screen(
    viewModel: VM,
    navController: NavController,
    onBack: ((state: State, viewModel: VM) -> Unit)? = null,
    onEvent: (state: State, viewModel: VM, event: Any) -> Unit = { _, _, _ ->  },
    content: @Composable (state: State, viewModel: VM) -> Unit
) {

    // Observing the state from the view model.
    val state by viewModel.state.collectAsState()

    val focusManager = LocalFocusManager.current

    // Handle back press event.
    if (onBack != null)
        BackHandler(
            onBack = {
                focusManager.clearFocus()
                onBack(state, viewModel)
            }
        )

    // Collect events emitted by the ViewModel.
    LaunchedEffect(viewModel) {
        viewModel.events
            .onEach { event ->
                if (event is Destination) navController.navigate(destination = event)
                else onEvent(state, viewModel, event)
            }.collect()
    }

    content(state, viewModel)

}