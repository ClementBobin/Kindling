package dev.kindling.core

import dev.kindling.core.components.*

/**
 * Public API entrypoint package for Kindling core module.
 *
 * Every component type and state class is re-exported via a `typealias` so
 * callers can import from this single surface rather than from internal packages.
 */
object KindlingCoreApi

// ── Button ────────────────────────────────────────────────────────────────────

/** @see KButtonSize */
typealias ButtonSize = KButtonSize

/** @see KButtonVariant */
typealias ButtonVariant = KButtonVariant

// ── Badge ─────────────────────────────────────────────────────────────────────

/** @see KBadgeVariant */
typealias BadgeVariant = KBadgeVariant

// ── Avatar ────────────────────────────────────────────────────────────────────

/** @see KAvatarSize */
typealias AvatarSize = KAvatarSize

// ── Calendar ──────────────────────────────────────────────────────────────────

/** @see KCalendarMode */
typealias CalendarMode = KCalendarMode

/** @see KCalendarCaptionLayout */
typealias CalendarCaptionLayout = KCalendarCaptionLayout

/** @see KCalendarLocale */
typealias CalendarLocale = KCalendarLocale

/** @see KCalendarPreset */
typealias CalendarPreset = KCalendarPreset

/** @see KCalendarDayContent */
typealias CalendarDayContent = KCalendarDayContent

/** @see KDateRange */
typealias DateRange = KDateRange

/** @see CalMonth */
typealias CalendarMonth = CalMonth

// ── Card ──────────────────────────────────────────────────────────────────────

/** @see KCardSize */
typealias CardSize = KCardSize

// ── Carousel ──────────────────────────────────────────────────────────────────

/** @see KCarouselAutoPlay */
typealias CarouselAutoPlay = KCarouselAutoPlay

/** @see CarouselApi */
typealias KCarouselApi = CarouselApi

// ── Combobox ──────────────────────────────────────────────────────────────────

/** @see KComboboxItem */
typealias ComboboxItem = KComboboxItem

/** @see KComboboxGroup */
typealias ComboboxGroup = KComboboxGroup

/** @see ComboboxState */
typealias KComboboxState = ComboboxState

// ── DataTable ─────────────────────────────────────────────────────────────────

/** @see KTableColumn */
typealias TableColumn<T> = KTableColumn<T>

/** @see KSortDirection */
typealias SortDirection = KSortDirection

// ── Dialog ────────────────────────────────────────────────────────────────────

/** @see DialogScope */
typealias KDialogScope = DialogScope

// ── Direction ────────────────────────────────────────────────────────────────

/** @see KLayoutDirection */
typealias LayoutDirectionKind = KLayoutDirection

// ── Empty state ───────────────────────────────────────────────────────────────

/** @see KEmptyMediaVariant */
typealias EmptyMediaVariant = KEmptyMediaVariant

// ── InputGroup ────────────────────────────────────────────────────────────────

/** @see KInputGroupAlign */
typealias InputGroupAlign = KInputGroupAlign

// ── InputOTP ──────────────────────────────────────────────────────────────────

/** @see InputOTPState */
typealias KInputOTPState = InputOTPState

/** @see InputOTPSlotState */
typealias KInputOTPSlotState = InputOTPSlotState

// ── MaskInput ─────────────────────────────────────────────────────────────────

/** @see KMaskPattern */
typealias MaskPattern = KMaskPattern

// ── Popover ───────────────────────────────────────────────────────────────────

/** @see KPopoverSide */
typealias PopoverSide = KPopoverSide

/** @see KPopoverAlign */
typealias PopoverAlign = KPopoverAlign

/** @see KPopoverOverlayPosition */
typealias PopoverOverlayPosition = KPopoverOverlayPosition

// ── Spinner ───────────────────────────────────────────────────────────────────

/** @see KSpinnerSize */
typealias SpinnerSize = KSpinnerSize

/** @see KStepState */
typealias StepState = KStepState

/** @see KStepperOrientation */
typealias StepperOrientation = KStepperOrientation

/** @see KNavigationDirection */
typealias NavigationDirection = KNavigationDirection

/** @see KActivationMode */
typealias ActivationMode = KActivationMode

/** @see StepperState */
typealias KStepperState = StepperState

// ── Toast ─────────────────────────────────────────────────────────────────────

/** @see KToastData */
typealias ToastData = KToastData

/** @see KToastType */
typealias ToastType = KToastType