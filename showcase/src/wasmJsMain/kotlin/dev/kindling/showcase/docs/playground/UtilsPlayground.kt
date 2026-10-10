package dev.kindling.showcase.docs.playground

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.ui.KInput
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonSize
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.utils.method.KDebouncer
import dev.kindling.utils.method.KThrottler
import dev.kindling.utils.method.format.communication.formatUsPhone
import dev.kindling.utils.method.format.communication.isValidUsPhone
import dev.kindling.utils.method.format.communication.maskPhone
import dev.kindling.utils.method.format.communication.toE164
import dev.kindling.utils.method.format.financial.bpsToHuman
import dev.kindling.utils.method.format.financial.centsToPrice
import dev.kindling.utils.method.format.financial.discountLabel
import dev.kindling.utils.method.format.financial.toCompactPrice
import dev.kindling.utils.method.format.financial.toPrice
import dev.kindling.utils.method.format.financial.withTax
import dev.kindling.utils.method.format.network.httpCategory
import dev.kindling.utils.method.format.network.isHttpError
import dev.kindling.utils.method.format.network.toHttpStatus
import dev.kindling.utils.method.format.number.clamp
import dev.kindling.utils.method.format.number.mapRange
import dev.kindling.utils.method.format.number.round
import dev.kindling.utils.method.format.number.toChangeLabel
import dev.kindling.utils.method.format.number.toCompact
import dev.kindling.utils.method.format.number.toPercent
import dev.kindling.utils.method.format.number.toThousands
import dev.kindling.utils.method.format.number.withSign
import dev.kindling.utils.method.format.number.zeroPad
import dev.kindling.utils.method.format.system.bytesToHuman
import dev.kindling.utils.method.format.system.bytesToHumanSi
import dev.kindling.utils.method.format.system.fromBase64
import dev.kindling.utils.method.format.system.toBase64
import dev.kindling.utils.method.format.text.camelToTitle
import dev.kindling.utils.method.format.text.collapseWhitespace
import dev.kindling.utils.method.format.text.toAce
import dev.kindling.utils.method.format.text.toCamelCase
import dev.kindling.utils.method.format.text.toInitials
import dev.kindling.utils.method.format.text.toKebabCase
import dev.kindling.utils.method.format.text.toSnakeCase
import dev.kindling.utils.method.format.text.toTitleCase
import dev.kindling.utils.method.format.text.truncate
import dev.kindling.utils.method.format.text.truncateMiddle
import dev.kindling.utils.method.format.text.wordWrap
import dev.kindling.utils.method.format.time.hhMmSsToSeconds
import dev.kindling.utils.method.format.time.minutesToHuman
import dev.kindling.utils.method.format.time.msToHuman
import dev.kindling.utils.method.format.time.secondsToHhMmSs
import dev.kindling.utils.method.format.time.secondsToHuman
import dev.kindling.utils.method.format.unit.celsiusToFahrenheit
import dev.kindling.utils.method.format.unit.cmToFeetInches
import dev.kindling.utils.method.format.unit.fahrenheitToCelsius
import dev.kindling.utils.method.format.unit.kgToLbs
import dev.kindling.utils.method.format.unit.kmToMiles
import dev.kindling.utils.method.format.unit.litersToGallons
import dev.kindling.utils.method.format.unit.milesToKm
import dev.kindling.utils.method.format.unit.mphToKph
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Model ──────────────────────────────────────────────────────────────────────

private enum class Kind { Text, Number, Toggle, Choice }

private class P(val key: String, val label: String, val kind: Kind, val default: String, val options: List<String> = emptyList())

private typealias V = Map<String, String>

private fun V.s(k: String) = this[k] ?: ""
private fun V.d(k: String) = this[k]?.trim()?.toDoubleOrNull() ?: 0.0
private fun V.i(k: String) = this[k]?.trim()?.toIntOrNull() ?: d(k).toInt()
private fun V.l(k: String) = this[k]?.trim()?.toLongOrNull() ?: d(k).toLong()
private fun V.b(k: String) = this[k] == "true"

/** Kotlin string literal. */
private fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$").replace("\n", "\\n") + "\""

