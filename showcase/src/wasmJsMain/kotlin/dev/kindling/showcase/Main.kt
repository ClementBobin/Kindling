package dev.kindling.showcase

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Entry point. The docs website embeds this app with query parameters:
 *
 *  - `component=<id>`  render a single demo (ids are registered in [demos]); anything else shows the gallery
 *  - `theme=dark|light` colour scheme, synced with the docs site theme
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val query = parseQuery(window.location.search)
    ComposeViewport(document.body!!) {
        App(
            component = query["component"],
            dark = query["theme"] == "dark",
        )
    }
}

private fun parseQuery(search: String): Map<String, String> =
    search.removePrefix("?")
        .split("&")
        .filter { it.contains("=") }
        .associate { it.substringBefore("=") to it.substringAfter("=") }
