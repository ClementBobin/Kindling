package dev.kindling.compose

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * Legacy lifecycle-aware screen wrapper for [KViewModel]-driven Composables.
 *
 * `KScreen` is the original screen wrapper in the Kindling hierarchy. It centralizes
 * the boilerplate required for a standard feature screen:
 *
 * - **State collection**: Automatically collects the ViewModel's [KViewModel.state] and
 *   provides it to the content block.
 * - **Event dispatching**: One-shot side effects emitted via [KViewModel.events] are
 *   handled here. If an event implements [KDestination], it triggers automatic navigation.
 * - **Back press handling**: Optional custom handling for the system back button,
 *   with automatic focus clearing.
 *
 * ---
 *
 * ## Basic usage
 *
 * ```kotlin
 * @Composable
 * fun ProfileScreen(
 *     viewModel: ProfileViewModel,
 *     navController: NavController
 * ) {
 *     KScreen(
 *         viewModel = viewModel,
 *         navController = navController,
 *         onEvent = { state, vm, event ->
 *             when (event) {
 *                 is ProfileEvent.ShowToast -> {
 *                     // Handle toast
 *                 }
 *             }
 *         }
 *     ) { state, vm ->
 *         // Screen content
 *         ProfileContent(
 *             name = state.name,
 *             onLogout = vm::onLogoutClick
 *         )
 *     }
 * }
 * ```
 *
 * ---
 *
 * ## Automatic Navigation
 *
 * Any event emitted by the ViewModel that implements [KDestination] is automatically
 * intercepted and used to navigate via the provided [navController].
 *
 * ---
 *
 * @param State UI state type produced by [viewModel].
 * @param VM ViewModel type, must extend [KViewModel]<[State]>.
 * @param viewModel The [KViewModel] instance driving this screen.
 * @param navController [NavController] used for automatic navigation when [KDestination] events occur.
 * @param onBack Optional back press handler. Receives the latest state and ViewModel.
 *   Focus is cleared automatically before this callback is invoked.
 * @param onEvent Handler for non-navigation events emitted by [viewModel].
 *   Receives the latest state, the ViewModel, and the event object.
 * @param content The main UI content of the screen.
 *
 * @see KViewModel
 * @see KDestination
 */
@Composable
fun <State, VM : KViewModel<State>> KScreen(
    viewModel: VM,
    navController: NavController,
    onBack: ((state: State, viewModel: VM) -> Unit)? = null,
    onEvent: (state: State, viewModel: VM, event: Any) -> Unit = { _, _, _ -> },
    content: @Composable (state: State, viewModel: VM) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentOnBack by rememberUpdatedState(onBack)

    if (onBack != null) {
        BackHandler(
            onBack = {
                focusManager.clearFocus()
                currentOnBack?.invoke(state, viewModel)
            }
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events
            .onEach { event ->
                when (event) {
                    is NavigationEvent -> {
                        navController.navigate(
                            destination = event.destination,
                            navOptions = event.navOptions,
                            navigatorExtras = event.navigatorExtras
                        )
                    }
                    is KDestination -> {
                        navController.navigate(destination = event)
                    }
                    else -> {
                        currentOnEvent(state, viewModel, event)
                    }
                }
            }.collect()
    }

    content(state, viewModel)
}