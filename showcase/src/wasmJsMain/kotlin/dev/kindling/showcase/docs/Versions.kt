package dev.kindling.showcase.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonSize
import dev.kindling.core.components.ui.button.KButtonVariant

/**
 * Under the page title: which release is shown, a picker for the others, and where the page first appeared.
 * [version] is the release being displayed, [newest] the newest release this page exists in.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VersionBar(page: DocPage, version: String, platforms: List<String>, onVersion: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    FlowRow(
        Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box {
            KButton(
                text = "Version " + rangeLabel(page.versionRanges().firstOrNull { version in it } ?: listOf(version)),
                onClick = { open = true },
                variant = KButtonVariant.Outline,
                size = KButtonSize.Sm,
            )
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                // One entry per distinct content: releases with an identical page are folded into a range.
                page.versionRanges().forEachIndexed { i, range ->
                    val selected = version in range
                    DropdownMenuItem(
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(rangeLabel(range), fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, fontSize = 14.sp)
                                if (i == 0) Pill("latest", strong = true)
                                if (range.last() == page.since) Pill("added")
                            }
                        },
                        onClick = { open = false; onVersion(range.first()) },
                    )
                }
                if (page.since.isNotEmpty() && page.since !in page.available) {
                    Text(
                        "Older: added in ${page.since}",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
        Text("Available since ${page.since}", fontSize = 13.sp, color = colors.onSurfaceVariant)
        if (platforms.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                platforms.forEach { Pill(platformLabel(it)) }
            }
        }
    }
}

/** Shown when the requested release is not the newest content of the page. */
@Composable
internal fun VersionBanner(page: DocPage, requested: String?, version: String, onVersion: (String?) -> Unit) {
    val newest = page.available.firstOrNull() ?: return
    val message = when {
        requested != null && requested !in page.available ->
            "This page isn't in $requested${if (page.since.isNotEmpty()) " (added in ${page.since})" else ""}. Showing $version instead."
        version == newest -> return
        page.sameAsNewest(version) -> "Viewing $version. The API on this page is unchanged up to $newest."
        else -> "Viewing $version. This page changed in a later release; the newest is $newest."
    }
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.outline, RoundedCornerShape(8.dp))
            .padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(message, fontSize = 13.sp, color = colors.onBackground, modifier = Modifier.weight(1f))
        KButton(text = "Latest", onClick = { onVersion(null) }, variant = KButtonVariant.Ghost, size = KButtonSize.Sm)
    }
}

/** Filter chips: All, Common, then each platform the page's declarations reach. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlatformFilter(decls: List<Decl>, selected: String?, onSelect: (String?) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val options = buildList {
        add(null to "All")
        if (decls.any { it.common }) add("common" to "Common")
        PLATFORMS.forEach { (id, name) -> if (decls.any { id in it.platforms }) add(id to name) }
    }
    Column(Modifier.padding(bottom = 8.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Platform", fontSize = 13.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(end = 4.dp))
            options.forEach { (id, name) ->
                val n = decls.count { it.matches(id) }
                KButton(
                    text = "$name  $n",
                    onClick = { onSelect(id) },
                    variant = if (selected == id) KButtonVariant.Default else KButtonVariant.Outline,
                    size = KButtonSize.Sm,
                )
            }
        }
    }
}

/** `4.3.6` for one release, `4.3.1 – 4.3.6` for a run of releases with the same page (list is newest first). */
private fun rangeLabel(range: List<String>): String = if (range.size == 1) range.first() else "${range.last()} – ${range.first()}"

/** Releases grouped by identical content, newest group first, each newest release first. */
internal fun DocPage.versionRanges(): List<List<String>> = listOf(headVersions) + history.map { it.versions }
