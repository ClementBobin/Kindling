package dev.kindling.core.components.dashboard

/**
 * Utility manager for querying, listing, and searching through available catalog widgets
 * populated by the KSP-generated metadata catalog.
 */
object KWidgetCatalogManager {

    /**
     * Retrieves all available catalog widgets, optionally restricted by a maximum count limit.
     *
     * @param limit Maximum number of records to return. Defaults to [Int.MAX_VALUE].
     * @return List of [KWidgetMetadata] available in the system.
     */
    fun listAll(limit: Int = Int.MAX_VALUE): List<KWidgetMetadata> {
        return GeneratedWidgetCatalog.take(limit)
    }

    /**
     * Searches for widgets matching a query string and/or filter tag, restricted by a result limit.
     *
     * @param query Search keyword matched against widget title or identifier type.
     * @param tag Optional category tag filter constraint.
     * @param limit Maximum number of results to return.
     * @return Filtered list of matching [KWidgetMetadata] items.
     */
    fun search(
        query: String = "",
        tag: String? = null,
        limit: Int = Int.MAX_VALUE
    ): List<KWidgetMetadata> {
        return GeneratedWidgetCatalog.filter { metadata ->
            val matchesQuery = query.isBlank() ||
                metadata.title.contains(query, ignoreCase = true) ||
                metadata.type.contains(query, ignoreCase = true)

            val matchesTag = tag == null || metadata.tags.contains(tag)

            matchesQuery && matchesTag
        }.take(limit)
    }
}