package dev.kindling.core

import dev.kindling.core.components.KAvatarData
import dev.kindling.core.components.KAvatarSize
import dev.kindling.core.components.KButtonSize
import dev.kindling.core.components.KButtonVariant
import dev.kindling.core.components.KCalendarMode
import dev.kindling.core.components.KCardSize
import dev.kindling.core.components.KCarouselOrientation
import dev.kindling.core.components.KComboboxItem
import dev.kindling.core.components.KDateRange
import dev.kindling.core.components.KInputGroupAlign
import dev.kindling.core.components.KMaskPattern
import dev.kindling.core.components.KPopoverAlign
import dev.kindling.core.components.KPopoverSide
import dev.kindling.core.components.KSelectSize
import dev.kindling.core.components.KSortDirection
import dev.kindling.core.components.KSpinnerSize
import dev.kindling.core.components.KStep
import dev.kindling.core.components.KStepState
import dev.kindling.core.components.KStepperOrientation
import dev.kindling.core.components.KToastData
import dev.kindling.core.components.KToastType

/**
 * Public API entrypoint package for Kindling core module.
 *
 * Every component type is re-exported via a `typealias` so callers
 * can import from this single surface rather than from internal packages.
 */
object KindlingCoreApi

// ── Existing types ────────────────────────────────────────────────────────────

typealias ButtonSize         = KButtonSize
typealias ButtonVariant      = KButtonVariant
typealias CarouselOrientation = KCarouselOrientation
typealias ComboboxItem       = KComboboxItem
typealias SortDirection      = KSortDirection
typealias SpinnerSize        = KSpinnerSize
typealias Step               = KStep
typealias StepState          = KStepState
typealias StepperOrientation = KStepperOrientation
typealias ToastData          = KToastData
typealias ToastType          = KToastType

// ── New types (added with the full component set) ─────────────────────────────

/** @see KAvatarSize */
typealias AvatarSize         = KAvatarSize

/** @see KAvatarData */
typealias AvatarData         = KAvatarData

/** @see KCalendarMode */
typealias CalendarMode       = KCalendarMode

/** @see KDateRange */
typealias DateRange          = KDateRange

/** @see KCardSize */
typealias CardSize           = KCardSize

/** @see KInputGroupAlign */
typealias InputGroupAlign    = KInputGroupAlign

/** @see KMaskPattern */
typealias MaskPattern        = KMaskPattern

/** @see KPopoverSide */
typealias PopoverSide        = KPopoverSide

/** @see KPopoverAlign */
typealias PopoverAlign       = KPopoverAlign

/** @see KSelectSize */
typealias SelectSize         = KSelectSize