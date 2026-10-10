package dev.kindling.showcase.docs.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonSize
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.core.theme.KindlingColors
import dev.kindling.core.theme.KindlingShapes
import dev.kindling.core.theme.KindlingTheme
import dev.kindling.core.theme.kindlingColors
import dev.kindling.showcase.AvatarDemo
import dev.kindling.showcase.BadgeDemo
import dev.kindling.showcase.ButtonDemo
import dev.kindling.showcase.CardDemo
import dev.kindling.showcase.DialogDemo
import dev.kindling.showcase.EmptyDemo
import dev.kindling.showcase.InputDemo
import dev.kindling.showcase.PaginationDemo
import dev.kindling.showcase.SkeletonDemo
import dev.kindling.showcase.SpinnerDemo
import kotlin.math.roundToInt
import kotlin.random.Random

// ── Palette data (shadcn/ui neutral families + accent colours) ─────────────────

private class Neutrals(
    val background: Long, val foreground: Long, val card: Long, val muted: Long,
    val mutedForeground: Long, val border: Long, val secondary: Long,
)

private class BaseColor(val name: String, val light: Neutrals, val dark: Neutrals)

private val bases = listOf(
    BaseColor(
        "Neutral",
        Neutrals(0xFFFFFFFF, 0xFF0A0A0A, 0xFFFFFFFF, 0xFFF5F5F5, 0xFF737373, 0xFFE5E5E5, 0xFFF5F5F5),
        Neutrals(0xFF0A0A0A, 0xFFFAFAFA, 0xFF171717, 0xFF262626, 0xFFA3A3A3, 0xFF2E2E2E, 0xFF262626),
    ),
    BaseColor(
        "Zinc",
        Neutrals(0xFFFFFFFF, 0xFF09090B, 0xFFFFFFFF, 0xFFF4F4F5, 0xFF71717A, 0xFFE4E4E7, 0xFFF4F4F5),
        Neutrals(0xFF09090B, 0xFFFAFAFA, 0xFF18181B, 0xFF27272A, 0xFFA1A1AA, 0xFF2F2F33, 0xFF27272A),
    ),
    BaseColor(
        "Slate",
        Neutrals(0xFFFFFFFF, 0xFF020617, 0xFFFFFFFF, 0xFFF1F5F9, 0xFF64748B, 0xFFE2E8F0, 0xFFF1F5F9),
        Neutrals(0xFF020617, 0xFFF8FAFC, 0xFF0F172A, 0xFF1E293B, 0xFF94A3B8, 0xFF2B3649, 0xFF1E293B),
    ),
    BaseColor(
        "Stone",
        Neutrals(0xFFFFFFFF, 0xFF0C0A09, 0xFFFFFFFF, 0xFFF5F5F4, 0xFF78716C, 0xFFE7E5E4, 0xFFF5F5F4),
        Neutrals(0xFF0C0A09, 0xFFFAFAF9, 0xFF1C1917, 0xFF292524, 0xFFA8A29E, 0xFF3A3532, 0xFF292524),
    ),
    BaseColor(
        "Gray",
        Neutrals(0xFFFFFFFF, 0xFF030712, 0xFFFFFFFF, 0xFFF3F4F6, 0xFF6B7280, 0xFFE5E7EB, 0xFFF3F4F6),
        Neutrals(0xFF030712, 0xFFF9FAFB, 0xFF111827, 0xFF1F2937, 0xFF9CA3AF, 0xFF2A3441, 0xFF1F2937),
    ),
)

/** `null` primaries mean "use the base colour's own foreground", i.e. the black/white look. */
private class Accent(
    val name: String, val swatch: Long,
    val lightPrimary: Long?, val lightOn: Long?, val darkPrimary: Long?, val darkOn: Long?,
)

private val accents = listOf(
    Accent("Mono", 0xFF171717, null, null, null, null),
    Accent("Blue", 0xFF2563EB, 0xFF2563EB, 0xFFFFFFFF, 0xFF3B82F6, 0xFFFFFFFF),
    Accent("Violet", 0xFF7C3AED, 0xFF7C3AED, 0xFFFFFFFF, 0xFF8B5CF6, 0xFFFFFFFF),
    Accent("Rose", 0xFFE11D48, 0xFFE11D48, 0xFFFFFFFF, 0xFFF43F5E, 0xFFFFFFFF),
    Accent("Red", 0xFFDC2626, 0xFFDC2626, 0xFFFFFFFF, 0xFFEF4444, 0xFFFFFFFF),
    Accent("Orange", 0xFFEA580C, 0xFFEA580C, 0xFFFFFFFF, 0xFFF97316, 0xFF1C0A00),
    Accent("Yellow", 0xFFEAB308, 0xFFEAB308, 0xFF1C1917, 0xFFFACC15, 0xFF1C1917),
    Accent("Green", 0xFF16A34A, 0xFF16A34A, 0xFFFFFFFF, 0xFF22C55E, 0xFF052E16),
    Accent("Teal", 0xFF0D9488, 0xFF0D9488, 0xFFFFFFFF, 0xFF14B8A6, 0xFF042F2E),
)

