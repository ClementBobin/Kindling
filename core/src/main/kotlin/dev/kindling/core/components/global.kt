package dev.kindling.core.components

/**
 * Define the visual style of a Kindling button.
 *
 * These options mirror the shadcn/ui button variants.
 */
enum class KButtonVariant {
    Default,
    Destructive,
    Outline,
    Secondary,
    Ghost,
    Link
}

/**
 * Define the sizing preset applied to a Kindling button.
 */
enum class KButtonSize {
    Default,
    Sm,
    Lg,
    Icon
}

/**
 * Represent a selectable combobox option.
 *
 * @property value Stable identifier for the option.
 * @property label Display label shown to the user.
 */
data class KComboboxItem(
    val value: String,
    val label: String
)

/**
 * Define the visual state of a step in a stepper.
 */
enum class KStepState {
    Upcoming,
    Current,
    Completed,
    Error
}

/**
 * Define the layout orientation of a stepper.
 */
enum class KStepperOrientation {
    Horizontal,
    Vertical
}

/**
 * Define the scroll orientation of a carousel.
 */
enum class KCarouselOrientation {
    Horizontal,
    Vertical
}

/**
 * Define the sorting direction applied to a column.
 */
enum class KSortDirection {
    Asc,
    Desc,
    None
}

/**
 * Define the sizing preset applied to a spinner.
 */
enum class KSpinnerSize {
    Sm,
    Default,
    Lg,
    Xl
}

/**
 * Define the visual variant of empty state media.
 */
enum class KEmptyMediaVariant {
    Icon,
    Image,
    Avatar
}