/** Double literal as the user would write it (`1234` -> `1234.0`). */
private fun V.dl(k: String): String {
    val t = this[k]?.trim().orEmpty()
    val n = t.toDoubleOrNull() ?: return "0.0"
    return if ('.' in t || 'e' in t.lowercase()) t else "$t.0"
}

private fun V.il(k: String) = "${i(k)}"
private fun V.ll(k: String) = "${l(k)}L"

/**
 * One playground entry. [call] returns the Kotlin expression shown in the code panel; [run] calls the real library.
 * The import line is derived from [pkg] and the function name in [signature].
 */
private class Fn(
    val group: String,
    val signature: String,
    val summary: String,
    val pkg: String,
    val params: List<P>,
    val run: (V) -> String,
    val call: (V) -> String,
) {
    val member = signature.substringAfterLast('.').substringBefore('(')
    val id = signature
}

private fun text(key: String, label: String, default: String) = P(key, label, Kind.Text, default)
private fun num(key: String, label: String, default: String) = P(key, label, Kind.Number, default)
private fun toggle(key: String, label: String, default: Boolean) = P(key, label, Kind.Toggle, default.toString())
private fun choice(key: String, label: String, default: String, vararg options: String) =
    P(key, label, Kind.Choice, default, options.toList())

private const val TEXT = "dev.kindling.utils.method.format.text"
private const val NUM = "dev.kindling.utils.method.format.number"
private const val FIN = "dev.kindling.utils.method.format.financial"
private const val COM = "dev.kindling.utils.method.format.communication"
private const val SYS = "dev.kindling.utils.method.format.system"
private const val UNIT = "dev.kindling.utils.method.format.unit"
private const val TIME = "dev.kindling.utils.method.format.time"
private const val NET = "dev.kindling.utils.method.format.network"

