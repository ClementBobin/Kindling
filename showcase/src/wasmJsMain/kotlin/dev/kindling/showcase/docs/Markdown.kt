package dev.kindling.showcase.docs

import dev.kindling.showcase.docs.generated.demoSources

/** Renders a page as Markdown, the way "Copy page" hands it to people and to AI assistants. */
internal fun DocPage.toMarkdown(blocks: List<Block>, ctx: RenderCtx): String = buildString {
    appendLine("# $title")
    if (description.isNotEmpty()) appendLine().appendLine(ctx.fill(description))
    appendBlocks(blocks, ctx, 2)
}.trimEnd() + "\n"

/** Markdown plus a short preamble, for pasting into a chat with an AI assistant. */
internal fun DocPage.toAiPrompt(blocks: List<Block>, ctx: RenderCtx): String =
    "I'm reading the Kindling (Kotlin Multiplatform / Compose) documentation page \"$title\" (version ${ctx.version}). " +
        "Use it as context and answer my questions about it.\n\n---\n\n" + toMarkdown(blocks, ctx)

private fun StringBuilder.appendBlocks(blocks: List<Block>, ctx: RenderCtx, declLevel: Int) {
    for (b in blocks) {
        when (b) {
            is Heading -> appendLine().appendLine("#".repeat(b.level.coerceIn(1, 6)) + " " + ctx.fill(b.text))
            is Para -> appendLine().appendLine(ctx.fill(b.text))
            is Quote -> appendLine().appendLine(ctx.fill(b.text).lines().joinToString("\n") { "> $it" })
            is CodeBlock -> appendLine().appendLine("```" + b.lang).appendLine(ctx.fill(b.code).trimEnd()).appendLine("```")
            is Bullets -> {
                appendLine()
                b.items.forEachIndexed { i, item -> appendLine((if (b.ordered) "${i + 1}. " else "- ") + ctx.fill(item)) }
            }
            is Props -> {
                appendLine()
                appendLine("| Name | Type | Default | Description |")
                appendLine("| --- | --- | --- | --- |")
                for (r in b.rows) {
                    val cells = listOf(r.name + if (r.required) " (required)" else "", r.type, r.default, r.description)
                    appendLine(cells.joinToString(" | ", "| ", " |") { ctx.fill(it).replace("|", "\\|").replace("\n", " ") })
                }
            }
            is Demo -> demoSources[b.id]?.let { appendLine().appendLine("```kotlin").appendLine(it.trimEnd()).appendLine("```") }
            is DeclGroup -> {
                appendLine().appendLine("#".repeat(declLevel) + " " + b.title)
                for (d in b.decls) {
                    appendLine().appendLine("#".repeat(declLevel + 1) + " " + d.name)
                    if (d.platforms.isNotEmpty() && !d.universal) appendLine().appendLine("Platforms: " + d.platforms.joinToString(", ") { platformLabel(it) })
                    appendBlocks(d.children, ctx, declLevel + 2)
                }
            }
            is Source -> appendLine().appendLine("Source: " + ctx.fill(b.url))
            is Playground, Gallery, Rule -> {}
        }
    }
}
