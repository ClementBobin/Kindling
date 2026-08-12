package dev.kindling.core.components.dashboard

object KWidgetCatalogManager {

    fun listAll(limit: Int = Int.MAX_VALUE): List<KWidgetMetadata> {
        return GeneratedWidgetCatalog.take(limit)
    }

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