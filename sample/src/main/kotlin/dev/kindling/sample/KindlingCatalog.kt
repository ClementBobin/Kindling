package dev.kindling.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.*
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
//  Root catalog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun KindlingCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        Text("Kindling Catalog", style = MaterialTheme.typography.headlineMedium)

        AspectRatioSection()
        AvatarSection()
        ButtonSection()
        CalendarSection()
        CardSection()
        CarouselSection()
        ComboboxSection()
        DataTableSection()
        DatePickerSection()
        DialogSection()
        DirectionSection()
        EmptySection()
        InputSection()
        InputGroupSection()
        InputOtpSection()
        LabelSection()
        MaskInputSection()
        PaginationSection()
        PopoverSection()
        SkeletonSection()
        SpinnerSection()
        StepperSection()
        TextareaSection()
        ToasterSection()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
    }
}

@Composable
private fun SubLabel(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  AspectRatio
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AspectRatioSection() {
    SectionHeader("AspectRatio")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SubLabel("16 / 9")
        KAspectRatio(ratio = 16f / 9f) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer)) {
                Text("16 : 9", modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        SubLabel("1 / 1 (square)")
        KAspectRatio(ratio = 1f, modifier = Modifier.width(120.dp)) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer)) {
                Text("1 : 1", modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        SubLabel("4 / 3")
        KAspectRatio(ratio = 4f / 3f) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiaryContainer)) {
                Text("4 : 3", modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Avatar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AvatarSection() {
    SectionHeader("Avatar")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SubLabel("Sizes — Sm / Default / Lg")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            KAvatar(fallbackText = "CN", size = KAvatarSize.Sm)
            KAvatar(fallbackText = "CN", size = KAvatarSize.Default)
            KAvatar(fallbackText = "CN", size = KAvatarSize.Lg)
        }

        SubLabel("Fallback initials")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KAvatar(fallbackText = "AB")
            KAvatar(fallbackText = "MX")
            KAvatar(fallbackText = "ZK")
        }

        SubLabel("No content (empty fallback)")
        KAvatar()

        SubLabel("With badge")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            KAvatar(fallbackText = "AB", size = KAvatarSize.Sm) {
                Box(Modifier.fillMaxSize().background(Color.Green))
            }
            KAvatar(fallbackText = "AB", size = KAvatarSize.Default) {
                Box(Modifier.fillMaxSize().background(Color(0xFF22C55E)))
            }
            KAvatar(fallbackText = "AB", size = KAvatarSize.Lg) {
                Box(Modifier.fillMaxSize().background(Color(0xFFEF4444)))
            }
        }

        SubLabel("AvatarGroup — maxVisible = 3, overflow shown")
        KAvatarGroup(
            avatars = listOf(
                KAvatarData(fallbackText = "CN"),
                KAvatarData(fallbackText = "AB"),
                KAvatarData(fallbackText = "MX"),
                KAvatarData(fallbackText = "ZK"),
                KAvatarData(fallbackText = "OP"),
            ),
            maxVisible = 3
        )

        SubLabel("AvatarGroup — Lg size")
        KAvatarGroup(
            avatars = listOf(
                KAvatarData(fallbackText = "AA"),
                KAvatarData(fallbackText = "BB"),
                KAvatarData(fallbackText = "CC"),
            ),
            size = KAvatarSize.Lg
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ButtonSection() {
    SectionHeader("Button")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SubLabel("Variants")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KButton(text = "Default",     onClick = {})
            KButton(text = "Secondary",   onClick = {}, variant = KButtonVariant.Secondary)
            KButton(text = "Outline",     onClick = {}, variant = KButtonVariant.Outline)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Ghost",       onClick = {}, variant = KButtonVariant.Ghost)
            KButton(text = "Destructive", onClick = {}, variant = KButtonVariant.Destructive)
            KButton(text = "Link",        onClick = {}, variant = KButtonVariant.Link)
        }

        SubLabel("Sizes")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            KButton(text = "Sm",      onClick = {}, size = KButtonSize.Sm)
            KButton(text = "Default", onClick = {})
            KButton(text = "Lg",      onClick = {}, size = KButtonSize.Lg)
            KButton(onClick = {}, size = KButtonSize.Icon) {
                Icon(Icons.Default.Star, contentDescription = null)
            }
        }

        SubLabel("States")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Enabled",  onClick = {})
            KButton(text = "Disabled", onClick = {}, enabled = false)
            KButton(text = "Loading",  onClick = {}, isLoading = true)
        }

        SubLabel("Destructive — all sizes")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            KButton(text = "Sm",  onClick = {}, variant = KButtonVariant.Destructive, size = KButtonSize.Sm)
            KButton(text = "Md",  onClick = {}, variant = KButtonVariant.Destructive)
            KButton(text = "Lg",  onClick = {}, variant = KButtonVariant.Destructive, size = KButtonSize.Lg)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Calendar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CalendarSection() {
    SectionHeader("Calendar")
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        SubLabel("Single selection")
        var singleDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(
            mode           = KCalendarMode.Single,
            selected       = singleDate,
            onSelectSingle = { singleDate = it }
        )
        Text("Selected: ${singleDate ?: "none"}", fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        SubLabel("Range selection")
        var range by remember { mutableStateOf(KDateRange()) }
        KCalendar(
            mode          = KCalendarMode.Range,
            selectedRange = range,
            onSelectRange = { range = it }
        )
        Text("From: ${range.from ?: "—"}  To: ${range.to ?: "—"}", fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        SubLabel("Dropdown caption")
        var dropDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(
            captionLayout  = KCalendarCaptionLayout.Dropdown,
            selected       = dropDate,
            onSelectSingle = { dropDate = it }
        )

        SubLabel("Two months side-by-side")
        var twoDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(numberOfMonths = 2, selected = twoDate, onSelectSingle = { twoDate = it })

        SubLabel("Show outside days = false, show week numbers")
        var noOutside by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(showOutsideDays = false, showWeekNumber = true,
            selected = noOutside, onSelectSingle = { noOutside = it })

        SubLabel("With presets")
        var presetDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(
            selected       = presetDate,
            onSelectSingle = { presetDate = it },
            presets        = listOf(
                KCalendarPreset("Today",    LocalDate.now()),
                KCalendarPreset("Tomorrow", LocalDate.now().plusDays(1)),
                KCalendarPreset("In a week", LocalDate.now().plusDays(7)),
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CardSection() {
    SectionHeader("Card")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SubLabel("Default size — header + content + footer")
        KCard {
            KCardHeader(
                action = { KButton(text = "Action", onClick = {}, size = KButtonSize.Sm) }
            ) {
                KCardTitle("Card Title")
                KCardDescription("This is a short description of the card content.")
            }
            KCardContent { Text("Main body content goes here.", fontSize = 12.sp) }
            KCardFooter {
                KButton(text = "Cancel",  onClick = {}, variant = KButtonVariant.Outline)
                KButton(text = "Confirm", onClick = {})
            }
        }

        SubLabel("Sm size — no footer")
        KCard(size = KCardSize.Sm) {
            KCardHeader {
                KCardTitle("Compact Card")
                KCardDescription("Reduced padding for dense UIs.")
            }
            KCardContent { Text("Compact content.", fontSize = 12.sp) }
        }

        SubLabel("Header only (no description)")
        KCard {
            KCardHeader { KCardTitle("Notifications") }
            KCardContent { Text("You have 3 unread messages.", fontSize = 12.sp) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Carousel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CarouselSection() {
    SectionHeader("Carousel")
    val items = listOf("Slide 1", "Slide 2", "Slide 3", "Slide 4")
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SubLabel("Horizontal — arrows + dots")
        KCarousel(pageCount = items.size) { page ->
            Card(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(items[page], style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        SubLabel("No arrows, no dots")
        KCarousel(pageCount = items.size, showArrows = false, showDots = false) { page ->
            Card(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(items[page])
                }
            }
        }

        SubLabel("Vertical (arrows disabled, dots visible)")
        KCarousel(
            pageCount   = items.size,
            orientation = KCarouselOrientation.Vertical,
            showArrows  = false,
            modifier    = Modifier.height(200.dp)
        ) { page ->
            Card(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(items[page])
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Combobox
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ComboboxSection() {
    SectionHeader("Combobox")
    val frameworks = listOf(
        KComboboxItem("next",   "Next.js"),
        KComboboxItem("svelte", "SvelteKit"),
        KComboboxItem("nuxt",   "Nuxt.js"),
        KComboboxItem("astro",  "Astro"),
        KComboboxItem("remix",  "Remix"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SubLabel("Default — nothing selected")
        var sel1 by remember { mutableStateOf<KComboboxItem?>(null) }
        KCombobox(items = frameworks, selected = sel1, onSelect = { sel1 = it },
            placeholder = "Select framework…")

        SubLabel("Pre-selected")
        var sel2 by remember { mutableStateOf<KComboboxItem?>(frameworks[1]) }
        KCombobox(items = frameworks, selected = sel2, onSelect = { sel2 = it })

        SubLabel("Multiple selection")
        var selM by remember { mutableStateOf(listOf(frameworks[0], frameworks[2])) }
        KCombobox(
            items            = frameworks,
            multiple         = true,
            selectedMultiple = selM,
            onSelectMultiple = { selM = it }
        )

        SubLabel("Disabled")
        KCombobox(items = frameworks, selected = null, onSelect = {}, enabled = false,
            placeholder = "Disabled")

        SubLabel("Custom empty label")
        KCombobox(items = emptyList(), selected = null, onSelect = {},
            emptyLabel = "No frameworks found.")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DataTable
// ─────────────────────────────────────────────────────────────────────────────

private data class Payment(val id: String, val status: String, val method: String, val amount: Int)

@Composable
private fun DataTableSection() {
    SectionHeader("DataTable")
    val payments = listOf(
        Payment("INV001", "Paid",    "Credit Card",   250),
        Payment("INV002", "Pending", "PayPal",         150),
        Payment("INV003", "Unpaid",  "Bank Transfer",  350),
        Payment("INV004", "Paid",    "Credit Card",    450),
        Payment("INV005", "Paid",    "PayPal",          50),
    )
    val columns = listOf(
        KTableColumn<Payment>("id",     "Invoice", sortable = true) { Text(it.id,            fontSize = 13.sp) },
        KTableColumn<Payment>("status", "Status",  sortable = true) { Text(it.status,         fontSize = 13.sp) },
        KTableColumn<Payment>("method", "Method")                   { Text(it.method,         fontSize = 13.sp) },
        KTableColumn<Payment>("amount", "Amount",  sortable = true) { Text("$${it.amount}",   fontSize = 13.sp) },
    )
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SubLabel("Basic table with sortable columns")
        KDataTable(
            columns = columns,
            data    = payments,
            onSort  = { _, _ -> /* re-sort in real usage */ }
        )

        SubLabel("Striped rows")
        KDataTable(columns = columns, data = payments.take(3), striped = true)

        SubLabel("Paginated (page size = 2)")
        KDataTable(columns = columns, data = payments, pageSize = 2)

        SubLabel("Empty state")
        KDataTable(columns = columns, data = emptyList())
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DatePicker
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DatePickerSection() {
    SectionHeader("DatePicker")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SubLabel("Basic")
        var date1 by remember { mutableStateOf<LocalDate?>(null) }
        KDatePicker(selected = date1, onSelect = { date1 = it }, placeholder = "Pick a date")

        SubLabel("With min date (today)")
        var date2 by remember { mutableStateOf<LocalDate?>(null) }
        KDatePicker(selected = date2, onSelect = { date2 = it }, minDate = LocalDate.now())

        SubLabel("With max date (end of year)")
        var date3 by remember { mutableStateOf<LocalDate?>(null) }
        KDatePicker(
            selected = date3,
            onSelect = { date3 = it },
            maxDate  = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear())
        )

        SubLabel("Disabled")
        KDatePicker(selected = null, onSelect = {}, enabled = false)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DialogSection() {
    SectionHeader("Dialog")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SubLabel("AlertDialog — confirm")
        var alertOpen by remember { mutableStateOf(false) }
        KButton(text = "Open Alert Dialog", onClick = { alertOpen = true },
            variant = KButtonVariant.Outline)
        KAlertDialog(
            open         = alertOpen,
            onDismiss    = { alertOpen = false },
            title        = "Are you absolutely sure?",
            description  = "This action cannot be undone.",
            confirmLabel = "Continue",
            onConfirm    = { alertOpen = false }
        )

        SubLabel("AlertDialog — destructive")
        var destructiveOpen by remember { mutableStateOf(false) }
        KButton(text = "Open Destructive Dialog", onClick = { destructiveOpen = true },
            variant = KButtonVariant.Destructive)
        KAlertDialog(
            open          = destructiveOpen,
            onDismiss     = { destructiveOpen = false },
            title         = "Delete account?",
            description   = "All your data will be permanently removed.",
            confirmLabel  = "Delete",
            isDestructive = true,
            onConfirm     = { destructiveOpen = false }
        )

        SubLabel("Free-form Dialog")
        var freeOpen by remember { mutableStateOf(false) }
        var editName by remember { mutableStateOf("") }
        KButton(text = "Open Free Dialog", onClick = { freeOpen = true })
        KDialog(open = freeOpen, onDismiss = { freeOpen = false }) {
            KDialogHeader(
                title       = "Edit Profile",
                description = "Make changes to your profile here."
            )
            Spacer(Modifier.height(12.dp))
            KFormField(label = "Name", value = editName, onValueChange = { editName = it },
                placeholder = "Your name")
            Spacer(Modifier.height(8.dp))
            KDialogFooter {
                KButton(text = "Cancel",       onClick = { freeOpen = false },
                    variant = KButtonVariant.Outline)
                KButton(text = "Save changes", onClick = { freeOpen = false })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Direction
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DirectionSection() {
    SectionHeader("Direction (LTR / RTL)")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SubLabel("LTR")
        KDirectionProvider(direction = androidx.compose.ui.unit.LayoutDirection.Ltr) {
            KButton(text = "Left-to-right button", onClick = {})
        }

        SubLabel("RTL")
        KDirectionProvider(direction = androidx.compose.ui.unit.LayoutDirection.Rtl) {
            KButton(text = "زر من اليمين إلى اليسار", onClick = {})
        }

        SubLabel("Toggle via KDirectionManager")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Set LTR", onClick = { KDirectionManager.set(androidx.compose.ui.unit.LayoutDirection.Ltr) },
                variant = KButtonVariant.Outline, size = KButtonSize.Sm)
            KButton(text = "Set RTL", onClick = { KDirectionManager.set(androidx.compose.ui.unit.LayoutDirection.Rtl) },
                variant = KButtonVariant.Outline, size = KButtonSize.Sm)
            KButton(text = "Toggle",  onClick = { KDirectionManager.toggle() },
                size = KButtonSize.Sm)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptySection() {
    SectionHeader("Empty / EmptyState")
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SubLabel("Preset — icon + title + description + action")
        KEmptyState(
            icon        = Icons.Outlined.Info,
            title       = "No projects",
            description = "Get started by creating your first project.",
            actionLabel = "Create Project",
            onAction    = {}
        )

        SubLabel("Preset — two actions")
        KEmptyState(
            icon                 = Icons.Outlined.Email,
            title                = "Your inbox is empty",
            description          = "Messages from your team will appear here.",
            actionLabel          = "Invite teammates",
            secondaryActionLabel = "Learn more",
            onAction             = {},
            onSecondaryAction    = {}
        )

        SubLabel("Outlined")
        KEmptyState(
            icon        = Icons.Outlined.Info,
            title       = "No files",
            description = "Upload a file to get started.",
            outlined    = true,
            actionLabel = "Upload",
            onAction    = {}
        )

        SubLabel("Show background")
        KEmptyState(
            icon           = Icons.Outlined.Email,
            title          = "All caught up",
            description    = "Nothing to see here.",
            showBackground = true
        )

        SubLabel("Custom KEmpty builder")
        KEmpty(outlined = true) {
            KEmptyHeader {
                KEmptyMedia(variant = KEmptyMediaVariant.Icon) {
                    Icon(Icons.Outlined.Info, null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxSize())
                }
                KEmptyTitle("Custom builder")
                KEmptyDescription("Use KEmpty + KEmptyHeader + KEmptyContent for full control.")
            }
            KEmptyContent {
                KButton(text = "Primary",   onClick = {})
                KButton(text = "Secondary", onClick = {}, variant = KButtonVariant.Outline)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Input
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InputSection() {
    SectionHeader("Input")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SubLabel("Default")
        var t1 by remember { mutableStateOf("") }
        KInput(value = t1, onValueChange = { t1 = it }, placeholder = "Placeholder text")

        SubLabel("With value")
        KInput(value = "Hello, world!", onValueChange = {})

        SubLabel("Password")
        var pwd by remember { mutableStateOf("") }
        KInput(value = pwd, onValueChange = { pwd = it }, placeholder = "Password", isPassword = true)

        SubLabel("Error state")
        KInput(value = "bad-value", onValueChange = {}, isError = true)

        SubLabel("Disabled")
        KInput(value = "", onValueChange = {}, placeholder = "Disabled", enabled = false)

        SubLabel("KFormField — label + helper + validation")
        var email by remember { mutableStateOf("") }
        KFormField(
            label         = "Email address",
            value         = email,
            onValueChange = { email = it },
            placeholder   = "m@example.com",
            helperText    = "We'll never share your email.",
            isError       = email.isNotEmpty() && !email.contains("@"),
            errorMessage  = "Please enter a valid email address."
        )

        SubLabel("KFormField — disabled")
        KFormField(label = "Read-only field", value = "Cannot edit this",
            onValueChange = {}, enabled = false)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  InputGroup
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InputGroupSection() {
    SectionHeader("InputGroup")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SubLabel("Leading icon text addon")
        var ig1 by remember { mutableStateOf("") }
        KInputGroup {
            KInputGroupAddon(align = KInputGroupAlign.InlineStart) { KInputGroupText("@") }
            KInputGroupInput(value = ig1, onValueChange = { ig1 = it }, placeholder = "username")
        }

        SubLabel("Trailing button addon")
        var ig2 by remember { mutableStateOf("") }
        KInputGroup {
            KInputGroupInput(value = ig2, onValueChange = { ig2 = it }, placeholder = "Search…")
            KInputGroupAddon(align = KInputGroupAlign.InlineEnd) {
                KInputGroupButton(onClick = { ig2 = "" }) {
                    Icon(Icons.Default.Star, contentDescription = "Clear",
                        modifier = Modifier.size(14.dp))
                }
            }
        }

        SubLabel("Block-start label")
        var ig3 by remember { mutableStateOf("") }
        KInputGroup {
            KInputGroupAddon(align = KInputGroupAlign.BlockStart) {
                KInputGroupText("Website URL")
            }
            KInputGroupInput(value = ig3, onValueChange = { ig3 = it },
                placeholder = "https://example.com")
        }

        SubLabel("Block-end hint")
        var ig4 by remember { mutableStateOf("") }
        KInputGroup {
            KInputGroupInput(value = ig4, onValueChange = { ig4 = it }, placeholder = "Amount")
            KInputGroupAddon(align = KInputGroupAlign.BlockEnd) { KInputGroupText("USD") }
        }

        SubLabel("Error state")
        var ig5 by remember { mutableStateOf("bad") }
        KInputGroup(isError = true) {
            KInputGroupAddon(align = KInputGroupAlign.InlineStart) { KInputGroupText("$") }
            KInputGroupInput(value = ig5, onValueChange = { ig5 = it }, isError = true)
        }

        SubLabel("Textarea variant")
        var ig6 by remember { mutableStateOf("") }
        KInputGroup {
            KInputGroupTextarea(value = ig6, onValueChange = { ig6 = it },
                placeholder = "Multi-line input…")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  InputOtp
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InputOtpSection() {
    SectionHeader("InputOTP")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SubLabel("6-digit OTP")
        var otp1 by remember { mutableStateOf("") }
        KInputOtp(value = otp1, onValueChange = { otp1 = it }, length = 6)
        Text("Value: $otp1", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SubLabel("4-digit PIN")
        var otp2 by remember { mutableStateOf("") }
        KInputOtp(value = otp2, onValueChange = { otp2 = it }, length = 4)

        SubLabel("6-digit with separator after slot 2 (0-indexed)")
        var otp3 by remember { mutableStateOf("") }
        KInputOtp(value = otp3, onValueChange = { otp3 = it }, length = 6, separatorAt = setOf(2))

        SubLabel("Error state")
        var otp4 by remember { mutableStateOf("123") }
        KInputOtp(value = otp4, onValueChange = { otp4 = it }, length = 6, isError = true)

        SubLabel("Disabled")
        KInputOtp(value = "123456", onValueChange = {}, length = 6, enabled = false)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LabelSection() {
    SectionHeader("Label")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SubLabel("Default")
        KLabel("Accept terms and conditions")

        SubLabel("Disabled")
        KLabel("Disabled label", disabled = true)

        SubLabel("Paired with checkbox")
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var checked by remember { mutableStateOf(false) }
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            KLabel("Accept terms and conditions")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  MaskInput
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MaskInputSection() {
    SectionHeader("MaskInput")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SubLabel("Phone — (###) ###-####")
        var phone by remember { mutableStateOf("") }
        KPhoneInput(value = phone, onValueChange = { phone = it })

        SubLabel("SSN — ###-##-####")
        var ssn by remember { mutableStateOf("") }
        KSsnInput(value = ssn, onValueChange = { ssn = it })

        SubLabel("Date — MM/DD/YYYY")
        var dateMask by remember { mutableStateOf("") }
        KDateMaskInput(value = dateMask, onValueChange = { dateMask = it })

        SubLabel("Credit card — #### #### #### ####")
        var cc by remember { mutableStateOf("") }
        KCreditCardInput(value = cc, onValueChange = { cc = it })

        SubLabel("EIN — ##-#######")
        var ein by remember { mutableStateOf("") }
        KEinInput(value = ein, onValueChange = { ein = it })

        SubLabel("Custom pattern — ##:##:##")
        var custom by remember { mutableStateOf("") }
        KMaskInput(value = custom, onValueChange = { custom = it },
            customPattern = "##:##:##", placeholder = "HH:MM:SS")

        SubLabel("Currency")
        var currency by remember { mutableStateOf("") }
        KCurrencyInput(value = currency, onValueChange = { currency = it })

        SubLabel("Percentage")
        var pct by remember { mutableStateOf("") }
        KPercentageInput(value = pct, onValueChange = { pct = it })

        SubLabel("License plate (alphanumeric)")
        var plate by remember { mutableStateOf("") }
        KMaskInput(value = plate, onValueChange = { plate = it },
            mask = KMaskPattern.LicensePlate, allowLetters = true)

        SubLabel("Error state")
        var errMask by remember { mutableStateOf("123") }
        KPhoneInput(value = errMask, onValueChange = { errMask = it }, isError = true)

        SubLabel("Disabled")
        KPhoneInput(value = "5551234567", onValueChange = {}, enabled = false)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Pagination
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PaginationSection() {
    SectionHeader("Pagination")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SubLabel("5 pages")
        var page5 by remember { mutableStateOf(1) }
        KPagination(currentPage = page5, totalPages = 5, onPageChange = { page5 = it })

        SubLabel("20 pages — with ellipsis")
        var page20 by remember { mutableStateOf(1) }
        KPagination(currentPage = page20, totalPages = 20, onPageChange = { page20 = it })

        SubLabel("Large page (middle) — sibling count = 2")
        var pageMid by remember { mutableStateOf(10) }
        KPagination(currentPage = pageMid, totalPages = 20, siblingCount = 2,
            onPageChange = { pageMid = it })

        SubLabel("No edge buttons")
        var pageNoEdge by remember { mutableStateOf(3) }
        KPagination(currentPage = pageNoEdge, totalPages = 10, showEdges = false,
            onPageChange = { pageNoEdge = it })

        SubLabel("Single page — hidden")
        KPagination(currentPage = 1, totalPages = 1, onPageChange = {})
        Text("(nothing shown above — total pages = 1)", fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Popover
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PopoverSection() {
    SectionHeader("Popover")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SubLabel("Bottom / Center (default)")
        var open1 by remember { mutableStateOf(false) }
        KPopover(
            open      = open1,
            onDismiss = { open1 = false },
            trigger   = { KButton(text = "Open Popover", onClick = { open1 = !open1 }) }
        ) {
            KPopoverHeader {
                KPopoverTitle("Dimensions")
                KPopoverDescription("Set the dimensions for the layer.")
            }
            Spacer(Modifier.height(8.dp))
            KInput(value = "100%", onValueChange = {})
        }

        SubLabel("Top / Start")
        var open2 by remember { mutableStateOf(false) }
        KPopover(
            open      = open2,
            onDismiss = { open2 = false },
            side      = KPopoverSide.Top,
            align     = KPopoverAlign.Start,
            trigger   = {
                KButton(text = "Top-Start Popover", onClick = { open2 = !open2 },
                    variant = KButtonVariant.Outline)
            }
        ) {
            KPopoverTitle("Top popover")
            KPopoverDescription("Anchored above the trigger, aligned to start.")
        }

        SubLabel("Right side")
        var open3 by remember { mutableStateOf(false) }
        KPopover(
            open      = open3,
            onDismiss = { open3 = false },
            side      = KPopoverSide.Right,
            trigger   = {
                KButton(text = "Right Popover", onClick = { open3 = !open3 },
                    variant = KButtonVariant.Secondary)
            }
        ) {
            KPopoverDescription("Appears to the right of the button.")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Skeleton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SkeletonSection() {
    SectionHeader("Skeleton")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SubLabel("Text lines")
        KSkeleton(modifier = Modifier.fillMaxWidth().height(20.dp))
        KSkeleton(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp))
        KSkeleton(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))

        SubLabel("Avatar + text rows")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            KSkeleton(modifier = Modifier.size(48.dp), shape = CircleShape)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KSkeleton(modifier = Modifier.width(120.dp).height(14.dp))
                KSkeleton(modifier = Modifier.width(80.dp).height(12.dp))
            }
        }

        SubLabel("Card placeholder")
        KSkeleton(modifier = Modifier.fillMaxWidth().height(80.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Spinner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SpinnerSection() {
    SectionHeader("Spinner")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SubLabel("Sizes — Sm / Default / Lg / Xl")
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            KSpinner(size = KSpinnerSize.Sm)
            KSpinner(size = KSpinnerSize.Default)
            KSpinner(size = KSpinnerSize.Lg)
            KSpinner(size = KSpinnerSize.Xl)
        }

        SubLabel("With label")
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.Top) {
            KSpinner(size = KSpinnerSize.Default, label = "Loading…")
            KSpinner(size = KSpinnerSize.Lg,      label = "Please wait…")
        }

        SubLabel("Custom colour")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            KSpinner(color = MaterialTheme.colorScheme.secondary)
            KSpinner(color = MaterialTheme.colorScheme.tertiary)
            KSpinner(color = Color(0xFFEF4444))
        }

        SubLabel("SpinnerOverlay (inside fixed-height box)")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            KSpinnerOverlay(label = "Loading…")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Stepper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepperSection() {
    SectionHeader("Stepper")
    val steps = listOf(
        KStep("Account", "Create your account"),
        KStep("Profile", "Set up your profile"),
        KStep("Review",  "Review your details"),
        KStep("Done",    "All set!")
    )
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        SubLabel("Horizontal — step 0 (first)")
        KStepper(steps = steps, currentStep = 0)

        SubLabel("Horizontal — step 2 (middle)")
        KStepper(steps = steps, currentStep = 2)

        SubLabel("Horizontal — step 3 (last / all complete)")
        KStepper(steps = steps, currentStep = 3)

        SubLabel("Horizontal — interactive (clickable steps)")
        var clickStep by remember { mutableStateOf(1) }
        KStepper(steps = steps, currentStep = clickStep, onStepClick = { clickStep = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Back", onClick = { if (clickStep > 0) clickStep-- },
                variant = KButtonVariant.Outline, size = KButtonSize.Sm, enabled = clickStep > 0)
            KButton(text = "Next", onClick = { if (clickStep < steps.lastIndex) clickStep++ },
                size = KButtonSize.Sm, enabled = clickStep < steps.lastIndex)
        }

        SubLabel("Vertical")
        KStepper(steps = steps, currentStep = 1, orientation = KStepperOrientation.Vertical)

        SubLabel("Vertical — all completed")
        KStepper(steps = steps, currentStep = steps.size, orientation = KStepperOrientation.Vertical)

        SubLabel("Steps without descriptions")
        val simpleSteps = listOf(KStep("Step 1"), KStep("Step 2"), KStep("Step 3"))
        KStepper(steps = simpleSteps, currentStep = 1)

        SubLabel("Error state on a step")
        val errorSteps = listOf(
            KStep("Account"),
            KStep("Verify", state = KStepState.Error),
            KStep("Done")
        )
        KStepper(steps = errorSteps, currentStep = 1)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Textarea
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TextareaSection() {
    SectionHeader("Textarea")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SubLabel("Default (auto-grow)")
        var ta1 by remember { mutableStateOf("") }
        KTextarea(value = ta1, onValueChange = { ta1 = it }, placeholder = "Type your message here.")

        SubLabel("With value")
        KTextarea(
            value         = "Pre-filled content that spans multiple lines to show the auto-sizing behaviour.",
            onValueChange = {}
        )

        SubLabel("Error state")
        KTextarea(value = "Invalid input", onValueChange = {}, isError = true)

        SubLabel("Disabled")
        KTextarea(value = "", onValueChange = {}, placeholder = "You cannot edit this.", enabled = false)

        SubLabel("Custom minLines = 4")
        var ta2 by remember { mutableStateOf("") }
        KTextarea(value = ta2, onValueChange = { ta2 = it },
            placeholder = "Starts at 4 lines tall.", minLines = 4)

        SubLabel("maxLines = 3 (scrolls after 3 lines)")
        var ta3 by remember { mutableStateOf("") }
        KTextarea(value = ta3, onValueChange = { ta3 = it },
            placeholder = "Capped at 3 visible lines.", maxLines = 3)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Toaster
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ToasterSection() {
    SectionHeader("Toaster")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SubLabel("Trigger toasts — KToasterHost must be in the root composition")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Default", onClick = { KToaster.show("Event created.") },
                variant = KButtonVariant.Outline)
            KButton(text = "Success",
                onClick = { KToaster.success("Saved!", "Your changes have been saved.") })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Error",
                onClick = { KToaster.error("Uh oh!", "Something went wrong.") },
                variant = KButtonVariant.Destructive)
            KButton(text = "Warning",
                onClick = { KToaster.warning("Low disk space.") },
                variant = KButtonVariant.Secondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Info",
                onClick = { KToaster.info("New version available.") },
                variant = KButtonVariant.Outline)
            KButton(
                text    = "With action",
                onClick = { KToaster.show("Event deleted.", actionLabel = "Undo") },
                variant = KButtonVariant.Ghost
            )
        }
    }
}