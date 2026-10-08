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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import dev.kindling.showcase.docs.generated.DOCS_VERSION
import dev.kindling.showcase.docs.generated.generatedPages
import kotlinx.browser.window
import kotlinx.coroutines.launch

private const val GITHUB = "https://github.com/ClementBobin/Kindling"

private fun readRoute(): String =
    window.location.hash.removePrefix("#").trim('/').ifEmpty { "index" }

/** In-page links look like `/docs/components/badge`; the router uses `components/badge`. */
private fun normalizeLink(url: String): String =
    url.removePrefix("/docs").trim('/').ifEmpty { "index" }

private fun go(path: String) {
    window.location.hash = "#/$path"
}

/** The whole documentation website. Hash-routed so it works on GitHub Pages without server rewrites. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DocsSite() {
    val pages = remember { generatedPages() }
    val byPath = remember(pages) { pages.associateBy { it.path } }
    val groups = remember(pages) { pages.grouped() }
    val flat = remember(groups) { groups.flatMap { it.second } }

    val system = isSystemInDarkTheme()
    var dark by remember { mutableStateOf(system) }
    var route by remember { mutableStateOf(readRoute()) }
    var searchOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener: (org.w3c.dom.events.Event) -> Unit = { route = readRoute(); menuOpen = false }
        window.addEventListener("hashchange", listener)
        onDispose { window.removeEventListener("hashchange", listener) }
    }

    val page = byPath[route]
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
                    val xl = maxWidth >= 1200.dp
                    Column(Modifier.fillMaxSize()) {
                        TopBar(
                            wide = wide,
                            dark = dark,
                            onToggleTheme = { dark = !dark },
                            onSearch = { searchOpen = true },
                            onMenu = { menuOpen = !menuOpen },
                            route = route,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            if (wide) {
                                Sidebar(groups, route, Modifier.width(264.dp).fillMaxHeight())
                                VerticalDivider(color = MaterialTheme.colorScheme.outline)
                            }
                            PageContent(
                                page = page,
                                dark = dark,
                                flat = flat,
                                showToc = xl,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    }
                    if (!wide && menuOpen) {
                        Box(Modifier.fillMaxSize().padding(top = 57.dp).background(MaterialTheme.colorScheme.background)) {
                            Sidebar(groups, route, Modifier.fillMaxSize())
                        }
                    }
                }
                if (searchOpen) SearchOverlay(pages, onClose = { searchOpen = false }, onPick = { go(it); searchOpen = false })
            }
        }
    }
}

@Composable
private fun TopBar(
    wide: Boolean,
    dark: Boolean,
    onToggleTheme: () -> Unit,
    onSearch: () -> Unit,
    onMenu: () -> Unit,
    route: String,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!wide) KButton(text = "Menu", onClick = onMenu, variant = KButtonVariant.Ghost, size = KButtonSize.Sm)
        Row(
            Modifier.clickable { go("index") }.pointerHoverIcon(PointerIcon.Hand),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.height(20.dp).width(20.dp).clip(RoundedCornerShape(6.dp)).background(colors.primary))
            Text("Kindling", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colors.onBackground)
            Text(
                "v$DOCS_VERSION",
                fontSize = 11.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.border(1.dp, colors.outline, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        if (wide) {
            Row(Modifier.padding(start = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                NavLink("Docs", active = route == "index" || route == "installation" || route == "theming") { go("index") }
                NavLink("Components", active = route.startsWith("components")) { go("components") }
                NavLink("Showcase", active = route == "showcase") { go("showcase") }
            }
        }
        Box(Modifier.weight(1f))
        KButton(text = if (wide) "Search...  Ctrl K" else "Search", onClick = onSearch, variant = KButtonVariant.Outline, size = KButtonSize.Sm)
        KButton(text = if (dark) "Light" else "Dark", onClick = onToggleTheme, variant = KButtonVariant.Ghost, size = KButtonSize.Sm)
        if (wide) KButton(text = "GitHub", onClick = { window.open(GITHUB, "_blank") }, variant = KButtonVariant.Ghost, size = KButtonSize.Sm)
    }
}

@Composable
private fun NavLink(text: String, active: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Text(
        text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = if (active) colors.onBackground else colors.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun Sidebar(groups: List<Pair<String, List<DocPage>>>, route: String, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val collapsed = remember { mutableStateMapOf<String, Boolean>() }
    Column(modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        groups.forEach { (section, items) ->
            val isCollapsed = collapsed[section] ?: false
            Text(
                section,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { collapsed[section] = !isCollapsed }
                    .pointerHoverIcon(PointerIcon.Hand)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            )
            if (!isCollapsed) {
                items.forEach { p ->
                    val active = p.path == route
                    Text(
                        text = if (p.order == 0) "Overview" else p.title,
                        fontSize = 14.sp,
                        fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                        color = if (active) colors.onBackground else colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) colors.secondaryContainer else Color.Transparent)
                            .clickable { go(p.path) }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: DocPage?, dark: Boolean, flat: List<DocPage>, showToc: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val coords = remember(page) { mutableMapOf<Int, LayoutCoordinates>() }
    var viewport by remember { mutableStateOf<LayoutCoordinates?>(null) }
    LaunchedEffect(page) { scroll.scrollTo(0) }

    val ctx = remember(dark, page) {
        RenderCtx(
            dark = dark,
            onLink = { url -> if (url.startsWith("http")) window.open(url, "_blank") else go(normalizeLink(url)) },
            headingCoords = coords,
        )
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
                    Column(Modifier.widthIn(max = 800.dp).fillMaxWidth().padding(horizontal = 28.dp, vertical = 40.dp)) {
                        Text(page.section, fontSize = 13.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                        Text(page.title, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
                        if (page.description.isNotEmpty()) {
                            Text(page.description, fontSize = 17.sp, lineHeight = 26.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                        }
                        HorizontalDivider(Modifier.padding(bottom = 16.dp), color = colors.outline)
                        page.blocks.forEachIndexed { i, b -> RenderBlock(i, b, ctx) }
                        PrevNext(page, flat)
                    }
                }
            }
        }
        if (showToc && page != null && page.headings.isNotEmpty()) {
            Column(
                Modifier.width(240.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(top = 40.dp, end = 16.dp, start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text("On this page", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.onBackground, modifier = Modifier.padding(bottom = 8.dp))
                page.headings.forEach { (index, h) ->
                    Text(
                        h.text.replace("`", ""),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val target = coords[index]
                                val top = viewport
                                if (target != null && target.isAttached && top != null) {
                                    val y = target.positionInRoot().y - top.positionInRoot().y
                                    scope.launch { scroll.animateScrollTo((scroll.value + y - 16f).toInt().coerceAtLeast(0)) }
                                }
                            }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .padding(start = if (h.level == 3) 12.dp else 0.dp, top = 3.dp, bottom = 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PrevNext(page: DocPage, flat: List<DocPage>) {
    val i = flat.indexOfFirst { it.path == page.path }
    if (i < 0) return
    val prev = flat.getOrNull(i - 1)
    val next = flat.getOrNull(i + 1)
    Row(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        if (prev != null) KButton(text = "Previous: ${prev.title}", onClick = { go(prev.path) }, variant = KButtonVariant.Outline) else Box {}
        if (next != null) KButton(text = "Next: ${next.title}", onClick = { go(next.path) }, variant = KButtonVariant.Outline) else Box {}
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