/** `colors == null` means "derive from the Material 3 scheme" (`KindlingColors.fromMaterial3()`). */
private class ChartPreset(val name: String, val colors: List<Long>?)

private val chartPresets = listOf(
    ChartPreset("Auto", null),
    ChartPreset("Vibrant", listOf(0xFFE76E50, 0xFF2A9D90, 0xFF274754, 0xFFE8C468, 0xFFF4A462)),
    ChartPreset("Cool", listOf(0xFF3B82F6, 0xFF06B6D4, 0xFF6366F1, 0xFF14B8A6, 0xFF0EA5E9)),
    ChartPreset("Warm", listOf(0xFFEF4444, 0xFFF97316, 0xFFF59E0B, 0xFFEAB308, 0xFFDC2626)),
    ChartPreset("Mono", listOf(0xFF111111, 0xFF444444, 0xFF777777, 0xFFAAAAAA, 0xFFD4D4D4)),
    ChartPreset("Earth", listOf(0xFF8D6E63, 0xFFA1887F, 0xFFBCAAA4, 0xFF6D4C41, 0xFF4E342E)),
)

private val radii = listOf(0, 4, 8, 10, 14, 20)

private const val ERROR_LIGHT = 0xFFE7000BL
private const val ERROR_DARK = 0xFFFF6467L

// ── Config -> colour scheme + Kotlin code ──────────────────────────────────────

/** Ordered (parameter name, ARGB) pairs. Both the live preview and the generated code come from this list. */
private fun colorSpec(base: BaseColor, accent: Accent, dark: Boolean): List<Pair<String, Long>> {
    val n = if (dark) base.dark else base.light
    val primary = (if (dark) accent.darkPrimary else accent.lightPrimary) ?: n.foreground
    val onPrimary = (if (dark) accent.darkOn else accent.lightOn) ?: n.background
    return listOf(
        "primary" to primary,
        "onPrimary" to onPrimary,
        "primaryContainer" to primary,
        "onPrimaryContainer" to onPrimary,
        "secondary" to n.secondary,
        "onSecondary" to n.foreground,
        "secondaryContainer" to n.secondary,
        "onSecondaryContainer" to n.foreground,
        "background" to n.background,
        "onBackground" to n.foreground,
        "surface" to n.card,
        "onSurface" to n.foreground,
        "surfaceVariant" to n.muted,
        "onSurfaceVariant" to n.mutedForeground,
        "surfaceContainerLowest" to n.background,
        "surfaceContainerLow" to n.card,
        "surfaceContainer" to n.card,
        "surfaceContainerHigh" to n.muted,
        "surfaceContainerHighest" to n.muted,
        "outline" to n.border,
        "outlineVariant" to n.border,
        "error" to (if (dark) ERROR_DARK else ERROR_LIGHT),
        "onError" to n.background,
    )
}

