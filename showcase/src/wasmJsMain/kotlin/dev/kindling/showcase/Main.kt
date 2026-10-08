package dev.kindling.showcase

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.kindling.showcase.docs.DocsSite
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Entry point of the Kindling documentation website (hash-routed, see docs/Site.kt).
 *
 * `?component=<id>&theme=dark|light` still renders a single demo on its own, for embedding the live
 * preview of one component in another page.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val query = parseQuery(window.location.search)
    ComposeViewport(document.body!!) {
        if (query["component"] != null) {
            App(component = query["component"], dark = query["theme"] == "dark")
        } else {
            DocsSite()
        }
    }
}

private fun parseQuery(search: String): Map<String, String> =
    search.removePrefix("?")
        .split("&")
        .filter { it.contains("=") }
        .associate { it.substringBefore("=") to it.substringAfter("=") }
