package dev.kindling.core.components

/**
 * Button visual style.
 */
enum class KButtonVariant { Default, Destructive, Outline, Secondary, Ghost, Link }

/**
 * Button sizing preset.
 */
enum class KButtonSize { Default, Sm, Lg, Icon }

/**
 * Spinner sizing preset.
 */
enum class KSpinnerSize { Sm, Default, Lg, Xl }

/**
 * Step progress state.
 */
enum class KStepState { Upcoming, Current, Completed, Error }

/**
 * Stepper orientation.
 */
enum class KStepperOrientation { Horizontal, Vertical }

/**
 * Carousel orientation.
 */
enum class KCarouselOrientation { Horizontal, Vertical }

/**
 * Sort direction used by table headers.
 */
enum class KSortDirection { Asc, Desc, None }

/**
 * Empty-state media variant.
 */
enum class KEmptyMediaVariant { Icon, Image, Avatar }

/**
 * Toast type.
 */
enum class KToastType { Default, Success, Error, Warning, Info }

/**
 * Card size preset — matches the `size` data attribute on the shadcn/ui Card.
 */
enum class KCardSize { Default, Sm }

/**
 * Select / combobox trigger size — matches the `size` prop on SelectTrigger.
 */
enum class KSelectSize { Default, Sm }

/**
 * Avatar size presets — `sm` = 24 dp, `default` = 32 dp, `lg` = 40 dp.
 * Declared here for public API symmetry; impl lives in Avatar.kt.
 */
// KAvatarSize is declared in Avatar.kt

/**
 * Toast payload.
 *
 * @property id Unique identifier.
 * @property message Main toast message.
 * @property description Optional secondary text.
 * @property type Semantic toast style.
 * @property actionLabel Optional action label.
 * @property onAction Optional action callback.
 * @property durationMs Auto-dismiss delay in milliseconds.
 */
data class KToastData(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val description: String? = null,
    val type: KToastType = KToastType.Default,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val durationMs: Long = 4_000L
)

/**
 * Combobox item model.
 *
 * @property value Stable item value.
 * @property label Human-readable label.
 */
data class KComboboxItem(val value: String, val label: String)