private fun scheme(spec: List<Pair<String, Long>>, dark: Boolean): ColorScheme {
    val m = spec.toMap()
    fun c(k: String) = Color(m.getValue(k))
    return if (dark) {
        darkColorScheme(
            primary = c("primary"), onPrimary = c("onPrimary"),
            primaryContainer = c("primaryContainer"), onPrimaryContainer = c("onPrimaryContainer"),
            secondary = c("secondary"), onSecondary = c("onSecondary"),
            secondaryContainer = c("secondaryContainer"), onSecondaryContainer = c("onSecondaryContainer"),
            background = c("background"), onBackground = c("onBackground"),
            surface = c("surface"), onSurface = c("onSurface"),
            surfaceVariant = c("surfaceVariant"), onSurfaceVariant = c("onSurfaceVariant"),
            surfaceContainerLowest = c("surfaceContainerLowest"), surfaceContainerLow = c("surfaceContainerLow"),
            surfaceContainer = c("surfaceContainer"), surfaceContainerHigh = c("surfaceContainerHigh"),
            surfaceContainerHighest = c("surfaceContainerHighest"),
            outline = c("outline"), outlineVariant = c("outlineVariant"),
            error = c("error"), onError = c("onError"),
        )
    } else {
        lightColorScheme(
            primary = c("primary"), onPrimary = c("onPrimary"),
            primaryContainer = c("primaryContainer"), onPrimaryContainer = c("onPrimaryContainer"),
            secondary = c("secondary"), onSecondary = c("onSecondary"),
            secondaryContainer = c("secondaryContainer"), onSecondaryContainer = c("onSecondaryContainer"),
            background = c("background"), onBackground = c("onBackground"),
            surface = c("surface"), onSurface = c("onSurface"),
            surfaceVariant = c("surfaceVariant"), onSurfaceVariant = c("onSurfaceVariant"),
            surfaceContainerLowest = c("surfaceContainerLowest"), surfaceContainerLow = c("surfaceContainerLow"),
            surfaceContainer = c("surfaceContainer"), surfaceContainerHigh = c("surfaceContainerHigh"),
            surfaceContainerHighest = c("surfaceContainerHighest"),
            outline = c("outline"), outlineVariant = c("outlineVariant"),
            error = c("error"), onError = c("onError"),
        )
    }
}

private fun hex(v: Long) = "Color(0x" + (v and 0xFFFFFFFFL).toString(16).uppercase().padStart(8, '0') + ")"

private fun generateCode(base: BaseColor, accent: Accent, radius: Int, chart: ChartPreset): String {
    fun block(name: String, fn: String, dark: Boolean) = buildString {
        appendLine("val $name = $fn(")
        for ((k, v) in colorSpec(base, accent, dark)) appendLine("    $k = ${hex(v)},")
        append(")")
    }
    val chartArg = chart.colors?.let { cs ->
        "        colors = KindlingColors(\n" +
            cs.mapIndexed { i, c -> "            chart${i + 1} = ${hex(c)}," }.joinToString("\n") +
            "\n        ),\n"
    } ?: "        colors = KindlingColors.fromMaterial3(),\n"

    return buildString {
        appendLine("// Kindling theme: ${base.name} base, ${accent.name} accent, ${radius}dp radius")
        appendLine("import androidx.compose.foundation.isSystemInDarkTheme")
        appendLine("import androidx.compose.material3.darkColorScheme")
        appendLine("import androidx.compose.material3.lightColorScheme")
        appendLine("import androidx.compose.runtime.Composable")
        appendLine("import androidx.compose.ui.graphics.Color")
        appendLine("import androidx.compose.ui.unit.dp")
        appendLine("import dev.kindling.core.theme.KindlingColors")
        appendLine("import dev.kindling.core.theme.KindlingShapes")
        appendLine("import dev.kindling.core.theme.KindlingTheme")
        appendLine()
        appendLine(block("AppLightColors", "lightColorScheme", false))
        appendLine()
        appendLine(block("AppDarkColors", "darkColorScheme", true))
        appendLine()
        appendLine("@Composable")
        appendLine("fun AppTheme(")
        appendLine("    darkTheme: Boolean = isSystemInDarkTheme(),")
        appendLine("    content: @Composable () -> Unit,")
        appendLine(") {")
        appendLine("    KindlingTheme(")
        appendLine("        colorScheme = if (darkTheme) AppDarkColors else AppLightColors,")
        appendLine("        shapes = KindlingShapes(base = ${radius}.dp),")
        append(chartArg)
        appendLine("    ) { content() }")
        append("}")
    }
}