private val functions: List<Fn> = listOf(
    // Text
    Fn("Text", "String.toTitleCase()", "Converts a string to title case.", TEXT,
        listOf(text("v", "Text", "hello wORLD from kindling")), { it.s("v").toTitleCase() }, { "${q(it.s("v"))}.toTitleCase()" }),
    Fn("Text", "String.toSnakeCase()", "Converts any string to snake_case.", TEXT,
        listOf(text("v", "Text", "Hello World From Kindling")), { it.s("v").toSnakeCase() }, { "${q(it.s("v"))}.toSnakeCase()" }),
    Fn("Text", "String.toKebabCase()", "Converts any string to kebab-case.", TEXT,
        listOf(text("v", "Text", "Hello World From Kindling")), { it.s("v").toKebabCase() }, { "${q(it.s("v"))}.toKebabCase()" }),
    Fn("Text", "String.toCamelCase()", "Converts any string to camelCase.", TEXT,
        listOf(text("v", "Text", "hello world from kindling")), { it.s("v").toCamelCase() }, { "${q(it.s("v"))}.toCamelCase()" }),
    Fn("Text", "String.camelToTitle()", "Turns camelCase / PascalCase into a readable label.", TEXT,
        listOf(text("v", "Text", "firstNameAndLastName")), { it.s("v").camelToTitle() }, { "${q(it.s("v"))}.camelToTitle()" }),
    Fn("Text", "String.toInitials(maxLetters)", "Extracts initials from a full name.", TEXT,
        listOf(text("v", "Name", "Jean-Claude Van Damme"), num("n", "maxLetters", "3")),
        { it.s("v").toInitials(it.i("n")) }, { "${q(it.s("v"))}.toInitials(maxLetters = ${it.il("n")})" }),
    Fn("Text", "String.truncate(maxLength, ellipsis)", "Cuts a string and appends an ellipsis.", TEXT,
        listOf(text("v", "Text", "Kindling is a Compose component library"), num("n", "maxLength", "20"), text("e", "ellipsis", "...")),
        { it.s("v").truncate(it.i("n"), it.s("e")) }, { "${q(it.s("v"))}.truncate(maxLength = ${it.il("n")}, ellipsis = ${q(it.s("e"))})" }),
    Fn("Text", "String.truncateMiddle(maxLength, ellipsis)", "Keeps the start and the end, e.g. for file names.", TEXT,
        listOf(text("v", "Text", "quarterly-financial-report-final-v2.pdf"), num("n", "maxLength", "24"), text("e", "ellipsis", "...")),
        { it.s("v").truncateMiddle(it.i("n"), it.s("e")) }, { "${q(it.s("v"))}.truncateMiddle(maxLength = ${it.il("n")}, ellipsis = ${q(it.s("e"))})" }),
    Fn("Text", "String.collapseWhitespace()", "Collapses runs of whitespace to a single space.", TEXT,
        listOf(text("v", "Text", "too    many     spaces   here")), { it.s("v").collapseWhitespace() }, { "${q(it.s("v"))}.collapseWhitespace()" }),
    Fn("Text", "String.wordWrap(lineWidth)", "Wraps text at spaces to the given width.", TEXT,
        listOf(text("v", "Text", "The quick brown fox jumps over the lazy dog"), num("n", "lineWidth", "16")),
        { it.s("v").wordWrap(it.i("n")) }, { "${q(it.s("v"))}.wordWrap(lineWidth = ${it.il("n")})" }),
    Fn("Text", "String.toAce()", "Unicode domain to ASCII-compatible (punycode) form.", TEXT,
        listOf(text("v", "Domain", "münchen.de")), { it.s("v").toAce() }, { "${q(it.s("v"))}.toAce()" }),

    // Numbers
    Fn("Number", "Double.round(decimals)", "Formats a Double with a fixed number of decimals.", NUM,
        listOf(num("v", "Value", "3.14159"), num("n", "decimals", "2")), { it.d("v").round(it.i("n")) }, { "${it.dl("v")}.round(decimals = ${it.il("n")})" }),
    Fn("Number", "Double.toThousands(decimals)", "Thousands separators with fixed decimals.", NUM,
        listOf(num("v", "Value", "1234567.891"), num("n", "decimals", "2")), { it.d("v").toThousands(it.i("n")) }, { "${it.dl("v")}.toThousands(decimals = ${it.il("n")})" }),
    Fn("Number", "Double.toCompact(decimals)", "Abbreviates with K / M / B / T.", NUM,
        listOf(num("v", "Value", "1500000"), num("n", "decimals", "1")), { it.d("v").toCompact(it.i("n")) }, { "${it.dl("v")}.toCompact(decimals = ${it.il("n")})" }),
    Fn("Number", "Double.toPercent(decimals)", "Formats a 0.0 to 1.0 ratio as a percentage.", NUM,
        listOf(num("v", "Ratio", "0.4231"), num("n", "decimals", "1")), { it.d("v").toPercent(it.i("n")) }, { "${it.dl("v")}.toPercent(decimals = ${it.il("n")})" }),
    Fn("Number", "Double.toChangeLabel(previous, decimals)", "Signed percentage change versus a previous value.", NUM,
        listOf(num("v", "Current", "120"), num("p", "previous", "100"), num("n", "decimals", "1")),
        { it.d("v").toChangeLabel(it.d("p"), it.i("n")) }, { "${it.dl("v")}.toChangeLabel(previous = ${it.dl("p")}, decimals = ${it.il("n")})" }),
    Fn("Number", "Double.withSign(decimals)", "Formats a number with an explicit + or - prefix.", NUM,
        listOf(num("v", "Value", "42"), num("n", "decimals", "0")), { it.d("v").withSign(it.i("n")) }, { "${it.dl("v")}.withSign(decimals = ${it.il("n")})" }),
    Fn("Number", "Int.zeroPad(width)", "Pads an integer with leading zeros.", NUM,
        listOf(num("v", "Value", "7"), num("n", "width", "3")), { it.i("v").zeroPad(it.i("n")).toString() }, { "${it.il("v")}.zeroPad(width = ${it.il("n")})" }),
    Fn("Number", "Double.clamp(min, max)", "Limits a value to a range.", NUM,
        listOf(num("v", "Value", "150"), num("a", "min", "0"), num("b", "max", "100")),
        { it.d("v").clamp(it.d("a"), it.d("b")).toString() }, { "${it.dl("v")}.clamp(min = ${it.dl("a")}, max = ${it.dl("b")})" }),
    Fn("Number", "Double.mapRange(fromMin, fromMax, toMin, toMax)", "Linearly maps a value from one range to another.", NUM,
        listOf(num("v", "Value", "50"), num("a", "fromMin", "0"), num("b", "fromMax", "100"), num("c", "toMin", "0"), num("e", "toMax", "1")),
        { it.d("v").mapRange(it.d("a"), it.d("b"), it.d("c"), it.d("e")).toString() },
        { "${it.dl("v")}.mapRange(${it.dl("a")}, ${it.dl("b")}, ${it.dl("c")}, ${it.dl("e")})" }),

    // Money
    Fn("Money", "Double.toPrice(symbol, decimals, thousandsSep, decimalSep, symbolAfter)", "Formats a price with a currency symbol.", FIN,
        listOf(num("v", "Amount", "1234567.5"), text("s", "symbol", "$"), num("n", "decimals", "2"), text("t", "thousandsSep", ","), text("d", "decimalSep", "."), toggle("a", "symbolAfter", false)),
        { it.d("v").toPrice(it.s("s"), it.i("n"), it.s("t"), it.s("d"), it.b("a")) },
        { "${it.dl("v")}.toPrice(symbol = ${q(it.s("s"))}, decimals = ${it.il("n")}, thousandsSep = ${q(it.s("t"))}, decimalSep = ${q(it.s("d"))}, symbolAfter = ${it.b("a")})" }),
    Fn("Money", "Double.toCompactPrice(symbol)", "Compact price for large amounts.", FIN,
        listOf(num("v", "Amount", "1500000"), text("s", "symbol", "$")), { it.d("v").toCompactPrice(it.s("s")) }, { "${it.dl("v")}.toCompactPrice(symbol = ${q(it.s("s"))})" }),
    Fn("Money", "Long.centsToPrice(symbol, thousandsSep)", "Minor currency units (cents) to a price string.", FIN,
        listOf(num("v", "Cents", "123456"), text("s", "symbol", "$"), text("t", "thousandsSep", ",")),
        { it.l("v").centsToPrice(it.s("s"), it.s("t")) }, { "${it.ll("v")}.centsToPrice(symbol = ${q(it.s("s"))}, thousandsSep = ${q(it.s("t"))})" }),
    Fn("Money", "Double.discountLabel(salePrice, decimals)", "Discount label such as \"-20%\".", FIN,
        listOf(num("v", "Original price", "100"), num("p", "salePrice", "80"), num("n", "decimals", "0")),
        { it.d("v").discountLabel(it.d("p"), it.i("n")) }, { "${it.dl("v")}.discountLabel(salePrice = ${it.dl("p")}, decimals = ${it.il("n")})" }),
    Fn("Money", "Double.withTax(rate)", "Adds a tax / VAT rate (0.20 = 20%).", FIN,
        listOf(num("v", "Price", "100"), num("r", "rate", "0.2")), { it.d("v").withTax(it.d("r")).toString() }, { "${it.dl("v")}.withTax(rate = ${it.dl("r")})" }),
    Fn("Money", "Double.bpsToHuman(decimals)", "Bits per second as a readable bandwidth.", FIN,
        listOf(num("v", "bits/s", "125000000"), num("n", "decimals", "1")), { it.d("v").bpsToHuman(it.i("n")) }, { "${it.dl("v")}.bpsToHuman(decimals = ${it.il("n")})" }),

    // Phone
    Fn("Phone", "String.formatUsPhone()", "Formats 10 digits as (XXX) XXX-XXXX.", COM,
        listOf(text("v", "Phone", "8005551234")), { it.s("v").formatUsPhone() }, { "${q(it.s("v"))}.formatUsPhone()" }),
    Fn("Phone", "String.maskPhone(visible)", "Hides all but the last digits.", COM,
        listOf(text("v", "Phone", "8005551234"), num("n", "visible", "4")), { it.s("v").maskPhone(it.i("n")) }, { "${q(it.s("v"))}.maskPhone(visible = ${it.il("n")})" }),
    Fn("Phone", "String.toE164(countryCode)", "International E.164 format.", COM,
        listOf(text("v", "Phone", "(800) 555-1234"), text("c", "countryCode", "1")), { it.s("v").toE164(it.s("c")) }, { "${q(it.s("v"))}.toE164(countryCode = ${q(it.s("c"))})" }),
    Fn("Phone", "String.isValidUsPhone()", "Does it look like a valid 10-digit US number?", COM,
        listOf(text("v", "Phone", "800-555-1234")), { it.s("v").isValidUsPhone().toString() }, { "${q(it.s("v"))}.isValidUsPhone()" }),

    // System
    Fn("System", "Long.bytesToHuman(decimals)", "Binary units (KiB, MiB, GiB).", SYS,
        listOf(num("v", "Bytes", "1536000"), num("n", "decimals", "1")), { it.l("v").bytesToHuman(it.i("n")) }, { "${it.ll("v")}.bytesToHuman(decimals = ${it.il("n")})" }),
    Fn("System", "Long.bytesToHumanSi(decimals)", "Decimal SI units (kB, MB, GB).", SYS,
        listOf(num("v", "Bytes", "1536000"), num("n", "decimals", "1")), { it.l("v").bytesToHumanSi(it.i("n")) }, { "${it.ll("v")}.bytesToHumanSi(decimals = ${it.il("n")})" }),
    Fn("System", "ByteArray.toBase64()", "Encodes bytes (here: the text as UTF-8) to Base64.", SYS,
        listOf(text("v", "Text", "Hello, Kindling!")), { it.s("v").encodeToByteArray().toBase64() }, { "${q(it.s("v"))}.encodeToByteArray().toBase64()" }),
    Fn("System", "String.fromBase64()", "Decodes Base64 back to bytes (shown as UTF-8 text).", SYS,
        listOf(text("v", "Base64", "SGVsbG8sIEtpbmRsaW5nIQ==")),
        { runCatching { it.s("v").fromBase64().decodeToString() }.getOrElse { e -> "Invalid Base64: ${e.message}" } }, { "${q(it.s("v"))}.fromBase64().decodeToString()" }),

    // Units
    Fn("Units", "Double.celsiusToFahrenheit()", "Temperature conversion.", UNIT,
        listOf(num("v", "Celsius", "100")), { it.d("v").celsiusToFahrenheit().toString() }, { "${it.dl("v")}.celsiusToFahrenheit()" }),
    Fn("Units", "Double.fahrenheitToCelsius()", "Temperature conversion.", UNIT,
        listOf(num("v", "Fahrenheit", "212")), { it.d("v").fahrenheitToCelsius().toString() }, { "${it.dl("v")}.fahrenheitToCelsius()" }),
    Fn("Units", "Double.kmToMiles()", "Distance conversion.", UNIT,
        listOf(num("v", "Kilometres", "42.195")), { it.d("v").kmToMiles().toString() }, { "${it.dl("v")}.kmToMiles()" }),
    Fn("Units", "Double.milesToKm()", "Distance conversion.", UNIT,
        listOf(num("v", "Miles", "26.2")), { it.d("v").milesToKm().toString() }, { "${it.dl("v")}.milesToKm()" }),
    Fn("Units", "Double.kgToLbs()", "Weight conversion.", UNIT,
        listOf(num("v", "Kilograms", "75")), { it.d("v").kgToLbs().toString() }, { "${it.dl("v")}.kgToLbs()" }),
    Fn("Units", "Double.cmToFeetInches()", "Height as feet and inches.", UNIT,
        listOf(num("v", "Centimetres", "178")), { it.d("v").cmToFeetInches() }, { "${it.dl("v")}.cmToFeetInches()" }),
    Fn("Units", "Double.litersToGallons()", "Volume conversion (US gallons).", UNIT,
        listOf(num("v", "Litres", "10")), { it.d("v").litersToGallons().toString() }, { "${it.dl("v")}.litersToGallons()" }),
    Fn("Units", "Double.mphToKph()", "Speed conversion.", UNIT,
        listOf(num("v", "mph", "60")), { it.d("v").mphToKph().toString() }, { "${it.dl("v")}.mphToKph()" }),

    // Time
    Fn("Time", "Long.secondsToHhMmSs()", "Seconds as HH:MM:SS.", TIME,
        listOf(num("v", "Seconds", "3661")), { it.l("v").secondsToHhMmSs() }, { "${it.ll("v")}.secondsToHhMmSs()" }),
    Fn("Time", "Long.secondsToHuman()", "Seconds as a readable duration.", TIME,
        listOf(num("v", "Seconds", "3750")), { it.l("v").secondsToHuman() }, { "${it.ll("v")}.secondsToHuman()" }),
    Fn("Time", "Long.msToHuman()", "Milliseconds as a readable duration.", TIME,
        listOf(num("v", "Milliseconds", "90500")), { it.l("v").msToHuman() }, { "${it.ll("v")}.msToHuman()" }),
    Fn("Time", "Int.minutesToHuman()", "Minutes as \"Xh Ym\".", TIME,
        listOf(num("v", "Minutes", "90")), { it.i("v").minutesToHuman() }, { "${it.il("v")}.minutesToHuman()" }),
    Fn("Time", "String.hhMmSsToSeconds()", "Parses HH:MM:SS to total seconds.", TIME,
        listOf(text("v", "Duration", "01:02:03")), { it.s("v").hhMmSsToSeconds().toString() }, { "${q(it.s("v"))}.hhMmSsToSeconds()" }),

    // HTTP
    Fn("HTTP", "Int.toHttpStatus()", "Status code with its reason phrase.", NET,
        listOf(num("v", "Status code", "404")), { it.i("v").toHttpStatus() }, { "${it.il("v")}.toHttpStatus()" }),
    Fn("HTTP", "Int.httpCategory()", "Category of an HTTP status code.", NET,
        listOf(num("v", "Status code", "503")), { it.i("v").httpCategory() }, { "${it.il("v")}.httpCategory()" }),
    Fn("HTTP", "Int.isHttpError()", "True for 4xx and 5xx codes.", NET,
        listOf(num("v", "Status code", "418")), { it.i("v").isHttpError().toString() }, { "${it.il("v")}.isHttpError()" }),
)

