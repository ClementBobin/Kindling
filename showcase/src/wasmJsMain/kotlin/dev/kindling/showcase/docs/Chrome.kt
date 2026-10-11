package dev.kindling.showcase.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.showcase.docs.generated.DOCS_VERSION

/** Max width of the whole site chrome, like the shadcn docs container. */
internal val SITE_MAX_WIDTH = 1400.dp

/** Top-level destinations. `docs` stands for every sidebar page. */
private val NAV = listOf("docs" to "Docs", "create" to "Create", "playground" to "Playground", "showcase" to "Showcase")

private fun navTarget(id: String) = if (id == "docs") "index" else id

@Composable
internal fun IconBox(icon: ImageVector, description: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).pointerHoverIcon(PointerIcon.Hand),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, description, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
}

@Composable
internal fun TopBar(
    wide: Boolean,
    dark: Boolean,
    active: String,
    onNavigate: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onSearch: () -> Unit,
    onMenu: () -> Unit,
    onGithub: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Row(
                Modifier.widthIn(max = SITE_MAX_WIDTH).fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!wide) IconBox(Icons.Filled.Menu, "Menu", onMenu)
                Row(
                    Modifier.clip(RoundedCornerShape(8.dp)).clickable { onNavigate("index") }.pointerHoverIcon(PointerIcon.Hand).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(colors.primary), contentAlignment = Alignment.Center) {
                        Text("K", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.onPrimary)
                    }
                    Text("Kindling", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = colors.onBackground)
                }
                if (wide) {
                    NAV.forEach { (id, label) -> NavLink(label, active == id) { onNavigate(navTarget(id)) } }
                }
                Box(Modifier.weight(1f))
                SearchButton(wide, onSearch)
                if (wide) Text("v$DOCS_VERSION", fontSize = 12.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
                IconBox(Icons.Filled.Code, "GitHub", onGithub)
                IconBox(if (dark) Icons.Filled.LightMode else Icons.Filled.DarkMode, "Toggle theme", onToggleTheme)
            }
        }
        HorizontalDivider(color = colors.outline)
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun SearchButton(wide: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .then(if (wide) Modifier.width(260.dp) else Modifier)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, colors.outline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(if (wide) "Search documentation..." else "Search", fontSize = 13.sp, color = colors.onSurfaceVariant)
        if (wide) Pill("Ctrl K")
    }
}

/** Links shown at the top of the mobile menu, where the top bar has no room for them. */
@Composable
internal fun MobileLinks(active: String, onNavigate: (String) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        NAV.forEach { (id, label) -> NavLink(label, active == id) { onNavigate(navTarget(id)) } }
    }
}

// ── Sidebar tree ────────────────────────────────────────────────────────────────

internal class NavNode(val key: String, val title: String, val path: String?, val dim: Boolean, val children: List<NavNode>) {
    fun contains(route: String): Boolean = path == route || children.any { it.contains(route) }
}

private fun humanize(token: String) = token.replaceFirstChar { it.uppercase() }

/**
 * Sections become folders. Inside a section, pages sharing a first word (`format-text`, `format-number`,
 * `input`, `input-group`...) are folded under one parent; a page named exactly like the word becomes that parent's own page.
 */
internal fun buildNav(groups: List<Pair<String, List<DocPage>>>): List<NavNode> = groups.map { (section, pages) ->
    val api = pages.filter { '/' in it.path && it.order != 0 }
    val other = pages.filter { it !in api }
    val byToken = api.groupBy { it.path.substringAfterLast('/').substringBefore('-') }
    val nodes = mutableListOf<NavNode>()
    other.forEach { nodes += leaf(it, if (it.order == 0) "Overview" else it.title) }
    val seen = mutableSetOf<String>()
    api.forEach { p ->
        val token = p.path.substringAfterLast('/').substringBefore('-')
        val family = byToken.getValue(token)
        if (family.size < 2) {
            nodes += leaf(p, p.title)
        } else if (seen.add(token)) {
            val parent = family.firstOrNull { it.path.substringAfterLast('/') == token }
            val kids = family.filter { it !== parent }.map { k ->
                leaf(k, k.title.removePrefix(humanize(token) + " ").ifEmpty { k.title })
            }
            nodes += NavNode("$section/$token", parent?.title ?: humanize(token), parent?.path, parent?.isDim() == true, kids)
        }
    }
    NavNode("section:$section", section, null, false, nodes)
}

