package dev.kindling.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.util.Locale

/**
 * KSP compiler symbol processor that scans functions annotated with [@KWidget]
 * and generates both the runtime rendering map and catalog metadata declarations.
 *
 * @property codeGenerator Generator utility for writing target source files.
 * @property logger System logging diagnostic handler.
 */
class KWidgetProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation("dev.kindling.core.components.dashboard.KWidget")
        val annotatedFunctions = symbols.filterIsInstance<KSFunctionDeclaration>().toList()

        val validFunctions = if (annotatedFunctions.isEmpty()) {
            emptyList()
        } else {
            val seenTypes = mutableSetOf<String>()
            annotatedFunctions.filter { isValidWidgetFunction(it, seenTypes) }
        }

        generateRegistryFile(resolver, validFunctions)
        generated = true
        return emptyList()
    }

    private fun isValidWidgetFunction(func: KSFunctionDeclaration, seenTypes: MutableSet<String>): Boolean {
        if (func.parentDeclaration != null) {
            logger.error("@KWidget function must be a top-level declaration.", func)
            return false
        }

        if (func.extensionReceiver != null) {
            logger.error("@KWidget function cannot be an extension function.", func)
            return false
        }

        if (func.parameters.isEmpty()) {
            logger.error("@KWidget function must accept parameters (at least a 'title' parameter).", func)
            return false
        }

        val titleParam = func.parameters.find { it.name?.asString() == "title" }
        if (titleParam == null) {
            logger.error("@KWidget function must have a parameter named 'title'.", func)
            return false
        }

        val resolvedType = titleParam.type.resolve()
        val typeName = resolvedType.declaration.qualifiedName?.asString()
        if (typeName != null && typeName != "kotlin.String") {
            logger.error("@KWidget function 'title' parameter must be of type String.", func)
            return false
        }

        val nonTitleParams = func.parameters.filter { it.name?.asString() != "title" }
        if (nonTitleParams.any { !it.hasDefault && !it.isVararg }) {
            logger.error("@KWidget function cannot have required parameters other than 'title'.", func)
            return false
        }

        val annotation = func.annotations.firstOrNull { it.shortName.asString() == "KWidget" }
        val typeValue = annotation?.arguments?.find { it.name?.asString() == "type" }?.value?.toString()
        if (typeValue.isNullOrBlank()) {
            logger.error("@KWidget annotation must define a non-blank 'type' value.", func)
            return false
        }

        if (!seenTypes.add(typeValue)) {
            logger.error("Duplicate @KWidget type value found: '$typeValue'.", func)
            return false
        }

        return true
    }

    private fun String.escapeKotlinLiteral(): String {
        return buildString {
            for (c in this@escapeKotlinLiteral) {
                when (c) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    '$' -> append("\\$")
                    else -> {
                        if (c.isISOControl()) {
                            append(String.format(Locale.ROOT, "\\u%04x", c.code))
                        } else {
                            append(c)
                        }
                    }
                }
            }
        }
    }

    private fun findFullyQualifiedName(resolver: Resolver, simpleName: String): String? {
        for (file in resolver.getAllFiles()) {
            val found = findInDeclarations(file.declarations, simpleName)
            if (found != null) return found
        }
        return null
    }

    private fun findInDeclarations(declarations: Sequence<KSDeclaration>, simpleName: String): String? {
        for (decl in declarations) {
            if (decl is KSClassDeclaration && decl.simpleName.asString() == simpleName) {
                return decl.qualifiedName?.asString()
            }
            if (decl is KSClassDeclaration) {
                val nested = findInDeclarations(decl.declarations, simpleName)
                if (nested != null) return nested
            }
        }
        return null
    }

    private fun generateRegistryFile(resolver: Resolver, functions: List<KSFunctionDeclaration>) {
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = "dev.kindling.generated",
            fileName = "KWidgetModuleInitializer"
        )

        file.bufferedWriter().use { writer ->
            writer.write("""
                package dev.kindling.generated

                import dev.kindling.core.components.ui.dashboard.KWidgetRegistry
                import dev.kindling.core.components.ui.dashboard.KWidgetMetadata

                // AUTO-GENERATED BY KINDLING KSP - DO NOT EDIT
                object KWidgetModuleInitializer {
                    init {
            """.trimIndent())

            functions.forEach { func ->
                val annotation = func.annotations.first { it.shortName.asString() == "KWidget" }
                val arguments = annotation.arguments.associate { it.name?.asString() to it.value }

                // Extract fields matching your exact @KWidget parameters
                val type = arguments["type"] as? String ?: error("Missing 'type' in @KWidget on ${func.simpleName}")
                val title = (arguments["title"] as? String)?.takeIf { it.isNotEmpty() } ?: type
                
                // KSP handles Array<String> values as List<String>
                @Suppress("UNCHECKED_CAST")
                val tagsList = (arguments["tags"] as? List<String>) ?: emptyList()
                val tagsArrayLiteral = tagsList.joinToString(prefix = "arrayOf(", postfix = ")") { "\"$it\"" }

                val icon = arguments["icon"] as? String ?: ""
                val iconParam = if (icon.isNotEmpty()) "\"$icon\"" else "null"

                val widthCells = arguments["widthCells"] as? Int ?: 1
                val heightCells = arguments["heightCells"] as? Int ?: 1
                val sizeStr = "${widthCells}x${heightCells}" // Or "${widthCells}*${heightCells}" depending on your format choice

                val pkg = func.packageName.asString()
                val funcName = func.simpleName.asString()

                writer.write("""
                    
                        KWidgetRegistry.register(
                            metadata = KWidgetMetadata(
                                type = "$type",
                                title = "$title",
                                tags = listOf($tagsArrayLiteral),
                                icon = $iconParam,
                                size = "$sizeStr"
                            )
                        ) { widget -> 
                            $pkg.$funcName(widget = widget) 
                        }
                """.trimIndent())
            }

            writer.write("\n    }\n}\n")
        }
    }
}

/**
 * Provider interface implementation for registering [KWidgetProcessor] with the KSP compiler.
 */
class KWidgetProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KWidgetProcessor(environment.codeGenerator, environment.logger)
    }
}