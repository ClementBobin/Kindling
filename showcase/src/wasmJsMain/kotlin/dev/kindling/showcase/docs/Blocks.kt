package dev.kindling.showcase.docs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.showcase.demos
import dev.kindling.showcase.docs.playground.PlaygroundHost

/** What the block renderer needs from the surrounding site. */
internal class RenderCtx(
    val dark: Boolean,
    val onLink: (String) -> Unit,
    val headingCoords: MutableMap<Int, LayoutCoordinates>,
    /** Release being shown (fills `{{version}}` in snippets). Empty on unversioned pages. */
    val version: String = "",
    /** Git ref for source links (fills `{{ref}}`): the branch for the newest release, the tag otherwise. */
    val ref: String = "main",
    /** Active platform filter, used to highlight matching implementations. */
    val platform: String? = null,
    /** True when showing a release other than the one the live previews are built from. */
    val oldVersion: Boolean = false,
) {
    fun fill(text: String): String = text.replace("{{version}}", version).replace("{{ref}}", ref)
}

@Composable
private fun rich(text: String, ctx: RenderCtx): AnnotatedString {
    val bg = MaterialTheme.colorScheme.surfaceVariant
    val link = MaterialTheme.colorScheme.onBackground
    return remember(text, bg, link) { parseInline(text, bg, link, ctx.onLink) }
}

@Composable
internal fun RenderBlock(index: Int, block: Block, ctx: RenderCtx) {
    val colors = MaterialTheme.colorScheme
    when (block) {
        is Heading -> {
            val (size, top) = when (block.level) {
                1 -> 30.sp to 8.dp
                2 -> 24.sp to 40.dp
                3 -> 18.sp to 28.dp
                else -> 16.sp to 20.dp
            }
            Text(
                text = rich(block.text, ctx),
                fontSize = size,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground,
                modifier = Modifier
                    .padding(top = top, bottom = 12.dp)
                    .onGloballyPositioned { ctx.headingCoords[index] = it },
            )
        }
        is Para -> Text(
            text = rich(block.text, ctx),
            fontSize = 15.sp,
            lineHeight = 25.sp,
            color = colors.onBackground,
            modifier = Modifier.padding(bottom = 14.dp),
        )
        is CodeBlock -> CodeBox(block, ctx)
        is Bullets -> Column(Modifier.padding(bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.items.forEachIndexed { i, item ->
                Row(verticalAlignment = Alignment.Top) {
                    if (block.ordered) {
                        Text("${i + 1}.", fontSize = 15.sp, color = colors.onSurfaceVariant, modifier = Modifier.width(26.dp))
                    } else {
                        Box(Modifier.width(26.dp).padding(top = 10.dp, start = 8.dp)) {
                            Box(Modifier.size(5.dp).clip(CircleShape).background(colors.onSurfaceVariant))
                        }
                    }
                    Text(rich(item, ctx), fontSize = 15.sp, lineHeight = 24.sp, color = colors.onBackground)
                }
            }
        }
        is Quote -> Row(
            Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceVariant),
        ) {
            Box(Modifier.width(3.dp).heightIn(min = 40.dp).background(colors.primary))
            Text(rich(block.text, ctx), fontSize = 14.sp, lineHeight = 22.sp, color = colors.onBackground, modifier = Modifier.padding(14.dp))
        }
        is Props -> PropsTable(block, ctx)
        is Demo -> DemoBox(block.id, ctx.oldVersion)
        is Playground -> PlaygroundHost(block.id, ctx.dark)
        is DeclGroup -> DeclGroupView(index, block, ctx)
        is Source -> Text(
            text = rich("[View source on GitHub](${ctx.fill(block.url)})", ctx),
            fontSize = 13.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        Gallery -> GalleryBox()
        Rule -> HorizontalDivider(Modifier.padding(vertical = 24.dp), color = colors.outline)
    }
}

@Composable
private fun CodeBox(block: CodeBlock, ctx: RenderCtx) {
    val colors = MaterialTheme.colorScheme
    val text = remember(block, ctx.dark, ctx.version) { highlight(ctx.fill(block.code), ctx.dark) }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceVariant.copy(alpha = if (ctx.dark) 0.5f else 0.6f),
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
    ) {
        Column {
            Text(
                block.lang,
                fontSize = 11.sp,
                color = colors.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 14.dp, top = 8.dp),
            )
            Text(
                text = text,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = colors.onBackground,
                softWrap = false,
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(14.dp),
            )
        }
    }
}

