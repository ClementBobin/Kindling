# Kindling

[![CI](https://github.com/ClementBobin/Kindling/actions/workflows/ci.yml/badge.svg)](https://github.com/ClementBobin/Kindling/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.clementbobin.kindling/core)](https://central.sonatype.com/search?q=io.github.clementbobin.kindling)
[![JitPack](https://jitpack.io/v/ClementBobin/Kindling.svg)](https://jitpack.io/#ClementBobin/Kindling)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A production-ready Kotlin multi-module component library for Jetpack Compose — shadcn/ui-inspired UI components, typed navigation, a structured ViewModel base, and coroutine utilities, all fully theme-aware via Material3.

Docs site: https://clementbobin.github.io/Kindling/

---

## Modules

| Module | Artifact | Description | Distribution |
|--------|----------|-------------|--------------|
| `core` | `io.github.clementbobin.kindling:core` | Shadcn/ui-style Compose components (Button, Input, Dialog, Carousel, DataTable, DatePicker, Stepper, Toaster, Skeleton, Spinner, Combobox, Empty state, Pagination…) | Maven Central · JitPack |
| `utils` | `io.github.clementbobin.kindling:utils` | Coroutine utilities: `Debouncer<T>`, `Throttler<T>`, `debounceLeading`, `throttleFirst` Flow extensions | Maven Central · JitPack |
| `compose` | `io.github.clementbobin.kindling:compose` | Typed navigation (`Destination`, `KNavHost`), `KViewModel` base with state/events/data-loading helpers | Maven Central only |

---

## Installation

### Gradle Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io") // fallback for core and utils
}

dependencies {
    // UI components (Compose + Material3)
    implementation("io.github.clementbobin.kindling:core:0.3.0")

    // Coroutine utilities (debounce, throttle)
    implementation("io.github.clementbobin.kindling:utils:0.3.0")

    // Typed navigation + KViewModel (Android only, Maven Central)
    implementation("io.github.clementbobin.kindling:compose:0.3.0")
}
```

### Gradle Groovy DSL

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'io.github.clementbobin.kindling:core:0.3.0'
    implementation 'io.github.clementbobin.kindling:utils:0.3.0'
    implementation 'io.github.clementbobin.kindling:compose:0.3.0'
}
```

> **Note:** `:compose` is an Android library and is only published to Maven Central. `:core` and `:utils` are available on both Maven Central and JitPack.

### Requirements

- **minSdk** 26+
- **JDK** 17+
- **Kotlin** 2.2.0+
- **Compose BOM** 2025.05.00+

---

## Components — `core`

All components read colours exclusively from `MaterialTheme.colorScheme`, so they work with any light/dark scheme out of the box.

### KButton

```kotlin
// Variants
KButton(text = "Default",     onClick = {})
KButton(text = "Secondary",   onClick = {}, variant = KButtonVariant.Secondary)
KButton(text = "Outline",     onClick = {}, variant = KButtonVariant.Outline)
KButton(text = "Ghost",       onClick = {}, variant = KButtonVariant.Ghost)
KButton(text = "Destructive", onClick = {}, variant = KButtonVariant.Destructive)
KButton(text = "Link",        onClick = {}, variant = KButtonVariant.Link)

// Sizes
KButton(text = "Small",   onClick = {}, size = KButtonSize.Sm)
KButton(text = "Large",   onClick = {}, size = KButtonSize.Lg)
KButton(text = "Icon",    onClick = {}, size = KButtonSize.Icon) { Icon(Icons.Default.Add, null) }

// States
KButton(text = "Loading",  onClick = {}, isLoading = true)
KButton(text = "Disabled", onClick = {}, enabled = false)
```

### KInput / KFormField

```kotlin
var value by remember { mutableStateOf("") }

KInput(
    value         = value,
    onValueChange = { value = it },
    placeholder   = "m@example.com"
)

// With label, helper text and validation
KFormField(
    label         = "Email",
    value         = email,
    onValueChange = { email = it },
    placeholder   = "m@example.com",
    helperText    = "We'll never share your email.",
    isError       = email.isNotEmpty() && !email.contains("@"),
    errorMessage  = "Please enter a valid email address."
)

// Password field
KInput(value = pwd, onValueChange = { pwd = it }, isPassword = true)
```

### KDialog / KAlertDialog

```kotlin
// Alert (confirmation / destructive)
var open by remember { mutableStateOf(false) }

KAlertDialog(
    open          = open,
    onDismiss     = { open = false },
    title         = "Are you absolutely sure?",
    description   = "This action cannot be undone.",
    confirmLabel  = "Delete",
    isDestructive = true,
    onConfirm     = { /* delete */ open = false }
)

// Free-form dialog
KDialog(open = open, onDismiss = { open = false }) {
    KDialogHeader(title = "Edit Profile", description = "Make changes here.")
    Spacer(Modifier.height(16.dp))
    // … form fields …
    KDialogFooter {
        KButton(text = "Cancel", onClick = { open = false }, variant = KButtonVariant.Outline)
        KButton(text = "Save",   onClick = { open = false })
    }
}
```

### KToaster

Place `KToasterHost` once at the root of your composable tree, then call `KToaster` from anywhere:

```kotlin
// Root
Box(Modifier.fillMaxSize()) {
    NavHost(…)
    KToasterHost()
}

// Anywhere in the app
KToaster.success("Saved!")
KToaster.error("Upload failed", "Please try again.")
KToaster.warning("Low disk space")
KToaster.info("New version available")
KToaster.show(
    message     = "Event deleted",
    actionLabel = "Undo",
    onAction    = { /* undo */ }
)
```

### KCarousel

```kotlin
KCarousel(pageCount = items.size) { page ->
    Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Text(items[page])
    }
}

// Auto-play
KCarousel(
    pageCount   = items.size,
    autoPlayMs  = 3_000L,
    showDots    = true,
    showArrows  = true
) { page -> AsyncImage(items[page].imageUrl) }
```

### KDataTable

```kotlin
val columns = listOf(
    KTableColumn<Invoice>("id",     "Invoice",  sortable = true) { Text(it.id)            },
    KTableColumn<Invoice>("status", "Status",   sortable = true) { StatusBadge(it.status) },
    KTableColumn<Invoice>("amount", "Amount")                    { Text("$${it.amount}")   },
)

KDataTable(
    columns  = columns,
    data     = invoices,
    striped  = true,
    pageSize = 10,
    onSort   = { key, dir -> viewModel.sort(key, dir) }
)
```

### KDatePicker

```kotlin
var date by remember { mutableStateOf<LocalDate?>(null) }

KDatePicker(
    selected    = date,
    onSelect    = { date = it },
    placeholder = "Pick a date",
    minDate     = LocalDate.now()
)
```

### KStepper

```kotlin
val steps = listOf(
    KStep("Account", "Create your account"),
    KStep("Profile", "Set up your profile"),
    KStep("Review",  "Review your details"),
    KStep("Done",    "All set!")
)

var currentStep by remember { mutableStateOf(0) }

KStepper(steps = steps, currentStep = currentStep)

// Vertical
KStepper(steps = steps, currentStep = currentStep, orientation = KStepperOrientation.Vertical)

// Clickable steps
KStepper(steps = steps, currentStep = currentStep, onStepClick = { currentStep = it })
```

### KCombobox

```kotlin
val frameworks = listOf(
    KComboboxItem("next",   "Next.js"),
    KComboboxItem("svelte", "SvelteKit"),
    KComboboxItem("nuxt",   "Nuxt.js"),
)

var selected by remember { mutableStateOf<KComboboxItem?>(null) }

KCombobox(
    items       = frameworks,
    selected    = selected,
    onSelect    = { selected = it },
    placeholder = "Select framework…"
)
```

### KSkeleton

```kotlin
// Raw skeleton block
KSkeleton(modifier = Modifier.fillMaxWidth().height(20.dp))
KSkeleton(modifier = Modifier.size(40.dp), shape = CircleShape)

// Presets
repeat(3) { KSkeletonListItem() }
KSkeletonCard()
```

### KSpinner

```kotlin
KSpinner()
KSpinner(size = KSpinnerSize.Lg, label = "Loading…")

// Full-screen overlay
if (isLoading) KSpinnerOverlay()
```

### KPagination

```kotlin
var page by remember { mutableStateOf(1) }

KPagination(
    currentPage  = page,
    totalPages   = 20,
    onPageChange = { page = it }
)
```

### KEmpty / KEmptyState

```kotlin
// Convenience preset
KEmptyState(
    icon        = Icons.Outlined.FolderOpen,
    title       = "No projects",
    description = "Create your first project to get started.",
    actionLabel = "Create Project",
    onAction    = { navController.navigate(Screen.NewProject) }
)

// Composable builder
KEmpty(outlined = true) {
    KEmptyHeader {
        KEmptyMedia { Icon(Icons.Outlined.Inbox, null) }
        KEmptyTitle("Nothing here yet")
        KEmptyDescription("Your inbox is empty.")
    }
    KEmptyContent {
        KButton(text = "Refresh", onClick = { viewModel.refresh() }, variant = KButtonVariant.Outline)
    }
}
```

### KDirectionProvider (RTL support)

```kotlin
// Wrap your nav host to support RTL
KDirectionProvider(direction = KDirectionManager.direction) {
    NavHost(…)
}

// Toggle at runtime
KButton("Toggle RTL", onClick = { KDirectionManager.toggle() })

// Set from locale
KDirectionManager.setFromLocale(Locale("ar"))
```

---

## Navigation & ViewModel — `compose`

### Typed navigation

```kotlin
// Define routes once
sealed class Screen(route: String, arguments: List<NamedNavArgument> = emptyList())
    : Destination(route, arguments) {

    object Splash  : Screen("splash")
    object Home    : Screen("home")
    object Profile : Screen(
        route     = "profile/{userId}",
        arguments = listOf(navArgument("userId") { type = NavType.StringType })
    )
}

// Build the graph — no raw strings
@Composable
fun AppNavHost(navController: NavHostController) {
    KNavHost(navController, startDestination = Screen.Splash) {
        composable(Screen.Splash)  { SplashScreen(navController) }
        composable(Screen.Home)    { HomeScreen(navController)   }
        composable(Screen.Profile) { backStack ->
            val userId = backStack.arguments?.getString("userId") ?: ""
            ProfileScreen(userId, navController)
        }
    }
}

// Navigate — no raw strings
navController.navigate(Screen.Home)
navController.navigate(
    destination = Screen.Home,
    navOptions  = navOptions { popUpTo(Screen.Splash.route) { inclusive = true } }
)
navController.popUpTo(Screen.Login, inclusive = true)
```

### KViewModel

```kotlin
data class HomeState(
    val isLoading: Boolean      = true,
    val items:     List<String> = emptyList()
)

sealed interface HomeEvent {
    data class ShowError(val message: String) : HomeEvent
    object NavigateToDetail                   : HomeEvent
}

class HomeViewModel(
    private val repository: ItemRepository
) : KViewModel<HomeState, HomeEvent>(HomeState()) {

    init { loadItems() }

    private fun loadItems() {
        fetchData(
            source   = { repository.getItems() },
            onResult = { result ->
                result
                    .onSuccess { items -> updateState { copy(items = items, isLoading = false) } }
                    .onFailure { e    -> sendEvent(HomeEvent.ShowError(e.message ?: "Unknown error")) }
            }
        )
    }
}

// Composable
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowError       -> snackbarHostState.showSnackbar(event.message)
                is HomeEvent.NavigateToDetail -> navController.navigate(Screen.Detail)
            }
        }
    }

    if (state.isLoading) KSpinnerOverlay()
    else LazyColumn { items(state.items) { Text(it) } }
}
```

---

## Utilities — `utils`

### Debouncer

```kotlin
// In a ViewModel
private val searchDebouncer = KDebounce<String>(viewModelScope, 300.milliseconds) { query ->
    search(query)
}

// In the UI
KInput(
    value         = searchText,
    onValueChange = { searchText = it; searchDebouncer.emit(it) }
)

// Or collect the flow directly
searchDebouncer.flow.collect { stableQuery -> search(stableQuery) }
```

### Throttler

```kotlin
// Prevent double-taps
private val submitThrottler = KThrottle<Unit>(viewModelScope, 500.milliseconds) {
    submitForm()
}

KButton(text = "Submit", onClick = { submitThrottler.emit(Unit) })
```

### Flow extensions

```kotlin
// Leading-edge debounce on any Flow
searchFlow
    .kDebounceLeading(300.milliseconds)
    .collect { query -> search(query) }

// Throttle to one emission per period
clickFlow
    .kThrottleFirst(500.milliseconds)
    .collect { handleClick() }
```

---

## Build & test

```bash
./gradlew build
./gradlew test
./gradlew :utils:test   # utils tests only
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

Branch naming: `feat/<short-description>` · `fix/<short-description>` · `chore/<short-description>`

---

## Publishing

Releases are published automatically to Maven Central when a GitHub release is created (via the `publish.yml` workflow). The Git tag (e.g. `0.3.0` or `v0.3.0`) sets the artifact version.

Required repository secrets: `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `SIGNING_KEY_ID`, `SIGNING_PASSWORD`, `GPG_KEY_CONTENTS`.

`:core` and `:utils` are additionally available on JitPack as a fallback (built from the `jitpack.yml` at the repo root). `:compose` is Android-only and distributed exclusively through Maven Central.

---

## License

Licensed under the [Apache License 2.0](LICENSE).
