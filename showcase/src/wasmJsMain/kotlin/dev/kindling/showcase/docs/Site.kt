package dev.kindling.showcase.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.KInput
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonSize
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.core.theme.KindlingTheme
import dev.kindling.showcase.ShadcnDark
import dev.kindling.showcase.ShadcnLight
import dev.kindling.showcase.docs.generated.DOCS_BRANCH
import dev.kindling.showcase.docs.generated.DOCS_VERSION
import dev.kindling.showcase.docs.generated.generatedPages
import kotlinx.browser.window
import kotlinx.coroutines.launch

private const val GITHUB = "https://github.com/ClementBobin/Kindling"

/** Hash location: `#/components/button?v=4.2.0&p=ios` (version and platform filter are optional). */
internal class Loc(val path: String, val version: String?, val platform: String?)

private var currentLoc = Loc("index", null, null)

private fun readLoc(): Loc {
    val raw = window.location.hash.removePrefix("#").trimStart('/')
    val q = raw.indexOf('?')
    val path = (if (q >= 0) raw.substring(0, q) else raw).trim('/').ifEmpty { "index" }
    val params = if (q >= 0) {
        raw.substring(q + 1).split('&').mapNotNull { kv -> kv.indexOf('=').takeIf { it > 0 }?.let { kv.substring(0, it) to kv.substring(it + 1) } }.toMap()
    } else emptyMap()
    return Loc(path, params["v"]?.ifEmpty { null }, params["p"]?.ifEmpty { null }).also { currentLoc = it }
}

private fun navigate(path: String, version: String?, platform: String?) {
    val q = listOfNotNull(version?.let { "v=$it" }, platform?.let { "p=$it" }).joinToString("&")
    window.location.hash = "#/$path" + if (q.isNotEmpty()) "?$q" else ""
}

/** In-page links look like `/docs/components/badge`; the router uses `components/badge`. */
private fun normalizeLink(url: String): String =
    url.removePrefix("/docs").trim('/').ifEmpty { "index" }

/** Opens a page, keeping the chosen version (the platform filter only makes sense per page). */
private fun go(path: String) = navigate(path, currentLoc.version, null)

