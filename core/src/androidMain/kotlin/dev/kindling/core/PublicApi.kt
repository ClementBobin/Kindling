package dev.kindling.core

import dev.kindling.core.components.KAvatarData
import dev.kindling.core.components.KAvatarSize
import dev.kindling.core.components.KButtonSize
import dev.kindling.core.components.KButtonVariant
import dev.kindling.core.components.KCalendarCaptionLayout
import dev.kindling.core.components.KCalendarLocale
import dev.kindling.core.components.KCalendarMode
import dev.kindling.core.components.KCalendarPreset
import dev.kindling.core.components.KCalendarTimeRange
import dev.kindling.core.components.KCardSize
import dev.kindling.core.components.KCarouselAutoPlay
import dev.kindling.core.components.KCarouselOrientation
import dev.kindling.core.components.KComboboxGroup
import dev.kindling.core.components.KComboboxItem
import dev.kindling.core.components.KDateRange
import dev.kindling.core.components.KEmptyMediaVariant
import dev.kindling.core.components.KInputGroupAlign
import dev.kindling.core.components.KMaskPattern
import dev.kindling.core.components.KPopoverAlign
import dev.kindling.core.components.KPopoverOverlayPosition
import dev.kindling.core.components.KPopoverSide
import dev.kindling.core.components.KSelectSize
import dev.kindling.core.components.KSortDirection
import dev.kindling.core.components.KSpinnerSize
import dev.kindling.core.components.KStep
import dev.kindling.core.components.KStepState
import dev.kindling.core.components.KStepperOrientation
import dev.kindling.core.components.KTableColumn
import dev.kindling.core.components.KToastData
import dev.kindling.core.components.KToastType

/**
 * Public API entrypoint package for Kindling core module.
 *
 * Every component type is re-exported via a `typealias` so callers
 * can import from this single surface rather than from internal packages.
 */
object KindlingCoreApi

// ── Button ────────────────────────────────────────────────────────────────────

/** @see KButtonSize */
typealias ButtonSize = KButtonSize

/** @see KButtonVariant */
typealias ButtonVariant = KButtonVariant

// ── Avatar ────────────────────────────────────────────────────────────────────

/** @see KAvatarSize */
typealias AvatarSize = KAvatarSize

/** @see KAvatarData */
typealias AvatarData = KAvatarData

// ── Calendar ──────────────────────────────────────────────────────────────────

/** @see KCalendarMode */
typealias CalendarMode = KCalendarMode

/** @see KCalendarCaptionLayout */
typealias CalendarCaptionLayout = KCalendarCaptionLayout

/** @see KCalendarLocale */
typealias CalendarLocale = KCalendarLocale

/** @see KCalendarPreset */
typealias CalendarPreset = KCalendarPreset

/** @see KCalendarTimeRange */
typealias CalendarTimeRange = KCalendarTimeRange

/** @see KDateRange */
typealias DateRange = KDateRange

// ── Card ──────────────────────────────────────────────────────────────────────

/** @see KCardSize */
typealias CardSize = KCardSize

// ── Carousel ──────────────────────────────────────────────────────────────────

/** @see KCarouselOrientation */
typealias CarouselOrientation = KCarouselOrientation

/** @see KCarouselAutoPlay */
typealias CarouselAutoPlay = KCarouselAutoPlay

// ── Combobox ──────────────────────────────────────────────────────────────────

/** @see KComboboxItem */
typealias ComboboxItem = KComboboxItem

/** @see KComboboxGroup */
typealias ComboboxGroup = KComboboxGroup

// ── DataTable ─────────────────────────────────────────────────────────────────

/** @see KTableColumn */
typealias TableColumn<T> = KTableColumn<T>

/** @see KSortDirection */
typealias SortDirection = KSortDirection

// ── Empty state ───────────────────────────────────────────────────────────────

/** @see KEmptyMediaVariant */
typealias EmptyMediaVariant = KEmptyMediaVariant

// ── InputGroup ────────────────────────────────────────────────────────────────

/** @see KInputGroupAlign */
typealias InputGroupAlign = KInputGroupAlign

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

// ── Select ────────────────────────────────────────────────────────────────────

/** @see KSelectSize */
typealias SelectSize = KSelectSize

// ── Spinner ───────────────────────────────────────────────────────────────────

/** @see KSpinnerSize */
typealias SpinnerSize = KSpinnerSize

// ── Stepper ───────────────────────────────────────────────────────────────────

/** @see KStep */
typealias Step = KStep

/** @see KStepState */
typealias StepState = KStepState

/** @see KStepperOrientation */
typealias StepperOrientation = KStepperOrientation

// ── Toast ─────────────────────────────────────────────────────────────────────

/** @see KToastData */
typealias ToastData = KToastData

/** @see KToastType */
typealias ToastType = KToastType