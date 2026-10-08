package dev.kindling.showcase.docs

/** One documentation page. Instances are produced by docs-gen/scripts/docgen.mjs. */
class DocPage(
    val path: String,
    val section: String,
    val title: String,
    val description: String,
    val order: Int,
    val blocks: List<Block>,
) {
    /** Lower-cased text used by the search dialog. */
    val searchText: String by lazy {
        (listOf(title, description) + blocks.mapNotNull { it.plainText() }).joinToString(" ").lowercase()
    }

    val headings: List<Pair<Int, Heading>> by lazy {
        blocks.withIndex().filter { (_, b) -> b is Heading && b.level in 2..3 }.map { (i, b) -> i to (b as Heading) }
    }
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
class Source(val url: String) : Block
data object Gallery : Block
data object Rule : Block

private fun Block.plainText(): String? = when (this) {
    is Heading -> text
    is Para -> text
    is Bullets -> items.joinToString(" ")
    is Quote -> text
    is Props -> rows.joinToString(" ") { it.name + " " + it.description }
    else -> null
}

/** Display order of the sidebar groups. */
internal val sectionOrder = listOf("Getting started", "Components", "Utils", "Compose", "Android", "Project")

/** Pages grouped for the sidebar, in display order. */
internal fun List<DocPage>.grouped(): List<Pair<String, List<DocPage>>> =
    groupBy { it.section }
        .toList()
        .sortedBy { (s, _) -> sectionOrder.indexOf(s).let { if (it < 0) Int.MAX_VALUE else it } }
        .map { (s, pages) -> s to pages.sortedWith(compareBy({ it.order }, { it.title })) }
