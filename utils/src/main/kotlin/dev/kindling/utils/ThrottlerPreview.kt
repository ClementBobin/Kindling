package dev.kindling.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Throttler — light")
@Preview(name = "Throttler — dark", uiMode = 0)
@Composable
private fun PreviewKThrottler() {
    previewCard(
        title = "Throttler<T> — 500 ms leading edge",
        lines = listOf("Throttler<T>", "emit(value)", "flow", "onThrottled(block)", "cancel()")
    )
}

private fun previewCard(title: String, lines: List<String>) {
    println(title)
    lines.forEach { println(it) }
}