@Composable
private fun PropsTable(block: Props, ctx: RenderCtx) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(10.dp)),
    ) {
        block.rows.forEachIndexed { i, row ->
            if (i > 0) HorizontalDivider(color = colors.outline)
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        row.name + if (row.required) " *" else "",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = colors.onBackground,
                    )
                }
                Text(row.type, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = colors.onSurfaceVariant)
                if (row.default.isNotEmpty()) {
                    Text("default: ${row.default}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = colors.onSurfaceVariant)
                }
                if (row.description.isNotEmpty()) {
                    Text(rich(row.description, ctx), fontSize = 14.sp, lineHeight = 21.sp, color = colors.onBackground)
                }
            }
        }
    }
}

/** A live, interactive preview of a real Kindling component. */
@Composable
private fun DemoBox(id: String, oldVersion: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val demo = demos.firstOrNull { it.id == id }
    Box(
        Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(10.dp))
            .background(colors.background)
            .heightIn(min = 160.dp)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (demo != null) demo.content() else Text("No live preview for this component yet.", color = colors.onSurfaceVariant, fontSize = 13.sp)
            if (oldVersion) {
                Text(
                    "Live preview always renders the newest release.",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GalleryBox() {
    var selected by remember { mutableStateOf(demos.first().id) }
    Column(Modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            demos.forEach { d ->
                KButton(
                    text = d.title,
                    onClick = { selected = d.id },
                    variant = if (d.id == selected) KButtonVariant.Default else KButtonVariant.Outline,
                )
            }
        }
        DemoBox(selected)
    }
}

@Composable
internal fun Pill(text: String, modifier: Modifier = Modifier, strong: Boolean = false, dim: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = if (strong) colors.onPrimary else if (dim) colors.onSurfaceVariant.copy(alpha = 0.6f) else colors.onSurfaceVariant,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .then(if (strong) Modifier.background(colors.primary) else Modifier.border(1.dp, colors.outline, RoundedCornerShape(6.dp)))
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

@Composable
private fun DeclGroupView(index: Int, group: DeclGroup, ctx: RenderCtx) {
    RenderBlock(index, Heading(2, group.title), ctx)
    group.decls.forEachIndexed { j, d ->
        if (j > 0) HorizontalDivider(Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outline)
        DeclView(index * 1000 + j, d, ctx)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeclView(key: Int, decl: Decl, ctx: RenderCtx) {
    val colors = MaterialTheme.colorScheme
    RenderBlock(key, Heading(3, decl.name), ctx)
    FlowRow(
        Modifier.padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val expect = decl.impls.any { it.kind == "expect" }
        if (decl.universal) {
            Pill(if (decl.common) "Common" else "All platforms", strong = true)
            if (expect) Pill("expect / actual")
        } else {
            decl.platforms.forEach { Pill(platformLabel(it), strong = ctx.platform == it) }
        }
    }
    decl.children.forEachIndexed { k, c -> RenderBlock(key * 1000 + k, c, ctx) }
    if (decl.impls.isNotEmpty()) {
        FlowRow(
            Modifier.padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(if (decl.impls.size > 1) "Implementations" else "Source", fontSize = 13.sp, color = colors.onSurfaceVariant)
            decl.impls.forEach { impl ->
                val hit = ctx.platform == null || (ctx.platform == "common" && impl.sourceSet == "commonMain") || ctx.platform in impl.covers
                val label = impl.sourceSet + if (impl.kind.isNotEmpty()) " · ${impl.kind}" else ""
                Text(
                    label,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (hit) colors.onBackground else colors.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, if (hit) colors.onSurfaceVariant else colors.outline, RoundedCornerShape(6.dp))
                        .clickable { ctx.onLink(ctx.fill(impl.url)) }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}
