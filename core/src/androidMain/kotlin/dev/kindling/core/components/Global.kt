package dev.kindling.core.components

// ─────────────────────────────────────────────
//  Button
// ─────────────────────────────────────────────

enum class KButtonVariant { Default, Destructive, Outline, Secondary, Ghost, Link }
enum class KButtonSize    { Default, Xs, Sm, Lg, Icon, IconXs, IconSm, IconLg }

// ─────────────────────────────────────────────
//  Badge
// ─────────────────────────────────────────────

enum class KBadgeVariant { Default, Secondary, Destructive, Outline, Ghost, Link }

// ─────────────────────────────────────────────
//  Avatar
// ─────────────────────────────────────────────

enum class KAvatarSize { Sm, Default, Lg }

// ─────────────────────────────────────────────
//  Spinner
// ─────────────────────────────────────────────

enum class KSpinnerSize { Sm, Default, Lg, Xl }

// ─────────────────────────────────────────────
//  Stepper
// ─────────────────────────────────────────────

enum class KStepState       { Inactive, Active, Completed, Error }
enum class KStepperOrientation { Horizontal, Vertical }
enum class KActivationMode  { Automatic, Manual }
enum class KNavigationDirection { Next, Prev }

// ─────────────────────────────────────────────
//  Sort (DataTable)
// ─────────────────────────────────────────────

enum class KSortDirection { Asc, Desc, None }

// ─────────────────────────────────────────────
//  Toast
// ─────────────────────────────────────────────

enum class KToastType { Default, Success, Error, Warning, Info }

data class KToastData(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val description: String? = null,
    val type: KToastType = KToastType.Default,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val durationMs: Long = 4_000L
)

// ─────────────────────────────────────────────
//  Empty state
// ─────────────────────────────────────────────

enum class KEmptyMediaVariant { Icon, Image, Avatar }

// ─────────────────────────────────────────────
//  Calendar
// ─────────────────────────────────────────────

enum class KCalendarCaptionLayout { Label, Dropdown }

sealed interface KCalendarMode {
    object Single : KCalendarMode
    object Range  : KCalendarMode
}

data class KDateRange(
    val from: java.time.LocalDate? = null,
    val to:   java.time.LocalDate? = null
)

// ─────────────────────────────────────────────
//  Direction (RTL/LTR)
// ─────────────────────────────────────────────

enum class KLayoutDirection { Ltr, Rtl }