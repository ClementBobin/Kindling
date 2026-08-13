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
 * `Screen` is the original screen wrapper in the Kindling hierarchy. It centralises
 * the boilerplate every screen needs:
 *
 * - **State collection** via [collectAsState]
 * - **One-shot event dispatch** — [Destination] events trigger automatic
 *   navigation; all other events are forwarded to [onEvent]
 * - **Back press handling** with optional focus clearing
 *
 * ---
 *
 * ## When to use
 *
 * | Screen wrapper | Use when |
 * |---|---|
 * | `Screen` ← you are here | ViewModel extends [KViewModel] (Application + Koin) |
 * | [KScreen][dev.kindling.compose.KScreen] | ViewModel extends the two-param [KViewModel][dev.kindling.compose.KViewModel] |
 *
 * ---
 *
 * ## Lifecycle awareness
 *
 * > **Note:** This wrapper uses [collectAsState] rather than `collectAsStateWithLifecycle`.
 * > State collection therefore continues while the app is in the background.
 * > Prefer [KScreen][dev.kindling.compose.KScreen] for new screens where
 * > lifecycle-safe collection is desired.
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
 *     Screen(
 *         viewModel = viewModel,
 *         navController = navController,
 *         onEvent = { _, _, event ->
 *             when (event) {
 *                 is ProfileEvent.ShowToast ->
 *                     Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
 *                 else -> Unit
 *             }
 *         }
 *     ) { state, vm ->
 *         ProfileContent(
 *             state = state,
 *             onSave = vm::save
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
 * sendEvent(AppScreen.Home)           // ← automatically navigated by Screen
 * sendEvent(ProfileEvent.ShowToast()) // ← forwarded to onEvent
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
 * Screen(
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
 * and default navigation behaviour applies.
 *
 * ---
 *
 * @param State UI state type produced by [viewModel].
 * @param VM ViewModel type, must extend [KViewModel]<[State]>.
 * @param viewModel The [KViewModel] driving this screen.
 * @param navController [NavController] used for automatic [Destination] navigation.
 * @param onBack Optional back press handler. Receives the latest state and ViewModel.
 *   Pass `null` to leave the system back behaviour unchanged.
 * @param onEvent Handler for non-navigation events emitted by [viewModel].
 *   Receives the latest state, the ViewModel, and the raw event object.
 *   Defaults to a no-op.
 * @param content Main screen content. Receives the latest [State] and [viewModel].
 *
 * @see KViewModel
 * @see dev.kindling.compose.KScreen
 * @see dev.kindling.compose.legacy.SimpleScreen
 * @see Destination
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
                    is Destination -> {
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