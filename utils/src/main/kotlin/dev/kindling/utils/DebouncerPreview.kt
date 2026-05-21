package dev.kindling.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Debouncer — light")
@Preview(name = "Debouncer — dark", uiMode = 0)
@Composable
private fun PreviewKDebouncer() {
    previewCard(
        title = "Debouncer<T> — 300 ms trailing edge",
        lines = listOf("Debouncer<T>", "emit(value)", "flow", "onDebounced(block)", "cancel()")
    )
}

private fun previewCard(title: String, lines: List<String>) {
    println(title)
    lines.forEach { println(it) }
}
