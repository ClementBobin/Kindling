package androidx.compose.ui.tooling.preview

/**
 * Mark a composable for design-time preview rendering.
 *
 * @param name Display name shown in preview tooling.
 * @param group Group name shown in preview tooling.
 * @param showBackground When `true`, draws a background behind the preview.
 * @param backgroundColor Background colour used when `showBackground` is enabled.
 * @param widthDp Width of the preview in dp.
 * @param heightDp Height of the preview in dp.
 * @param locale Locale tag used for preview rendering.
 * @param fontScale Font scale applied to the preview.
 * @param uiMode UI mode flags such as night mode.
 * @param showSystemUi When `true`, shows the system UI in the preview.
 * @param device Device configuration string for tooling.
 * @param apiLevel Android API level used for tooling.
 */
@Repeatable
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Preview(
    val name: String = "",
    val group: String = "",
    val showBackground: Boolean = false,
    val backgroundColor: Long = 0,
    val widthDp: Int = -1,
    val heightDp: Int = -1,
    val locale: String = "",
    val fontScale: Float = 1f,
    val uiMode: Int = 0,
    val showSystemUi: Boolean = false,
    val device: String = "",
    val apiLevel: Int = -1
)