/** The whole documentation website. Hash-routed so it works on GitHub Pages without server rewrites. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DocsSite() {
    val pages = remember { generatedPages() }
    val byPath = remember(pages) { pages.associateBy { it.path } }
    val groups = remember(pages) { pages.grouped() }
    val flat = remember(groups) { groups.flatMap { it.second } }
    val tree = remember(groups) { buildNav(groups) }

    val system = isSystemInDarkTheme()
    var dark by remember { mutableStateOf(system) }
    var loc by remember { mutableStateOf(readLoc()) }
    val route = loc.path
    var searchOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener: (org.w3c.dom.events.Event) -> Unit = { loc = readLoc(); menuOpen = false }
        window.addEventListener("hashchange", listener)
        onDispose { window.removeEventListener("hashchange", listener) }
    }

    val page = byPath[route]
    val full = page?.fullWidth == true
    val dev = route == "dev" // hidden smoke-test page, see DevPage.kt
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    KindlingTheme(colorScheme = if (dark) ShadcnDark else ShadcnLight) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(
                Modifier
                    .fillMaxSize()
                    .focusRequester(focus)
                    .focusable()
                    .onPreviewKeyEvent { e ->
                        val k = e.type == KeyEventType.KeyDown && e.key == Key.K && (e.isMetaPressed || e.isCtrlPressed)
                        if (k) searchOpen = true
                        k
                    },
            ) {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val wide = maxWidth >= 800.dp
                    val xl = maxWidth >= 1100.dp
                    Column(Modifier.fillMaxSize()) {
                        TopBar(
                            wide = wide,
                            dark = dark,
                            active = if (full) route else if (dev) "" else "docs",
                            onNavigate = { go(it) },
                            onToggleTheme = { dark = !dark },
                            onSearch = { searchOpen = true },
                            onMenu = { menuOpen = !menuOpen },
                            onGithub = { window.open(GITHUB, "_blank") },
                        )
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            Row(Modifier.widthIn(max = SITE_MAX_WIDTH).fillMaxSize()) {
                                if (wide && !full && !dev) Sidebar(tree, route, ::go, Modifier.width(256.dp).fillMaxHeight())
                                if (dev) {
                                    DevPage(onOpenPage = { go(it) }, modifier = Modifier.weight(1f).fillMaxHeight())
                                } else {
                                    PageContent(
                                        page = page,
                                        loc = loc,
                                        dark = dark,
                                        flat = flat,
                                        showToc = xl && !full,
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                    )
                                }
                            }
                        }
                    }
                    if (!wide && menuOpen) {
                        Column(Modifier.fillMaxSize().padding(top = 57.dp).background(MaterialTheme.colorScheme.background)) {
                            MobileLinks(if (full) route else "docs") { go(it) }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Sidebar(tree, route, ::go, Modifier.weight(1f).fillMaxWidth())
                        }
                    }
                }
                if (searchOpen) SearchOverlay(pages, onClose = { searchOpen = false }, onPick = { go(it); searchOpen = false })
            }
        }
    }
}

@Composable
private fun PageContent(page: DocPage?, loc: Loc, dark: Boolean, flat: List<DocPage>, showToc: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    // Release shown: the requested one if this page has it, else the page's newest.
    val version = when {
        page == null || !page.versioned -> ""
        loc.version != null && loc.version in page.available -> loc.version
        else -> page.available.first()
    }
    val rawBlocks = page?.blocksFor(version.ifEmpty { null }) ?: emptyList()
    val filterable = remember(rawBlocks) { rawBlocks.hasPlatformVariants() }
    val platform = loc.platform.takeIf { filterable }
    val blocks = remember(rawBlocks, platform) { rawBlocks.forPlatform(platform) }
    val headings = remember(blocks) { headingsOf(blocks) }

    val coords = remember(page, version, platform) { mutableMapOf<Int, LayoutCoordinates>() }
    var viewport by remember { mutableStateOf<LayoutCoordinates?>(null) }
    LaunchedEffect(page) { scroll.scrollTo(0) }

    val ctx = remember(dark, page, version, platform, coords) {
        RenderCtx(
            dark = dark,
            onLink = { url -> if (url.startsWith("http")) window.open(url, "_blank") else go(normalizeLink(url)) },
            headingCoords = coords,
            version = version.ifEmpty { DOCS_VERSION },
            ref = if (version.isEmpty() || version == DOCS_VERSION) DOCS_BRANCH else version,
            platform = platform,
            oldVersion = page != null && version.isNotEmpty() && version != page.available.first(),
        )
    }
    val here = page?.path ?: ""

    // Scroll-spy: the last heading that has reached the top of the viewport (or the last one at the bottom).
    val activeHeading by remember(headings) {
        derivedStateOf {
            val top = viewport?.takeIf { it.isAttached }?.positionInRoot()?.y ?: 0f
            val atEnd = scroll.maxValue > 0 && scroll.value >= scroll.maxValue - 4
            var active = headings.firstOrNull()?.first
            for ((index, _) in headings) {
                val c = coords[index]?.takeIf { it.isAttached } ?: continue
                if (c.positionInRoot().y - top <= 110f) active = index else break
            }
            if (atEnd) headings.lastOrNull()?.first ?: active else active
        }
    }
    val scrollTo: (Int) -> Unit = { index ->
        val target = coords[index]
        val top = viewport
        if (target != null && target.isAttached && top != null) {
            val y = target.positionInRoot().y - top.positionInRoot().y
            scope.launch { scroll.animateScrollTo((scroll.value + y - 24f).toInt().coerceAtLeast(0)) }
        }
    }

    Row(modifier) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .onGloballyPositioned { viewport = it }
                .verticalScroll(scroll),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (page == null) {
                Column(Modifier.padding(40.dp)) {
                    Text("Page not found", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = colors.onBackground)
                    Text("This page does not exist.", color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                    KButton(text = "Back to introduction", onClick = { go("index") })
                }
            } else {
                SelectionContainer {
                    Column(Modifier.widthIn(max = if (page.fullWidth) SITE_MAX_WIDTH else 760.dp).fillMaxWidth().padding(horizontal = 28.dp, vertical = 36.dp)) {
                        Breadcrumb(page)
                        val i = flat.indexOfFirst { it.path == page.path }
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                page.title,
                                fontSize = 32.sp,
                                lineHeight = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onBackground,
                                modifier = Modifier.weight(1f),
                            )
                            if (i >= 0) {
                                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    flat.getOrNull(i - 1)?.let { prev -> IconBox(Icons.AutoMirrored.Filled.ArrowBack, "Previous: ${prev.title}") { go(prev.path) } }
                                    flat.getOrNull(i + 1)?.let { next -> IconBox(Icons.AutoMirrored.Filled.ArrowForward, "Next: ${next.title}") { go(next.path) } }
                                }
                            }
                        }
                        if (page.description.isNotEmpty()) {
                            Text(page.description, fontSize = 16.sp, lineHeight = 25.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
                        }
                        if (page.versioned) {
                            VersionBar(page, version, rawBlocks.decls().flatMap { it.platforms }.distinct().sortedBy { p -> PLATFORMS.indexOfFirst { it.first == p } }) {
                                navigate(here, it, platform)
                            }
                            VersionBanner(page, loc.version, version) { navigate(here, it, platform) }
                        }
                        if (filterable) PlatformFilter(rawBlocks.decls(), platform) { navigate(here, loc.version, it) }
                        blocks.forEachIndexed { i, b -> RenderBlock(i, b, ctx) }
                    }
                }
            }
        }
        if (showToc && page != null && headings.isNotEmpty()) {
            Toc(headings, activeHeading, scrollTo, Modifier.width(240.dp).fillMaxHeight())
        }
    }
}

@Composable
private fun Breadcrumb(page: DocPage) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Docs", fontSize = 13.sp, color = colors.onSurfaceVariant)
        if (!page.fullWidth && page.section != "Getting started") {
            Text("/", fontSize = 13.sp, color = colors.onSurfaceVariant.copy(alpha = 0.5f))
            Text(page.section, fontSize = 13.sp, color = colors.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchOverlay(pages: List<DocPage>, onClose: () -> Unit, onPick: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    val results by remember(pages) {
        derivedStateOf {
            val tokens = query.lowercase().split(' ').filter { it.isNotBlank() }
            if (tokens.isEmpty()) emptyList()
            else pages
                .filter { p -> tokens.all { it in p.searchText } }
                .sortedBy { p -> if (tokens.all { it in p.title.lowercase() }) 0 else 1 }
                .take(8)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClose)
            .onPreviewKeyEvent { e ->
                val esc = e.type == KeyEventType.KeyDown && e.key == Key.Escape
                if (esc) onClose()
                esc
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .padding(top = 96.dp, start = 16.dp, end = 16.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.background)
                .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
                .clickable(enabled = false) {}
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KInput(value = query, onValueChange = { query = it }, placeholder = "Search documentation...", modifier = Modifier.fillMaxWidth().focusRequester(focus))
            if (query.isNotBlank() && results.isEmpty()) {
                Text("No results.", fontSize = 14.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(8.dp))
            }
            results.forEach { p ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPick(p.path) }
                        .pointerHoverIcon(PointerIcon.Hand)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(p.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.onBackground)
                    Text(p.section, fontSize = 12.sp, color = colors.onSurfaceVariant, fontFamily = FontFamily.Default)
                }
            }
        }
    }
}
