package dev.kindling.compose.legacy

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.kindling.compose.Destination

/**
 * Lifecycle-aware screen wrapper for [KSimpleViewModel]-driven Composables.
 *
 * `KSimpleScreen` is the legacy-tier counterpart to
 * [KScreen][dev.kindling.compose.KScreen]. It centralises the boilerplate
 * every screen needs:
 *
 * - **Lifecycle-aware state collection** via [collectAsStateWithLifecycle]
 * - **One-shot event dispatch** — [Destination] events trigger automatic
 *   navigation; all other events are forwarded to [onEvent]
 * - **Back press handling** with optional focus clearing
 * - **Snapshot-safe state capture** via [rememberUpdatedState], ensuring
 *   callbacks always see the latest state without restarting effects
 *
 * ---
 *
 * ## When to use
 *
 * Use `KSimpleScreen` when your ViewModel extends [KSimpleViewModel].
 * For typed-event ViewModels use [KScreen][dev.kindling.compose.KScreen] instead.
 *
 * ---
 *
 * ## Basic usage
 *
 * ```kotlin
 * @Composable
 * fun CounterScreen(
 *     viewModel: CounterViewModel,
 *     navController: NavController
 * ) {
 *     KSimpleScreen(
 *         viewModel = viewModel,
 *         navController = navController,
 *         onEvent = { _, _, event ->
 *             when (event) {
 *                 is CounterEvent.ShowToast -> Toast.makeText(
 *                     context,
 *                     (event as CounterEvent.ShowToast).message,
 *                     Toast.LENGTH_SHORT
 *                 ).show()
 *             }
 *         }
 *     ) { state, vm ->
 *         CounterContent(
 *             count = state.count,
 *             onIncrement = vm::increment,
 *             onReset = vm::reset
 *         )
 *     }
 * }
 * ```
 *
 * ---
 *
 * ## Navigation
 *
 * Any event that implements [Destination] is intercepted and forwarded directly
 * to the [NavController] — no extra handling needed in [onEvent]:
 *
 * ```kotlin
 * // In your ViewModel:
 * sendEvent(Screen.Home)          // ← automatically navigated by KSimpleScreen
 * sendEvent(MyEvent.ShowToast("")) // ← forwarded to onEvent
 * ```
 *
 * ---
 *
 * ## Back handling
 *
 * Supply [onBack] to intercept the system back press. Focus is cleared
 * automatically before the callback fires, dismissing any open keyboard:
 *
 * ```kotlin
 * KSimpleScreen(
 *     viewModel = viewModel,
 *     navController = navController,
 *     onBack = { state, vm ->
 *         if (state.hasUnsavedChanges) vm.showDiscardDialog()
 *         else navController.popBackStack()
 *     }
 * ) { state, vm -> ... }
 * ```
 *
 * If [onBack] is `null` (the default), the system back press is not intercepted
 * and the default navigation behaviour applies.
 *
 * ---
 *
 * @param State UI state type produced by [viewModel].
 * @param VM ViewModel type, must extend [KSimpleViewModel]<[State]>.
 * @param viewModel The [KSimpleViewModel] driving this screen.
 * @param navController [NavController] used for automatic [Destination] navigation.
 * @param onBack Optional back press handler. Receives the latest state and ViewModel.
 *   Pass `null` to leave the system back behaviour unchanged.
 * @param onEvent Handler for non-navigation events emitted by [viewModel].
 *   Receives the latest state, the ViewModel, and the raw event object.
 *   Defaults to a no-op.
 * @param content Main screen content. Receives the latest [State] and [viewModel].
 *
 * @see KSimpleViewModel
 * @see dev.kindling.compose.KScreen
 * @see dev.kindling.compose.Destination
 */
@Composable
fun <State, VM : KSimpleViewModel<State>> KSimpleScreen(
    viewModel: VM,
    navController: NavController,
    onBack: ((state: State, viewModel: VM) -> Unit)? = null,
    onEvent: (state: State, viewModel: VM, event: Any) -> Unit = { _, _, _ -> },
    content: @Composable (state: State, viewModel: VM) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentState by rememberUpdatedState(state)
    val focusManager = LocalFocusManager.current

    if (onBack != null) {
        BackHandler {
            focusManager.clearFocus()
            onBack(currentState, viewModel)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is Destination) navController.navigate(event)
            else onEvent(currentState, viewModel, event)
        }
    }

    content(currentState, viewModel)
}