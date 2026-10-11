package dev.kindling.showcase.docs

/** Releases that share an identical page; the blocks are only built when someone views that version. */
class VersionGroup(val versions: List<String>, build: () -> List<Block>) {
    val blocks: List<Block> by lazy(build)
}

/** One documentation page. Instances are produced by docs-gen/scripts/docgen.mjs. */
class DocPage(
    val path: String,
    val section: String,
    val title: String,
    val description: String,
    val order: Int,
    val blocks: List<Block>,
    /** Oldest release that has this page (empty for hand-written pages, which are not versioned). */
    val since: String = "",
    /** Releases with a snapshot of this page, newest first. */
    val available: List<String> = emptyList(),
    /** Releases whose content equals [blocks]. */
    val headVersions: List<String> = emptyList(),
    /** Older content, newest first; [blocks] covers [headVersions]. */
    val history: List<VersionGroup> = emptyList(),
) {
    /** Lower-cased text used by the search dialog. */
    val searchText: String by lazy {
        (listOf(title, description) + blocks.mapNotNull { it.plainText() }).joinToString(" ").lowercase()
    }

    val headings: List<Pair<Int, Heading>> by lazy { headingsOf(blocks) }

    val versioned: Boolean get() = available.isNotEmpty()

    /** Content of [version]; falls back to the newest content. */
    fun blocksFor(version: String?): List<Block> =
        if (version == null || version in headVersions) blocks else history.firstOrNull { version in it.versions }?.blocks ?: blocks

    /** True if [version] shows the same content as the page's newest snapshot. */
    fun sameAsNewest(version: String): Boolean = version in headVersions
}

sealed interface Block

class Heading(val level: Int, val text: String) : Block
class Para(val text: String) : Block
class CodeBlock(val lang: String, val code: String) : Block
class Bullets(val ordered: Boolean, val items: List<String>) : Block
class Quote(val text: String) : Block
class PropRow(val name: String, val type: String, val default: String, val description: String, val required: Boolean)
class Props(val rows: List<PropRow>) : Block
class Demo(val id: String) : Block

/** A full-width interactive tool (`theme` or `utils`), see docs/playground/. */
class Playground(val id: String) : Block
class Source(val url: String) : Block

/** One place a declaration is implemented: a source set (`commonMain`, `iosMain`...), expect/actual kind, link. */
class Impl(val sourceSet: String, val kind: String, val covers: List<String>, val url: String)

/** A documented declaration. [platforms] are the targets it reaches; [children] are the blocks under its heading. */
class Decl(
    val name: String,
    val platforms: List<String>,
    val common: Boolean,
    val universal: Boolean,
    val impls: List<Impl>,
    val children: List<Block>,
) {
    /** `null` = no filter, `common` = declared in commonMain, otherwise a platform id. */
    fun matches(platform: String?): Boolean = platform == null || (platform == "common" && common) || platform in platforms
}

/** A titled run of declarations ("Composables", "Types"...). */
class DeclGroup(val title: String, val decls: List<Decl>) : Block
data object Gallery : Block
data object Rule : Block

private fun Block.plainText(): String? = when (this) {
    is Heading -> text
    is Para -> text
    is Bullets -> items.joinToString(" ")
    is Quote -> text
    is Props -> rows.joinToString(" ") { it.name + " " + it.description }
    is DeclGroup -> title + " " + decls.joinToString(" ") { d -> d.name + " " + d.children.mapNotNull { it.plainText() }.joinToString(" ") }
    else -> null
}

/** Display order of the sidebar groups. */
internal val sectionOrder = listOf("Getting started", "Playground", "Components", "Utils", "Compose", "Android", "Project")

/** Pages grouped for the sidebar, in display order. */
internal fun List<DocPage>.grouped(): List<Pair<String, List<DocPage>>> =
    groupBy { it.section }
        .toList()
        .sortedBy { (s, _) -> sectionOrder.indexOf(s).let { if (it < 0) Int.MAX_VALUE else it } }
        .map { (s, pages) -> s to pages.sortedWith(compareBy({ it.order }, { it.title })) }

/** Platforms in display order. */
internal val PLATFORMS = listOf("android" to "Android", "ios" to "iOS", "desktop" to "Desktop", "web" to "Web")

internal fun platformLabel(id: String): String = if (id == "common") "Common" else PLATFORMS.firstOrNull { it.first == id }?.second ?: id

internal fun List<Block>.decls(): List<Decl> = filterIsInstance<DeclGroup>().flatMap { it.decls }

/** True when the platform filter has something to filter: a declaration that is not on every target, or has several implementations. */
internal fun List<Block>.hasPlatformVariants(): Boolean = decls().any { !it.universal || it.impls.size > 1 }

/** Keeps declarations available on [platform]; groups left empty disappear. */
internal fun List<Block>.forPlatform(platform: String?): List<Block> {
    if (platform == null) return this
    return mapNotNull { b ->
        if (b !is DeclGroup) b
        else b.decls.filter { it.matches(platform) }.let { kept -> if (kept.isEmpty()) null else DeclGroup(b.title, kept) }
    }
}

/**
 * Headings for the "On this page" list, keyed by the index [RenderBlock] reports them under: top-level blocks use
 * their position, declarations inside a group use `position * 1000 + declaration index`.
 */
internal fun headingsOf(blocks: List<Block>): List<Pair<Int, Heading>> = buildList {
    blocks.forEachIndexed { i, b ->
        when {
            b is Heading && b.level in 2..3 -> add(i to b)
            b is DeclGroup -> {
                add(i to Heading(2, b.title))
                b.decls.forEachIndexed { j, d -> add(i * 1000 + j to Heading(3, d.name)) }
            }
        }
    }
}