// ── UI ─────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ThemePlayground(siteDark: Boolean) {
    var base by remember { mutableStateOf(bases.first()) }
    var accent by remember { mutableStateOf(accents.first()) }
    var radius by remember { mutableStateOf(10) }
    var chart by remember { mutableStateOf(chartPresets.first()) }
    var dark by remember { mutableStateOf(false) }

    val code = remember(base, accent, radius, chart) { generateCode(base, accent, radius, chart) }
    val previewScheme = remember(base, accent, dark) { scheme(colorSpec(base, accent, dark), dark) }
    val previewColors = chart.colors?.let { cs ->
        KindlingColors(Color(cs[0]), Color(cs[1]), Color(cs[2]), Color(cs[3]), Color(cs[4]))
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val wide = maxWidth >= 900.dp
        val controls: @Composable (Modifier) -> Unit = { mod ->
            Controls(
                modifier = mod,
                base = base, onBase = { base = it },
                accent = accent, onAccent = { accent = it },
                radius = radius, onRadius = { radius = it },
                chart = chart, onChart = { chart = it },
                dark = dark, onDark = { dark = it },
                onReset = {
                    base = bases.first(); accent = accents.first(); radius = 10; chart = chartPresets.first()
                },
                onShuffle = {
                    base = bases.random(); accent = accents.random(); radius = radii.random(); chart = chartPresets.random()
                },
            )
        }
        val preview: @Composable (Modifier) -> Unit = { mod ->
            Column(mod, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // The outer MaterialTheme makes "Auto" chart colours derive from the previewed scheme, not the site's.
                MaterialTheme(colorScheme = previewScheme) {
                    KindlingTheme(
                        colorScheme = previewScheme,
                        shapes = KindlingShapes(base = radius.dp),
                        colors = previewColors ?: KindlingColors.fromMaterial3(),
                    ) {
                        PreviewSurface()
                    }
                }
                CodePanel(code = code, siteDark = siteDark, title = "Theme.kt", copyLabel = "Copy theme")
            }
        }
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.Top) {
                controls(Modifier.widthIn(max = 320.dp).fillMaxWidth(0.3f))
                preview(Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                controls(Modifier.fillMaxWidth())
                preview(Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Controls(
    modifier: Modifier,
    base: BaseColor, onBase: (BaseColor) -> Unit,
    accent: Accent, onAccent: (Accent) -> Unit,
    radius: Int, onRadius: (Int) -> Unit,
    chart: ChartPreset, onChart: (ChartPreset) -> Unit,
    dark: Boolean, onDark: (Boolean) -> Unit,
    onReset: () -> Unit, onShuffle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text("Customize", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.onBackground)

        ControlLabel("MODE")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip("Light", selected = !dark) { onDark(false) }
            Chip("Dark", selected = dark) { onDark(true) }
        }

        ControlLabel("BASE COLOR")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            bases.forEach { Chip(it.name, selected = it === base) { onBase(it) } }
        }

        ControlLabel("ACCENT: ${accent.name.uppercase()}")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            accents.forEach { a ->
                val selected = a === accent
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(a.swatch))
                        .border(if (selected) 3.dp else 1.dp, if (selected) colors.onBackground else colors.outline, CircleShape)
                        .clickable { onAccent(a) }
                        .pointerHoverIcon(PointerIcon.Hand),
                )
            }
        }

        ControlLabel("RADIUS: ${radius}DP")
        Slider(value = radius.toFloat(), onValueChange = { onRadius(it.roundToInt()) }, valueRange = 0f..24f)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            radii.forEach { Chip("$it", selected = it == radius) { onRadius(it) } }
        }

        ControlLabel("CHART COLORS")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            chartPresets.forEach { Chip(it.name, selected = it === chart) { onChart(it) } }
        }
        chart.colors?.let { cs ->
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                cs.forEach { Box(Modifier.size(18.dp).clip(CircleShape).background(Color(it))) }
            }
        }

        Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Shuffle", onClick = onShuffle, variant = KButtonVariant.Outline, size = KButtonSize.Sm)
            KButton(text = "Reset", onClick = onReset, variant = KButtonVariant.Ghost, size = KButtonSize.Sm)
        }
    }
}

/** Everything inside runs under the previewed [KindlingTheme], independent of the site's own theme. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewSurface() {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.background,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        FlowRow(
            Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Tile("Buttons", 380.dp) { ButtonDemo() }
            Tile("Card", 380.dp) { CardDemo() }
            Tile("Badges", 380.dp) { BadgeDemo() }
            Tile("Inputs", 380.dp) { InputDemo() }
            Tile("Avatars", 240.dp) { AvatarDemo() }
            Tile("Spinner", 240.dp) { SpinnerDemo() }
            Tile("Dialog", 240.dp) { DialogDemo() }
            Tile("Pagination", 380.dp) { PaginationDemo() }
            Tile("Skeleton", 380.dp) { SkeletonDemo() }
            Tile("Empty state", 380.dp) { EmptyDemo() }
            Tile("Chart palette", 380.dp) { ChartStrip() }
        }
    }
}

@Composable
private fun Tile(title: String, width: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .widthIn(min = width, max = width)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(10.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.onSurfaceVariant)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun ChartStrip() {
    val k = MaterialTheme.kindlingColors
    val bars = listOf(k.chart1 to 56.dp, k.chart2 to 84.dp, k.chart3 to 40.dp, k.chart4 to 72.dp, k.chart5 to 96.dp)
    Row(
        Modifier.height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { (c, h) ->
            Box(Modifier.size(width = 36.dp, height = h).clip(RoundedCornerShape(6.dp)).background(c))
        }
    }
}