private fun DocPage.isDim() = versioned && available.first() != DOCS_VERSION
private fun leaf(p: DocPage, title: String) = NavNode(p.path, title, p.path, p.isDim(), emptyList())

@Composable
internal fun Sidebar(tree: List<NavNode>, route: String, onGo: (String) -> Unit, modifier: Modifier) {
    val open = remember { mutableStateMapOf<String, Boolean>() }
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        tree.forEach { section ->
            val expanded = open[section.key] ?: (section.contains(route) || section.title == "Getting started")
            NavRow(section, section = true, route, expanded, onToggle = { open[section.key] = !expanded }, onGo)
            if (expanded) Guided { section.children.forEach { NavBranch(it, route, open, onGo) } }
            Spacer(Modifier.height(6.dp))
        }
    }
}

/** Children of a folder: indented under a thin guide line, like shadcn's nested sidebar menus. */
@Composable
private fun Guided(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.height(IntrinsicSize.Min).padding(start = 12.dp)) {
        Box(Modifier.width(1.dp).fillMaxHeight().background(colors.outline))
        Column(Modifier.padding(start = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { content() }
    }
}

@Composable
private fun NavBranch(node: NavNode, route: String, open: MutableMap<String, Boolean>, onGo: (String) -> Unit) {
    val expanded = open[node.key] ?: node.contains(route)
    NavRow(node, section = false, route, expanded, onToggle = { open[node.key] = !expanded }, onGo)
    if (expanded && node.children.isNotEmpty()) Guided { node.children.forEach { NavBranch(it, route, open, onGo) } }
}

@Composable
private fun NavRow(node: NavNode, section: Boolean, route: String, expanded: Boolean, onToggle: () -> Unit, onGo: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val active = node.path == route
    val folder = node.children.isNotEmpty()
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) colors.secondaryContainer else Color.Transparent)
            .clickable {
                if (node.path != null) onGo(node.path)
                if (folder && (node.path == null || !expanded)) onToggle()
            }
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(start = 8.dp, end = 4.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            node.title,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = if (section) 13.sp else 14.sp,
            fontWeight = if (section) FontWeight.SemiBold else if (active) FontWeight.Medium else FontWeight.Normal,
            color = when {
                active || section -> colors.onBackground
                node.dim -> colors.onSurfaceVariant.copy(alpha = 0.55f)
                else -> colors.onSurfaceVariant
            },
        )
        if (folder) {
            Box(Modifier.size(22.dp).clip(RoundedCornerShape(4.dp)).clickable(onClick = onToggle), contentAlignment = Alignment.Center) {
                Icon(
                    if (expanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                    if (expanded) "Collapse" else "Expand",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ── On this page ────────────────────────────────────────────────────────────────

/** shadcn-style table of contents: a rail on the left, the section being read highlighted. */
@Composable
internal fun Toc(headings: List<Pair<Int, Heading>>, activeIndex: Int?, onPick: (Int) -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.verticalScroll(rememberScrollState()).padding(top = 40.dp, start = 8.dp, end = 16.dp, bottom = 24.dp)) {
        Text(
            "On This Page",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Column(Modifier.height(IntrinsicSize.Min)) {
            headings.forEach { (index, h) ->
                val on = index == activeIndex
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .clickable { onPick(index) }
                        .pointerHoverIcon(PointerIcon.Hand),
                ) {
                    Box(Modifier.width(1.dp).fillMaxHeight().background(if (on) colors.onBackground else colors.outline))
                    Text(
                        h.text.replace("`", ""),
                        fontSize = 13.sp,
                        fontWeight = if (on) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (on) colors.onBackground else colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = if (h.level == 3) 24.dp else 12.dp, top = 4.dp, bottom = 4.dp),
                    )
                }
            }
        }
    }
}
