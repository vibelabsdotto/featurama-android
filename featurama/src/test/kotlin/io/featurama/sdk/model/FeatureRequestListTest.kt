package io.featurama.sdk.model

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.UUID

class FeatureRequestListTest {

    private fun createFeatureRequest(id: UUID = UUID.randomUUID()): FeatureRequest {
        return FeatureRequest(
            id = id,
            projectId = UUID.randomUUID(),
            title = "Test",
            description = null,
            status = FeatureRequestStatus.REQUESTED,
            source = FeatureRequestSource.SDK,
            voteCount = 0,
            submitterIdentifier = null,
            createdAt = Instant.now()
        )
    }

    @Test
    fun `totalPages calculates correctly`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 50,
            page = 1,
            pageSize = 20
        )

        assertEquals(3, list.totalPages)
    }

    @Test
    fun `totalPages returns 0 for zero pageSize`() {
        val list = FeatureRequestList(
            items = emptyList(),
            totalCount = 0,
            page = 1,
            pageSize = 0
        )

        assertEquals(0, list.totalPages)
    }

    @Test
    fun `hasNextPage returns true when more pages exist`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 50,
            page = 1,
            pageSize = 20
        )

        assertTrue(list.hasNextPage)
    }

    @Test
    fun `hasNextPage returns false on last page`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 50,
            page = 3,
            pageSize = 20
        )

        assertFalse(list.hasNextPage)
    }

    @Test
    fun `hasPreviousPage returns true when not on first page`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 50,
            page = 2,
            pageSize = 20
        )

        assertTrue(list.hasPreviousPage)
    }

    @Test
    fun `hasPreviousPage returns false on first page`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 50,
            page = 1,
            pageSize = 20
        )

        assertFalse(list.hasPreviousPage)
    }

    @Test
    fun `isEmpty returns true for empty list`() {
        val list = FeatureRequestList(
            items = emptyList(),
            totalCount = 0,
            page = 1,
            pageSize = 20
        )

        assertTrue(list.isEmpty)
    }

    @Test
    fun `isEmpty returns false for non-empty list`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 1,
            page = 1,
            pageSize = 20
        )

        assertFalse(list.isEmpty)
    }

    @Test
    fun `isFirstPage returns true on page 1`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 50,
            page = 1,
            pageSize = 20
        )

        assertTrue(list.isFirstPage)
    }

    @Test
    fun `isFirstPage returns false on other pages`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 50,
            page = 2,
            pageSize = 20
        )

        assertFalse(list.isFirstPage)
    }

    @Test
    fun `isLastPage returns true on last page`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 50,
            page = 3,
            pageSize = 20
        )

        assertTrue(list.isLastPage)
    }

    @Test
    fun `isLastPage returns false on other pages`() {
        val list = FeatureRequestList(
            items = listOf(createFeatureRequest()),
            totalCount = 50,
            page = 1,
            pageSize = 20
        )

        assertFalse(list.isLastPage)
    }
}
