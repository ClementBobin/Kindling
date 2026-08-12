package dev.kindling.core.components.ui.dashboard

import kotlinx.serialization.Serializable

/**
 * Annotation marker applied directly to Composable widget functions to register them
 * into the project's central routing registry and metadata catalog via KSP generation.
 *
 * @property type Unique string key matching incoming [KWidgetModel.type] parameters.
 * @property title Default display name of the widget for catalog search listings.
 * @property tags Categorization tags for filtering.
 * @property icon Optional icon reference string.
 * @property widthCells Default horizontal grid cell span.
 * @property heightCells Default vertical grid cell span.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class KWidget(
    val type: String,
    val title: String = "",
    val tags: Array<String> = [],
    val icon: String = "",
    val widthCells: Int = 1,
    val heightCells: Int = 1
)

/**
 * Metadata descriptor representing a registered widget available in the application catalog.
 *
 * @property type Routing type identifier string.
 * @property title User-friendly display title.
 * @property tags Category classification keywords.
 * @property icon Visual asset reference identifier or path.
 * @property size Formatted cell dimension footprint string (e.g., "2*1").
 */
data class KWidgetMetadata(
    val type: String,
    val title: String,
    val tags: List<String>,
    val icon: String?,
    val size: String
)

/**
 * Data blueprint representing a persistent layout node model saved to disk storage.
 *
 * @property id Unique string identifier for the widget runtime instance.
 * @property type String routing key linking this data model to its registered Composable function.
 * @property tags Array of string tags for categorizing the widget.
 * @property icon String path or name reference to the widget's icon resource.
 * @property title Display text header assigned to the widget view instance.
 * @property initialColumn Grid column positioning coordinate.
 * @property initialRow Grid row positioning coordinate.
 * @property widthCells Column width size span.
 * @property heightCells Row height size span.
 */
@Serializable
data class KWidgetModel(
    val id: String,
    val type: String,
    val tags: Array<String>,
    val icon: String?,
    val title: String,
    val initialColumn: Int,
    val initialRow: Int,
    val widthCells: Int = 1,
    val heightCells: Int = 1
)