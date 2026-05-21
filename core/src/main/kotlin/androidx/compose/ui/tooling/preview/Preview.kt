package androidx.compose.ui.tooling.preview

@MustBeDocumented
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Preview(
    val name: String = "",
    val showBackground: Boolean = false,
    val widthDp: Int = 0,
    val uiMode: Int = 0
)