private val groups = listOf("All") + functions.map { it.group }.distinct()

// ── UI ─────────────────────────────────────────────────────────────────────────

@Composable
internal fun UtilsPlayground(siteDark: Boolean) {
    var tab by remember { mutableStateOf("Formatters") }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip("Formatters", selected = tab == "Formatters") { tab = "Formatters" }
            Chip("Debounce & throttle", selected = tab == "Async") { tab = "Async" }
        }
        if (tab == "Formatters") FormatterLab(siteDark) else AsyncLab(siteDark)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormatterLab(siteDark: Boolean) {
    val colors = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("All") }
    var selectedId by remember { mutableStateOf(functions.first().id) }

    val visible = remember(query, group) {
        val q = query.trim().lowercase()
        functions.filter { (group == "All" || it.group == group) && (q.isEmpty() || it.signature.lowercase().contains(q) || it.summary.lowercase().contains(q)) }
    }
    val fn = functions.first { it.id == selectedId }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val wide = maxWidth >= 900.dp
        val list: @Composable (Modifier) -> Unit = { mod ->
            Column(mod, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KInput(value = query, onValueChange = { query = it }, placeholder = "Search ${functions.size} functions...", modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    groups.forEach { g -> Chip(g, selected = g == group) { group = g } }
                }
                Column(
                    Modifier
                        .heightIn(max = 520.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, colors.outline, RoundedCornerShape(10.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(6.dp),
                ) {
                    if (visible.isEmpty()) Text("No match.", fontSize = 13.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(10.dp))
                    visible.forEach { f ->
                        val active = f.id == selectedId
                        Text(
                            f.signature.substringBefore('('),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (active) colors.onBackground else colors.onSurfaceVariant,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) colors.secondaryContainer else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable { selectedId = f.id }
                                .pointerHoverIcon(PointerIcon.Hand)
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                        )
                    }
                }
            }
        }
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.Top) {
                list(Modifier.width(320.dp))
                FnDetail(fn, siteDark, Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                list(Modifier.fillMaxWidth())
                FnDetail(fn, siteDark, Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FnDetail(fn: Fn, siteDark: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val values = remember(fn.id) { mutableStateMapOf<String, String>().apply { fn.params.forEach { put(it.key, it.default) } } }
    val snapshot: V = values.toMap()
    val result = runCatching { fn.run(snapshot) }.getOrElse { "Error: ${it.message ?: it::class.simpleName}" }
    val code = remember(fn.id, snapshot) {
        "import ${fn.pkg}.${fn.member}\n\n// $result\nval result = ${fn.call(snapshot)}"
            .replace("// $result", "// => " + result.replace("\n", "\\n"))
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(fn.signature, fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.onBackground)
        Text(fn.summary, fontSize = 14.sp, color = colors.onSurfaceVariant)

        fn.params.forEach { p ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(p.label, fontSize = 12.sp, color = colors.onSurfaceVariant)
                when (p.kind) {
                    Kind.Text, Kind.Number -> KInput(
                        value = values[p.key] ?: "",
                        onValueChange = { values[p.key] = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Kind.Toggle -> Switch(checked = values[p.key] == "true", onCheckedChange = { values[p.key] = it.toString() })
                    Kind.Choice -> FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        p.options.forEach { o -> Chip(o, selected = values[p.key] == o) { values[p.key] = o } }
                    }
                }
            }
        }

        Text("RESULT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
        Text(
            result,
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            color = colors.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, colors.outline, RoundedCornerShape(10.dp))
                .padding(16.dp),
        )
        CodePanel(code = code, siteDark = siteDark, title = "Usage.kt", copyLabel = "Copy code")
    }
}

// ── Debounce / throttle lab (runs the real KDebouncer / KThrottler) ─────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AsyncLab(siteDark: Boolean) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    var delayMs by remember { mutableStateOf(500f) }
    var leading by remember { mutableStateOf(false) }
    var typed by remember { mutableStateOf("") }
    var events by remember { mutableStateOf(0) }
    var debounced by remember { mutableStateOf("") }
    var debounceFires by remember { mutableStateOf(0) }
    var throttled by remember { mutableStateOf("") }
    var throttleFires by remember { mutableStateOf(0) }

    val ms = delayMs.roundToInt()
    val debouncer = remember(ms, leading) {
        KDebouncer<String>(scope, ms.milliseconds, leading).also { d -> d.onDebounced { debounced = it; debounceFires++ } }
    }
    val throttler = remember(ms) {
        KThrottler<String>(scope, ms.milliseconds).also { t -> t.onThrottled { throttled = it; throttleFires++ } }
    }
    DisposableEffect(debouncer, throttler) { onDispose { debouncer.cancel(); throttler.cancel() } }

    fun fire(value: String) {
        events++
        debouncer.emit(value)
        throttler.emit(value)
    }

    val code = remember(ms, leading) {
        """
        // Debounce: wait until the input has been quiet for ${ms}ms
        val debouncer = KDebouncer<String>(scope, ${ms}.milliseconds, leading = $leading)
        debouncer.onDebounced { query -> search(query) }
        debouncer.emit(text)

        // Throttle: at most one value per ${ms}ms
        val throttler = KThrottler<String>(scope, ${ms}.milliseconds)
        throttler.onThrottled { position -> save(position) }
        throttler.emit(value)
        """.trimIndent()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            "Type in the box or spam events. The two outputs below come from the real KDebouncer and KThrottler.",
            fontSize = 14.sp, color = colors.onSurfaceVariant,
        )
        Text("QUIET PERIOD / INTERVAL: ${ms}MS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurfaceVariant)
        Slider(value = delayMs, onValueChange = { delayMs = it }, valueRange = 100f..2000f)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Switch(checked = leading, onCheckedChange = { leading = it })
            Text("Debouncer leading edge (emit the first value immediately)", fontSize = 13.sp, color = colors.onBackground)
        }
        KInput(value = typed, onValueChange = { typed = it; fire(it) }, placeholder = "Type here...", modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(
                text = "Spam 20 events",
                onClick = { scope.launch { repeat(20) { i -> fire("event ${i + 1}"); delay(50) } } },
                variant = KButtonVariant.Outline, size = KButtonSize.Sm,
            )
            KButton(
                text = "Reset counters",
                onClick = { events = 0; debounced = ""; debounceFires = 0; throttled = ""; throttleFires = 0 },
                variant = KButtonVariant.Ghost, size = KButtonSize.Sm,
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Stat("Raw events", events.toString(), "")
            Stat("Debounced", debounceFires.toString(), debounced)
            Stat("Throttled", throttleFires.toString(), throttled)
        }
        CodePanel(code = code, siteDark = siteDark, title = "Debounce.kt", copyLabel = "Copy code")
    }
}

@Composable
private fun Stat(title: String, count: String, last: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, fontSize = 12.sp, color = colors.onSurfaceVariant)
        Text(count, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
        if (last.isNotEmpty()) Text("last: $last", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = colors.onSurfaceVariant)
    }
}
