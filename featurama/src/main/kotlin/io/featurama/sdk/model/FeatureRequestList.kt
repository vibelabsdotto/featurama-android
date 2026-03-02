package io.featurama.sdk.model

import kotlinx.serialization.Serializable
import kotlin.math.ceil

/**
 * Paginated response containing feature requests.
 *
 * @property items List of feature requests for the current page.
 * @property totalCount Total number of feature requests across all pages.
 * @property page Current page number (1-indexed).
 * @property pageSize Number of items per page.
 */
@Serializable
data class FeatureRequestList(
    val items: List<FeatureRequest>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int
) {
    /**
     * Total number of pages available.
     */
    val totalPages: Int
        get() = if (pageSize > 0) ceil(totalCount.toDouble() / pageSize).toInt() else 0

    /**
     * Returns true if there is a next page available.
     */
    val hasNextPage: Boolean
        get() = page < totalPages

    /**
     * Returns true if there is a previous page available.
     */
    val hasPreviousPage: Boolean
        get() = page > 1

    /**
     * Returns true if the list is empty.
     */
    val isEmpty: Boolean
        get() = items.isEmpty()

    /**
     * Returns true if this is the first page.
     */
    val isFirstPage: Boolean
        get() = page == 1

    /**
     * Returns true if this is the last page.
     */
    val isLastPage: Boolean
        get() = page >= totalPages
}
