package dev.kindling.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.*
import dev.kindling.core.components.ui.KAspectRatio
import dev.kindling.core.components.ui.KButton
import dev.kindling.core.components.ui.KButtonSize
import dev.kindling.core.components.ui.KButtonVariant
import dev.kindling.core.components.ui.avatar.KAvatar
import dev.kindling.core.components.ui.avatar.KAvatarBadge
import dev.kindling.core.components.ui.avatar.KAvatarFallback
import dev.kindling.core.components.ui.avatar.KAvatarGroup
import dev.kindling.core.components.ui.avatar.KAvatarGroupCount
import dev.kindling.core.components.ui.avatar.KAvatarSize
import dev.kindling.core.components.ui.badge.KBadge
import dev.kindling.core.components.ui.badge.KBadgeVariant
import dev.kindling.core.components.ui.card.KCard
import dev.kindling.core.components.ui.card.KCardContent
import dev.kindling.core.components.ui.card.KCardDescription
import dev.kindling.core.components.ui.card.KCardFooter
import dev.kindling.core.components.ui.card.KCardHeader
import dev.kindling.core.components.ui.card.KCardSize
import dev.kindling.core.components.ui.card.KCardTitle
import dev.kindling.core.components.ui.carousel.KCarouselContent
import dev.kindling.core.components.ui.carousel.KCarouselItem
import dev.kindling.core.components.ui.carousel.rememberCarouselApi
import dev.kindling.core.components.ui.layout.KCenteredBox
import dev.kindling.core.components.ui.maskInput.KMaskPattern
import dev.kindling.core.components.ui.maskInput.applyCurrencyMask
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
        BadgeSection()
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
                Text(
                    "16 : 9",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        SubLabel("1 / 1 (square)")
        KAspectRatio(ratio = 1f, modifier = Modifier.width(120.dp)) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer)) {
                Text(
                    "1 : 1",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        SubLabel("4 / 3")
        KAspectRatio(ratio = 4f / 3f) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiaryContainer)) {
                Text(
                    "4 : 3",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KAvatar(size = KAvatarSize.Sm) {
                KAvatarFallback(size = KAvatarSize.Sm, initials = "CN")
            }
            KAvatar(size = KAvatarSize.Default) {
                KAvatarFallback(initials = "CN")
            }
            KAvatar(size = KAvatarSize.Lg) {
                KAvatarFallback(size = KAvatarSize.Lg, initials = "CN")
            }
        }

        SubLabel("Fallback initials — multiple users")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KAvatar { KAvatarFallback(initials = "AB") }
            KAvatar { KAvatarFallback(initials = "MX") }
            KAvatar { KAvatarFallback(initials = "ZK") }
        }

        SubLabel("No painter — empty fallback box")
        KAvatar {
            KAvatarFallback()
        }

        SubLabel("With status badge")
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KAvatar(size = KAvatarSize.Sm) {
                KAvatarFallback(size = KAvatarSize.Sm, initials = "AB")
                KAvatarBadge(size = KAvatarSize.Sm)
            }
            KAvatar(size = KAvatarSize.Default) {
                KAvatarFallback(initials = "AB")
                KAvatarBadge(size = KAvatarSize.Default)
            }
            KAvatar(size = KAvatarSize.Lg) {
                KAvatarFallback(size = KAvatarSize.Lg, initials = "AB")
                KAvatarBadge(size = KAvatarSize.Lg)
            }
        }

        SubLabel("AvatarGroup — overlapping stack")
        KAvatarGroup {
            KAvatar { KAvatarFallback(initials = "CN") }
            KAvatar { KAvatarFallback(initials = "AB") }
            KAvatar { KAvatarFallback(initials = "MX") }
            KAvatarGroupCount(count = 4)
        }

        SubLabel("AvatarGroup — Lg size, custom overlap")
        KAvatarGroup(overlap = 12.dp) {
            KAvatar(size = KAvatarSize.Lg) { KAvatarFallback(size = KAvatarSize.Lg, initials = "AA") }
            KAvatar(size = KAvatarSize.Lg) { KAvatarFallback(size = KAvatarSize.Lg, initials = "BB") }
            KAvatar(size = KAvatarSize.Lg) { KAvatarFallback(size = KAvatarSize.Lg, initials = "CC") }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BadgeSection() {
    SectionHeader("Badge")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SubLabel("All variants")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KBadge(variant = KBadgeVariant.Default)     { Text("Default") }
            KBadge(variant = KBadgeVariant.Secondary)   { Text("Secondary") }
            KBadge(variant = KBadgeVariant.Destructive) { Text("Destructive") }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KBadge(variant = KBadgeVariant.Outline) { Text("Outline") }
            KBadge(variant = KBadgeVariant.Ghost)   { Text("Ghost") }
            KBadge(variant = KBadgeVariant.Link)    { Text("Link") }
        }

        SubLabel("With icon")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KBadge(variant = KBadgeVariant.Default) {
                Icon(Icons.Default.Star, null, modifier = Modifier.size(10.dp))
                Text("Featured")
            }
            KBadge(variant = KBadgeVariant.Destructive) {
                Text("Error")
            }
        }
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KButton(text = "Xs",      onClick = {}, size = KButtonSize.Xs)
            KButton(text = "Sm",      onClick = {}, size = KButtonSize.Sm)
            KButton(text = "Default", onClick = {})
            KButton(text = "Lg",      onClick = {}, size = KButtonSize.Lg)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KButton(onClick = {}, size = KButtonSize.IconXs)  { Icon(Icons.Default.Star, null, modifier = Modifier.size(12.dp)) }
            KButton(onClick = {}, size = KButtonSize.IconSm)  { Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp)) }
            KButton(onClick = {}, size = KButtonSize.Icon)    { Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp)) }
            KButton(onClick = {}, size = KButtonSize.IconLg)  { Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp)) }
        }

        SubLabel("States")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Enabled",  onClick = {})
            KButton(text = "Disabled", onClick = {}, enabled = false)
            KButton(text = "Loading",  onClick = {}, isLoading = true)
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
        Text(
            "Selected: ${singleDate ?: "none"}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SubLabel("Range selection")
        var range by remember { mutableStateOf(KDateRange()) }
        KCalendar(
            mode          = KCalendarMode.Range,
            selectedRange = range,
            onSelectRange = { range = it }
        )
        Text(
            "From: ${range.from ?: "—"}  To: ${range.to ?: "—"}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SubLabel("Dropdown caption layout")
        var dropDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(
            captionLayout  = KCalendarCaptionLayout.Dropdown,
            selected       = dropDate,
            onSelectSingle = { dropDate = it }
        )

        SubLabel("Two months side-by-side")
        var twoDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(numberOfMonths = 2, selected = twoDate, onSelectSingle = { twoDate = it })

        SubLabel("Show week numbers, hide outside days")
        var wkDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(
            showOutsideDays = false,
            showWeekNumber  = true,
            selected        = wkDate,
            onSelectSingle  = { wkDate = it }
        )

        SubLabel("With presets")
        var presetDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(
            selected       = presetDate,
            onSelectSingle = { presetDate = it },
            presets        = listOf(
                KCalendarPreset("Today",     LocalDate.now()),
                KCalendarPreset("Tomorrow",  LocalDate.now().plusDays(1)),
                KCalendarPreset("In a week", LocalDate.now().plusDays(7)),
            )
        )

        SubLabel("Min / Max date (today ± 7 days)")
        var minMaxDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(
            selected       = minMaxDate,
            onSelectSingle = { minMaxDate = it },
            minDate        = LocalDate.now().minusDays(7),
            maxDate        = LocalDate.now().plusDays(7)
        )

        SubLabel("Persian (Jalali) locale")
        var persianDate by remember { mutableStateOf<LocalDate?>(null) }
        KCalendar(
            calendarLocale = KCalendarLocale.Persian(),
            selected       = persianDate,
            onSelectSingle = { persianDate = it }
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

        SubLabel("Default — header + content + footer")
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

        SubLabel("Card with image slot")
        KCard(
            image = androidx.compose.ui.res.painterResource(
                // Replace with any drawable in the sample app; fallback placeholder shown
                id = android.R.drawable.ic_menu_gallery
            )
        ) {
            KCardHeader { KCardTitle("Photo Card") }
            KCardContent { Text("An image can appear above the card body.", fontSize = 12.sp) }
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
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

        SubLabel("Horizontal — arrows + dots")
        val api1 = rememberCarouselApi(pageCount = items.size)
        Column {
            KCarouselContent(api = api1) { page ->
                KCarouselItem {
                    Card(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        KCenteredBox { Text(items[page], style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                KCarouselPrevious(onClick = {}, enabled = api1.canScrollPrev)
                Text(
                    "${api1.currentSlide + 1} / ${api1.slideCount}",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontSize = 12.sp
                )
                KCarouselNext(onClick = {}, enabled = api1.canScrollNext)
            }
        }

        SubLabel("No arrows — content only")
        val api2 = rememberCarouselApi(pageCount = items.size)
        KCarouselContent(api = api2) { page ->
            KCarouselItem {
                Card(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                    KCenteredBox { Text(items[page]) }
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
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        SubLabel("Trigger + search — nothing selected")
        val state1 = rememberComboboxState(onSelect = {})
        Combobox() {
            ComboboxTrigger(state = state1)
            ComboboxContent(state = state1) {
                ComboboxList {
                    val filtered = if (state1.query.isBlank()) frameworks
                    else frameworks.filter { it.label.contains(state1.query, true) }
                    if (filtered.isEmpty()) {
                        ComboboxEmpty { Text("No results.") }
                    } else {
                        ComboboxCollection(state = state1, items = filtered)
                    }
                }
            }
        }

        SubLabel("Pre-selected item")
        val state2 = rememberComboboxState(selected = frameworks[1], onSelect = {})
        Combobox() {
            ComboboxTrigger(state = state2)
            ComboboxContent(state = state2) {
                ComboboxList {
                    ComboboxCollection(state = state2, items = frameworks)
                }
            }
        }

        SubLabel("Input with search field + clear button")
        val state3 = rememberComboboxState(onSelect = {})
        Combobox() {
            ComboboxInput(state = state3, placeholder = "Search frameworks…", showClear = true)
            ComboboxContent(state = state3) {
                ComboboxList {
                    val filtered = if (state3.query.isBlank()) frameworks
                    else frameworks.filter { it.label.contains(state3.query, true) }
                    if (filtered.isEmpty()) ComboboxEmpty { Text("No results.") }
                    else ComboboxCollection(state = state3, items = filtered)
                }
            }
        }

        SubLabel("Multiple selection (chips)")
        val state4 = rememberComboboxState(multiple = true, onSelectMultiple = {})
        Combobox() {
            ComboboxInput(state = state4, placeholder = "Select frameworks…")
            ComboboxContent(state = state4) {
                ComboboxList {
                    ComboboxCollection(state = state4, items = frameworks)
                }
            }
        }

        SubLabel("Grouped items")
        val groupedItems = listOf(
            KComboboxItem("next",    "Next.js",    group = "react"),
            KComboboxItem("remix",   "Remix",      group = "react"),
            KComboboxItem("nuxt",    "Nuxt.js",    group = "vue"),
            KComboboxItem("astro",   "Astro",      group = "agnostic"),
            KComboboxItem("svelte",  "SvelteKit",  group = "agnostic"),
        )
        val groups = listOf(
            KComboboxGroup("react",    "React-based"),
            KComboboxGroup("vue",      "Vue-based"),
            KComboboxGroup("agnostic", "Framework-agnostic"),
        )
        val state5 = rememberComboboxState(onSelect = {})
        Combobox() {
            ComboboxTrigger(state = state5)
            ComboboxContent(state = state5) {
                ComboboxList {
                    ComboboxCollection(state = state5, items = groupedItems, groups = groups)
                }
            }
        }

        SubLabel("Disabled trigger")
        val stateDisabled = rememberComboboxState(onSelect = {})
        Combobox() {
            ComboboxTrigger(state = stateDisabled, enabled = false)
        }
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
        Payment("INV002", "Pending", "PayPal",        150),
        Payment("INV003", "Unpaid",  "Bank Transfer", 350),
        Payment("INV004", "Paid",    "Credit Card",   450),
        Payment("INV005", "Paid",    "PayPal",         50),
    )
    val columns = listOf(
        KTableColumn<Payment>("id",     "Invoice", sortable = true) { Text(it.id,          fontSize = 13.sp) },
        KTableColumn<Payment>("status", "Status",  sortable = true) { Text(it.status,       fontSize = 13.sp) },
        KTableColumn<Payment>("method", "Method")                   { Text(it.method,       fontSize = 13.sp) },
        KTableColumn<Payment>("amount", "Amount",  sortable = true) { Text("$${it.amount}", fontSize = 13.sp) },
    )
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

        SubLabel("Sortable columns")
        KDataTable(columns = columns, data = payments, onSort = { _, _ -> })

        SubLabel("Striped rows")
        KDataTable(columns = columns, data = payments.take(3), striped = true)

        SubLabel("Paginated — page size 2")
        KDataTable(columns = columns, data = payments, pageSize = 2)

        SubLabel("Empty state")
        KDataTable(columns = columns, data = emptyList())

        SubLabel("Raw Table slots")
        Table {
            TableHeader {
                TableRow {
                    TableHead { Text("Name", fontSize = 13.sp) }
                    TableHead { Text("Role", fontSize = 13.sp) }
                    TableHead(align = TextAlign.End) { Text("Status", fontSize = 13.sp) }
                }
            }
            TableBody {
                listOf("Alice" to "Admin", "Bob" to "Editor", "Carol" to "Viewer")
                    .forEachIndexed { i, (name, role) ->
                        if (i > 0) HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(.5f),
                            thickness = 1.dp
                        )
                        TableRow {
                            TableCell { Text(name,  fontSize = 13.sp) }
                            TableCell { Text(role,  fontSize = 13.sp) }
                            TableCell(align = TextAlign.End) {
                                KBadge(variant = KBadgeVariant.Secondary) { Text("Active") }
                            }
                        }
                    }
            }
            TableFooter {
                TableRow {
                    TableCell { Text("3 members", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            TableCaption { Text("Team members overview") }
        }
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

        SubLabel("Min date — today")
        var date2 by remember { mutableStateOf<LocalDate?>(null) }
        KDatePicker(selected = date2, onSelect = { date2 = it }, minDate = LocalDate.now())

        SubLabel("Max date — end of year")
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        SubLabel("Basic dialog — free-form content")
        var open1 by remember { mutableStateOf(false) }
        KButton(text = "Open Dialog", onClick = { open1 = true })
        DialogContent(open = open1, onDismiss = { open1 = false }) {
            DialogHeader {
                DialogTitle("Edit Profile")
                DialogDescription("Make changes to your profile here. Click save when you're done.")
            }
            Spacer(Modifier.height(12.dp))
            var name by remember { mutableStateOf("") }
            KInput(value = name, onValueChange = { name = it }, placeholder = "Your name")
            DialogFooter(showCloseButton = true, onDismiss = { open1 = false }) {
                KButton(text = "Save changes", onClick = { open1 = false })
            }
        }

        SubLabel("Close button only (no footer)")
        var open2 by remember { mutableStateOf(false) }
        KButton(text = "Info Dialog", onClick = { open2 = true }, variant = KButtonVariant.Outline)
        DialogContent(open = open2, onDismiss = { open2 = false }, showCloseButton = true) {
            DialogHeader {
                DialogTitle("Information")
                DialogDescription("This dialog has a built-in close button in the top-right corner.")
            }
        }

        SubLabel("No close button — dismiss by overlay tap")
        var open3 by remember { mutableStateOf(false) }
        KButton(text = "No × Button", onClick = { open3 = true }, variant = KButtonVariant.Secondary)
        DialogContent(open = open3, onDismiss = { open3 = false }, showCloseButton = false) {
            DialogHeader {
                DialogTitle("Tap outside to dismiss")
                DialogDescription("There is no × button on this dialog.")
            }
            Spacer(Modifier.height(12.dp))
            KButton(text = "Dismiss", onClick = { open3 = false }, variant = KButtonVariant.Outline)
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

        SubLabel("LTR (default)")
        KDirectionProvider(direction = androidx.compose.ui.unit.LayoutDirection.Ltr) {
            KButton(text = "Left-to-right button", onClick = {})
        }

        SubLabel("RTL override")
        KDirectionProvider(direction = androidx.compose.ui.unit.LayoutDirection.Rtl) {
            KButton(text = "زر من اليمين إلى اليسار", onClick = {})
        }

        SubLabel("RTL — pagination arrows mirror")
        KDirectionProvider(direction = androidx.compose.ui.unit.LayoutDirection.Rtl) {
            Pagination {
                PaginationContent {
                    PaginationItem { PaginationPrevious(onClick = {}, text = "السابق") }
                    PaginationItem { PaginationLink(1, isActive = true,  onClick = {}) }
                    PaginationItem { PaginationLink(2, isActive = false, onClick = {}) }
                    PaginationItem { PaginationNext(onClick = {}, text = "التالي") }
                }
            }
        }

        SubLabel("Toggle via KDirectionManager")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(
                text    = "Set LTR",
                onClick = { KDirectionManager.set(androidx.compose.ui.unit.LayoutDirection.Ltr) },
                variant = KButtonVariant.Outline,
                size    = KButtonSize.Sm
            )
            KButton(
                text    = "Set RTL",
                onClick = { KDirectionManager.set(androidx.compose.ui.unit.LayoutDirection.Rtl) },
                variant = KButtonVariant.Outline,
                size    = KButtonSize.Sm
            )
            KButton(
                text    = "Toggle",
                onClick = { KDirectionManager.toggle() },
                size    = KButtonSize.Sm
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptySection() {
    SectionHeader("Empty / EmptyState")
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

        SubLabel("Basic — icon + title + description")
        KEmpty {
            KEmptyHeader {
                KEmptyMedia(variant = KEmptyMediaVariant.Icon) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                KEmptyTitle("No projects yet")
                KEmptyDescription("Create your first project to get started.")
            }
            KEmptyContent {
                KButton(text = "Create Project", onClick = {})
            }
        }

        SubLabel("Two actions")
        KEmpty {
            KEmptyHeader {
                KEmptyMedia(variant = KEmptyMediaVariant.Icon) {
                    Icon(Icons.Outlined.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxSize())
                }
                KEmptyTitle("Your inbox is empty")
                KEmptyDescription("Messages from your team will appear here.")
            }
            KEmptyContent {
                KButton(text = "Invite teammates", onClick = {})
                KButton(text = "Learn more", onClick = {}, variant = KButtonVariant.Outline)
            }
        }

        SubLabel("Outlined container")
        KEmpty(outlined = true) {
            KEmptyHeader {
                KEmptyMedia(variant = KEmptyMediaVariant.Icon) {
                    Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxSize())
                }
                KEmptyTitle("No files")
                KEmptyDescription("Upload a file to get started.")
            }
            KEmptyContent { KButton(text = "Upload", onClick = {}) }
        }

        SubLabel("Show background (muted fill)")
        KEmpty(showBackground = true) {
            KEmptyHeader {
                KEmptyMedia(variant = KEmptyMediaVariant.Icon) {
                    Icon(Icons.Outlined.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxSize())
                }
                KEmptyTitle("All caught up")
                KEmptyDescription("Nothing to see here.")
            }
        }

        SubLabel("Avatar variant media")
        KEmpty {
            KEmptyHeader {
                KEmptyMedia(variant = KEmptyMediaVariant.Avatar, size = 56.dp) {
                    KAvatar(size = KAvatarSize.Lg) {
                        KAvatarFallback(size = KAvatarSize.Lg, initials = "?")
                    }
                }
                KEmptyTitle("Unknown user")
                KEmptyDescription("This user's profile is not available.")
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

        SubLabel("Default placeholder")
        var t1 by remember { mutableStateOf("") }
        KInput(value = t1, onValueChange = { t1 = it }, placeholder = "Placeholder text")

        SubLabel("Pre-filled value")
        KInput(value = "Hello, world!", onValueChange = {})

        SubLabel("Password field")
        var pwd by remember { mutableStateOf("") }
        KInput(value = pwd, onValueChange = { pwd = it }, placeholder = "Password", isPassword = true)

        SubLabel("With leading icon")
        var t2 by remember { mutableStateOf("") }
        KInput(
            value         = t2,
            onValueChange = { t2 = it },
            placeholder   = "Search…",
            leadingIcon   = { Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp)) }
        )

        SubLabel("Error state")
        KInput(value = "bad-value", onValueChange = {}, isError = true)

        SubLabel("Disabled")
        KInput(value = "", onValueChange = {}, placeholder = "Disabled", enabled = false)

        SubLabel("Multi-line (singleLine = false, maxLines = 3)")
        var ta by remember { mutableStateOf("") }
        KInput(
            value         = ta,
            onValueChange = { ta = it },
            placeholder   = "Write something…",
            singleLine    = false,
            maxLines      = 3
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  InputGroup
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InputGroupSection() {
    SectionHeader("InputGroup")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        SubLabel("Leading text addon (@)")
        var ig1 by remember { mutableStateOf("") }
        InputGroup {
            InputGroupAddon(align = KInputGroupAlign.InlineStart) { InputGroupText("@") }
            InputGroupInput(value = ig1, onValueChange = { ig1 = it }, placeholder = "username")
        }

        SubLabel("Trailing icon button (clear)")
        var ig2 by remember { mutableStateOf("") }
        InputGroup {
            InputGroupInput(value = ig2, onValueChange = { ig2 = it }, placeholder = "Search…")
            InputGroupAddon(align = KInputGroupAlign.InlineEnd) {
                InputGroupButton(onClick = { ig2 = "" }) {
                    Icon(Icons.Default.Star, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                }
            }
        }

        SubLabel("Block-start label")
        var ig3 by remember { mutableStateOf("") }
        InputGroup {
            InputGroupAddon(align = KInputGroupAlign.BlockStart) { InputGroupText("Website URL") }
            InputGroupInput(value = ig3, onValueChange = { ig3 = it }, placeholder = "https://")
        }

        SubLabel("Block-end currency hint")
        var ig4 by remember { mutableStateOf("") }
        InputGroup {
            InputGroupInput(value = ig4, onValueChange = { ig4 = it }, placeholder = "Amount")
            InputGroupAddon(align = KInputGroupAlign.BlockEnd) { InputGroupText("USD") }
        }

        SubLabel("Error state")
        var ig5 by remember { mutableStateOf("bad") }
        InputGroup(isError = true) {
            InputGroupAddon(align = KInputGroupAlign.InlineStart) { InputGroupText("$") }
            InputGroupInput(value = ig5, onValueChange = { ig5 = it }, isError = true)
        }

        SubLabel("Textarea variant")
        var ig6 by remember { mutableStateOf("") }
        InputGroup {
            InputGroupTextarea(value = ig6, onValueChange = { ig6 = it }, placeholder = "Multi-line…")
        }

        SubLabel("Disabled")
        InputGroup() {
            InputGroupAddon(align = KInputGroupAlign.InlineStart) { InputGroupText("https://") }
            InputGroupInput(value = "example.com", onValueChange = {}, enabled = false)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  InputOTP
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InputOtpSection() {
    SectionHeader("InputOTP")
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        SubLabel("6-digit — two groups of 3 with separator")
        var otp1 by remember { mutableStateOf("") }
        val state1 = rememberInputOTPState(value = otp1, length = 6) { otp1 = it }
        InputOTP(state = state1) {
            InputOTPGroup {
                repeat(3) { i -> InputOTPSlot(state1, i, isFirst = i == 0, isLast = i == 2) }
            }
            InputOTPSeparator()
            InputOTPGroup {
                repeat(3) { i -> InputOTPSlot(state1, i + 3, isFirst = i == 0, isLast = i == 2) }
            }
        }
        Text("Value: $otp1", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SubLabel("4-digit PIN — single group")
        var otp2 by remember { mutableStateOf("") }
        val state2 = rememberInputOTPState(value = otp2, length = 4) { otp2 = it }
        InputOTP(state = state2) {
            InputOTPGroup {
                repeat(4) { i -> InputOTPSlot(state2, i, isFirst = i == 0, isLast = i == 3) }
            }
        }

        SubLabel("6-digit flat (no separator, all in one group)")
        var otp3 by remember { mutableStateOf("") }
        val state3 = rememberInputOTPState(value = otp3, length = 6) { otp3 = it }
        InputOTP(state = state3) {
            InputOTPGroup {
                repeat(6) { i -> InputOTPSlot(state3, i, isFirst = i == 0, isLast = i == 5) }
            }
        }
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

        SubLabel("Disabled — 38% opacity")
        KLabel("Disabled label", disabled = true)

        SubLabel("Paired with Checkbox")
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var checked by remember { mutableStateOf(false) }
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            KLabel("Accept terms and conditions")
        }

        SubLabel("Custom style override")
        KLabel(
            "Headline label",
            style = MaterialTheme.typography.headlineSmall
        )
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
        MaskInput(value = phone, onValueChange = { phone = it }, mask = KMaskPattern.Phone)

        SubLabel("SSN — ###-##-####")
        var ssn by remember { mutableStateOf("") }
        MaskInput(value = ssn, onValueChange = { ssn = it }, mask = KMaskPattern.Ssn)

        SubLabel("Date — MM/DD/YYYY")
        var date by remember { mutableStateOf("") }
        MaskInput(value = date, onValueChange = { date = it }, mask = KMaskPattern.Date)

        SubLabel("Credit card — #### #### #### ####")
        var cc by remember { mutableStateOf("") }
        MaskInput(value = cc, onValueChange = { cc = it }, mask = KMaskPattern.CreditCard)

        SubLabel("Credit card expiry — MM/YY")
        var expiry by remember { mutableStateOf("") }
        MaskInput(value = expiry, onValueChange = { expiry = it }, mask = KMaskPattern.CreditCardExpiry)

        SubLabel("EIN — ##-#######")
        var ein by remember { mutableStateOf("") }
        MaskInput(value = ein, onValueChange = { ein = it }, mask = KMaskPattern.Ein)

        SubLabel("Zip code — #####")
        var zip by remember { mutableStateOf("") }
        MaskInput(value = zip, onValueChange = { zip = it }, mask = KMaskPattern.ZipCode)

        SubLabel("ISBN — ###-#-###-#####-#")
        var isbn by remember { mutableStateOf("") }
        MaskInput(value = isbn, onValueChange = { isbn = it }, mask = KMaskPattern.Isbn)

        SubLabel("License plate (alphanumeric) — ###-###")
        var plate by remember { mutableStateOf("") }
        MaskInput(value = plate, onValueChange = { plate = it }, mask = KMaskPattern.LicensePlate, allowLetters = true)

        SubLabel("MAC address — ##:##:##:##:##:##")
        var mac by remember { mutableStateOf("") }
        MaskInput(value = mac, onValueChange = { mac = it }, mask = KMaskPattern.MacAddress, allowLetters = true)

        SubLabel("Custom pattern — ##:##:##")
        var custom by remember { mutableStateOf("") }
        MaskInput(value = custom, onValueChange = { custom = it }, customPattern = "##:##:##", placeholder = "HH:MM:SS")

        SubLabel("Currency mask (applyCurrencyMask helper)")
        var rawAmt by remember { mutableStateOf("") }
        KInput(
            value         = rawAmt,
            onValueChange = { rawAmt = it },
            placeholder   = "Enter amount",
            trailingIcon  = {
                Text(
                    applyCurrencyMask(rawAmt.filter { it.isDigit() }.ifEmpty { "0" }),
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        )

        SubLabel("Error state")
        var errPhone by remember { mutableStateOf("123") }
        MaskInput(value = errPhone, onValueChange = { errPhone = it }, mask = KMaskPattern.Phone, isError = true)

        SubLabel("Disabled")
        MaskInput(value = "(555) 123-4567", onValueChange = {}, mask = KMaskPattern.Phone, enabled = false)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Pagination
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PaginationSection() {
    SectionHeader("Pagination")
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        SubLabel("5 pages — no ellipsis needed")
        var page5 by remember { mutableStateOf(1) }
        Pagination {
            PaginationContent {
                PaginationItem {
                    PaginationPrevious(onClick = { if (page5 > 1) page5-- }, enabled = page5 > 1)
                }
                (1..5).forEach { p ->
                    PaginationItem {
                        PaginationLink(p, isActive = p == page5, onClick = { page5 = p })
                    }
                }
                PaginationItem {
                    PaginationNext(onClick = { if (page5 < 5) page5++ }, enabled = page5 < 5)
                }
            }
        }

        SubLabel("20 pages — with ellipsis")
        var page20 by remember { mutableStateOf(1) }
        Pagination {
            PaginationContent {
                PaginationItem {
                    PaginationPrevious(onClick = { if (page20 > 1) page20-- }, enabled = page20 > 1, text = "Prev")
                }
                if (page20 > 2) {
                    PaginationItem { PaginationLink(1, isActive = false, onClick = { page20 = 1 }) }
                    if (page20 > 3) PaginationItem { PaginationEllipsis() }
                }
                val range = maxOf(1, page20 - 1)..minOf(20, page20 + 1)
                range.forEach { p ->
                    PaginationItem { PaginationLink(p, isActive = p == page20, onClick = { page20 = p }) }
                }
                if (page20 < 19) {
                    if (page20 < 18) PaginationItem { PaginationEllipsis() }
                    PaginationItem { PaginationLink(20, isActive = false, onClick = { page20 = 20 }) }
                }
                PaginationItem {
                    PaginationNext(onClick = { if (page20 < 20) page20++ }, enabled = page20 < 20, text = "Next")
                }
            }
        }
        Text(
            "Current page: $page20",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            var dim by remember { mutableStateOf("100%") }
            KInput(value = dim, onValueChange = { dim = it }, placeholder = "Width")
        }

        SubLabel("Top / Start")
        var open2 by remember { mutableStateOf(false) }
        KPopover(
            open      = open2,
            onDismiss = { open2 = false },
            side      = KPopoverSide.Top,
            align     = KPopoverAlign.Start,
            trigger   = {
                KButton(
                    text    = "Top-Start Popover",
                    onClick = { open2 = !open2 },
                    variant = KButtonVariant.Outline
                )
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
                KButton(
                    text    = "Right Popover",
                    onClick = { open3 = !open3 },
                    variant = KButtonVariant.Secondary
                )
            }
        ) {
            KPopoverDescription("Appears to the right of the button.")
        }

        SubLabel("Bottom-sheet overlay")
        var open4 by remember { mutableStateOf(false) }
        KPopover(
            open        = open4,
            onDismiss   = { open4 = false },
            overlayZone = KPopoverOverlayPosition.BottomSheet,
            trigger     = { KButton(text = "Open Sheet", onClick = { open4 = !open4 }, variant = KButtonVariant.Outline) }
        ) {
            KPopoverTitle("Bottom sheet")
            KPopoverDescription("This panel slides up from the bottom of the screen.")
            Spacer(Modifier.height(8.dp))
            KButton(text = "Dismiss", onClick = { open4 = false }, variant = KButtonVariant.Ghost)
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
        Skeleton(modifier = Modifier.fillMaxWidth().height(20.dp))
        Skeleton(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp))
        Skeleton(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))

        SubLabel("Avatar + text")
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Skeleton(modifier = Modifier.size(48.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Skeleton(modifier = Modifier.width(120.dp).height(14.dp))
                Skeleton(modifier = Modifier.width(80.dp).height(12.dp))
            }
        }

        SubLabel("Card placeholder")
        Skeleton(modifier = Modifier.fillMaxWidth().height(100.dp))

        SubLabel("Custom shape — pill")
        Skeleton(
            modifier = Modifier.width(80.dp).height(24.dp),
        )

        SubLabel("Shimmer brush directly")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(kindlingShimmerBrush())
        )
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spinner(size = KSpinnerSize.Sm)
            Spinner(size = KSpinnerSize.Default)
            Spinner(size = KSpinnerSize.Lg)
            Spinner(size = KSpinnerSize.Xl)
        }

        SubLabel("Custom colours")
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spinner(color = MaterialTheme.colorScheme.secondary)
            Spinner(color = MaterialTheme.colorScheme.tertiary)
            Spinner(color = Color(0xFFEF4444))
            Spinner(
                color      = Color(0xFF6366F1),
                trackColor = Color(0xFF6366F1).copy(alpha = 0.2f)
            )
        }

        SubLabel("Inside a loading card")
        KCard {
            KCardContent(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spinner(size = KSpinnerSize.Default)
                    Text("Fetching data…", fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Stepper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepperSection() {
    SectionHeader("Stepper")
    val stepValues = listOf("account", "billing", "review", "done")
    val stepLabels = listOf("Account", "Billing", "Review", "Done")

    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {

        SubLabel("Step 0 — first active")
        StepperDemo(stepValues = stepValues, stepLabels = stepLabels, initial = "account")

        SubLabel("Step 2 — middle active")
        StepperDemo(stepValues = stepValues, stepLabels = stepLabels, initial = "review")

        SubLabel("All completed (past last)")
        Stepper(state = rememberStepperState(steps = stepValues, defaultValue = "done")) {
            StepperList {
                stepValues.forEachIndexed { i, v ->
                    StepperItem(value = v) {
                        StepperTrigger {
                            StepperIndicator()
                            StepperTitle { Text(stepLabels[i]) }
                        }
                        if (i < stepValues.lastIndex) StepperSeparator()
                    }
                }
            }
        }

        SubLabel("Interactive — nav buttons")
        val state = rememberStepperState(steps = stepValues)
        Stepper(state = state) {
            StepperList {
                stepValues.forEachIndexed { i, v ->
                    StepperItem(value = v) {
                        StepperTrigger {
                            StepperIndicator()
                            Column {
                                StepperTitle { Text(stepLabels[i]) }
                                StepperDescription { Text("Step ${i + 1} of ${stepValues.size}") }
                            }
                        }
                        if (i < stepValues.lastIndex) StepperSeparator()
                    }
                }
            }
            StepperContent(value = "account") {
                Text("Account step content", fontSize = 13.sp)
            }
            StepperContent(value = "billing") {
                Text("Billing step content", fontSize = 13.sp)
            }
            StepperContent(value = "review") {
                Text("Review step content", fontSize = 13.sp)
            }
            StepperContent(value = "done") {
                Text("🎉 All done!", fontSize = 13.sp)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                StepperPrev { onClick ->
                    KButton(
                        text    = "Back",
                        onClick = onClick,
                        variant = KButtonVariant.Outline,
                        size    = KButtonSize.Sm,
                        enabled = state.canGoPrev
                    )
                }
                StepperNext { onClick ->
                    KButton(
                        text    = "Next",
                        onClick = onClick,
                        size    = KButtonSize.Sm,
                        enabled = state.canGoNext
                    )
                }
            }
        }

        SubLabel("Error state on a step")
        val errorState = rememberStepperState(steps = stepValues, defaultValue = "billing")
        Stepper(state = errorState) {
            StepperList {
                stepValues.forEachIndexed { i, v ->
                    StepperItem(value = v, completed = if (v == "billing") false else null) {
                        StepperTrigger {
                            StepperIndicator(content = if (v == "billing") {
                                { _ ->
                                    Icon(
                                        Icons.Default.Star,
                                        null,
                                        tint     = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null)
                            StepperTitle { Text(stepLabels[i]) }
                        }
                        if (i < stepValues.lastIndex) StepperSeparator()
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperDemo(
    stepValues: List<String>,
    stepLabels: List<String>,
    initial: String
) {
    Stepper(state = rememberStepperState(steps = stepValues, defaultValue = initial)) {
        StepperList {
            stepValues.forEachIndexed { i, v ->
                StepperItem(value = v) {
                    StepperTrigger {
                        StepperIndicator()
                        StepperTitle { Text(stepLabels[i]) }
                    }
                    if (i < stepValues.lastIndex) StepperSeparator()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Textarea
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TextareaSection() {
    SectionHeader("Textarea")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        SubLabel("Default (auto-grow, minLines = 2)")
        var ta1 by remember { mutableStateOf("") }
        Textarea(value = ta1, onValueChange = { ta1 = it }, placeholder = "Type your message here.")

        SubLabel("Pre-filled content")
        Textarea(
            value         = "Pre-filled content that spans multiple lines to show the auto-sizing behaviour.",
            onValueChange = {}
        )

        SubLabel("Error state")
        Textarea(value = "Invalid input", onValueChange = {}, isError = true)

        SubLabel("Disabled")
        Textarea(value = "", onValueChange = {}, placeholder = "You cannot edit this.", enabled = false)

        SubLabel("Custom minLines = 4")
        var ta2 by remember { mutableStateOf("") }
        Textarea(value = ta2, onValueChange = { ta2 = it }, placeholder = "Starts at 4 lines tall.", minLines = 4)

        SubLabel("maxLines = 3 (scrolls after 3 lines)")
        var ta3 by remember { mutableStateOf("") }
        Textarea(value = ta3, onValueChange = { ta3 = it }, placeholder = "Capped at 3 visible lines.", maxLines = 3)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Toaster
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ToasterSection() {
    SectionHeader("Toaster")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        SubLabel("Trigger toasts — KToastManager dispatches globally")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(
                text    = "Default",
                onClick = { KToastManager.show("Event created.") },
                variant = KButtonVariant.Outline
            )
            KButton(
                text    = "Success",
                onClick = { KToastManager.success("Saved!", "Your changes have been saved.") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(
                text    = "Error",
                onClick = { KToastManager.error("Uh oh!", "Something went wrong.") },
                variant = KButtonVariant.Destructive
            )
            KButton(
                text    = "Warning",
                onClick = { KToastManager.warning("Low disk space.") },
                variant = KButtonVariant.Secondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(
                text    = "Info",
                onClick = { KToastManager.info("New version available.") },
                variant = KButtonVariant.Outline
            )
            KButton(
                text    = "With action",
                onClick = {
                    KToastManager.show(
                        message     = "Event deleted.",
                        actionLabel = "Undo",
                        onAction    = { KToastManager.info("Undo successful.") }
                    )
                },
                variant = KButtonVariant.Ghost
            )
        }

        SubLabel("Static toast card (preview)")
        Toast(
            data    = KToastData(message = "Profile updated", type = KToastType.Success),
            onClose = {}
        )
        Toast(
            data    = KToastData(
                message     = "File deleted",
                description = "You can undo this action.",
                type        = KToastType.Default,
                actionLabel = "Undo",
                onAction    = {}
            ),
            onClose = {}
        )
    }
}