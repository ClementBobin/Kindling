package dev.kindling.core.components.ui.stepper

/**
 * Navigation direction used during step transitions and validation.
 */
enum class KNavigationDirection {
    Prev,
    Next
}

/**
 * Represents the current visual state of an individual step indicator.
 */
enum class KStepState {
    Inactive,
    Active,
    Completed,
    Error
}

/**
 * Orientation for the stepper component layout.
 */
enum class KStepperOrientation {
    Horizontal,
    Vertical
}