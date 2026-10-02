package io.featurama.sdk

import io.featurama.sdk.exception.*
import io.featurama.sdk.model.FeatureRequestSource
import io.featurama.sdk.model.FeatureRequestStatus
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class FeaturamaClientTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var client: FeaturamaClient

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()

        val config = FeaturamaConfig.Builder("fm_live_test123")
            .baseUrl(mockServer.url("/").toString().trimEnd('/'))
            .defaultUserIdentifier("test_user")
            .build()

        client = FeaturamaClient(config)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun `getFeatureRequests returns parsed response`() = runBlocking {
        val responseJson = """
            {
                "items": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "projectId": "660e8400-e29b-41d4-a716-446655440001",
                        "title": "Dark Mode",
                        "description": "Add dark mode support",
                        "status": "Requested",
                        "source": "SDK",
                        "voteCount": 5,
                        "submitterIdentifier": "user_123",
                        "createdAt": "2024-01-15T10:30:00Z"
                    }
                ],
                "totalCount": 1,
                "page": 1,
                "pageSize": 20
            }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(responseJson))

        val result = client.getFeatureRequests()

        assertEquals(1, result.items.size)
        assertEquals("Dark Mode", result.items[0].title)
        assertEquals(FeatureRequestStatus.REQUESTED, result.items[0].status)
        assertEquals(FeatureRequestSource.SDK, result.items[0].source)
        assertEquals(5, result.items[0].voteCount)
        assertEquals(1, result.page)
        assertEquals(20, result.pageSize)
        assertEquals(1, result.totalCount)
    }

    @Test
    fun `getFeatureRequests sends correct query parameters`() = runBlocking {
        mockServer.enqueue(MockResponse().setBody(emptyListResponse()))

        client.getFeatureRequests(page = 2, pageSize = 50)

        val request = mockServer.takeRequest()
        assertTrue(request.path!!.contains("page=2"))
        assertTrue(request.path!!.contains("pageSize=50"))
    }

    @Test
    fun `getFeatureRequests sends API key header`() = runBlocking {
        mockServer.enqueue(MockResponse().setBody(emptyListResponse()))

        client.getFeatureRequests()

        val request = mockServer.takeRequest()
        assertEquals("fm_live_test123", request.getHeader("X-Api-Key"))
    }

    @Test
    fun `createFeatureRequest sends correct body`() = runBlocking {
        mockServer.enqueue(MockResponse().setBody(singleFeatureRequestResponse()))

        client.createFeatureRequest(
            title = "New Feature",
            description = "Feature description",
            submitterIdentifier = "user_123"
        )

        val request = mockServer.takeRequest()
        assertEquals("POST", request.method)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"title\":\"New Feature\""))
        assertTrue(body.contains("\"description\":\"Feature description\""))
        assertTrue(body.contains("\"submitterIdentifier\":\"user_123\""))
    }

    @Test
    fun `createFeatureRequest uses default user identifier`() = runBlocking {
        mockServer.enqueue(MockResponse().setBody(singleFeatureRequestResponse()))

        client.createFeatureRequest(title = "New Feature", description = "Feature description")

        val request = mockServer.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"submitterIdentifier\":\"test_user\""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createFeatureRequest with blank title throws`() {
        runBlocking { client.createFeatureRequest(title = "   ", description = "Desc") }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createFeatureRequest with title over 200 chars throws`() {
        runBlocking {
            client.createFeatureRequest(title = "a".repeat(201), description = "Desc")
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createFeatureRequest with blank description throws`() {
        runBlocking { client.createFeatureRequest(title = "Title", description = "   ") }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateFeatureRequest with blank description throws`() {
        runBlocking {
            val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
            client.updateFeatureRequest(id = id, title = "Title", description = "")
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createFeatureRequest with description over 2000 chars throws`() {
        runBlocking {
            client.createFeatureRequest(title = "Title", description = "a".repeat(2001))
        }
    }

    @Test
    fun `updateFeatureRequest sends PUT with correct URL`() = runBlocking {
        mockServer.enqueue(MockResponse().setBody(singleFeatureRequestResponse()))
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        client.updateFeatureRequest(
            id = id,
            title = "Updated Title",
            description = "Updated description",
            submitterIdentifier = "user_123"
        )

        val request = mockServer.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path!!.contains(id.toString()))
        assertTrue(request.path!!.contains("submitterIdentifier=user_123"))
    }

    @Test
    fun `vote sends POST to correct endpoint`() = runBlocking {
        mockServer.enqueue(MockResponse().setBody(singleFeatureRequestResponse()))
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        client.vote(featureRequestId = id, voterIdentifier = "voter_123")

        val request = mockServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path!!.contains("$id/vote"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"voterIdentifier\":\"voter_123\""))
    }

    @Test
    fun `removeVote sends DELETE to correct endpoint`() = runBlocking {
        mockServer.enqueue(MockResponse().setBody(singleFeatureRequestResponse()))
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        client.removeVote(featureRequestId = id, voterIdentifier = "voter_123")

        val request = mockServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertTrue(request.path!!.contains("$id/vote"))
    }

    @Test(expected = UnauthorizedException::class)
    fun `401 response throws UnauthorizedException`() {
        runBlocking {
            mockServer.enqueue(MockResponse().setResponseCode(401))
            client.getFeatureRequests()
        }
    }

    @Test(expected = ForbiddenException::class)
    fun `403 response throws ForbiddenException`() {
        runBlocking {
            mockServer.enqueue(MockResponse().setResponseCode(403))
            client.getFeatureRequests()
        }
    }

    @Test(expected = NotFoundException::class)
    fun `404 response throws NotFoundException`() {
        runBlocking {
            mockServer.enqueue(MockResponse().setResponseCode(404))
            client.getFeatureRequests()
        }
    }

    @Test(expected = ConflictException::class)
    fun `409 response throws ConflictException`() {
        runBlocking {
            mockServer.enqueue(MockResponse().setResponseCode(409))
            val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
            client.vote(id)
        }
    }

    @Test(expected = ServerException::class)
    fun `500 response throws ServerException`() {
        runBlocking {
            mockServer.enqueue(MockResponse().setResponseCode(500))
            client.getFeatureRequests()
        }
    }

    @Test
    fun `setUserIdentifier updates identifier for subsequent calls`() = runBlocking {
        mockServer.enqueue(MockResponse().setBody(singleFeatureRequestResponse()))

        client.setUserIdentifier("new_user")
        client.createFeatureRequest(title = "Test", description = "Test description")

        val request = mockServer.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"submitterIdentifier\":\"new_user\""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getFeatureRequests with page less than 1 throws`() {
        runBlocking { client.getFeatureRequests(page = 0) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getFeatureRequests with pageSize less than 1 throws`() {
        runBlocking { client.getFeatureRequests(pageSize = 0) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getFeatureRequests with pageSize greater than 100 throws`() {
        runBlocking { client.getFeatureRequests(pageSize = 101) }
    }

    private fun emptyListResponse() = """
        {
            "items": [],
            "totalCount": 0,
            "page": 1,
            "pageSize": 20
        }
    """.trimIndent()

    private fun singleFeatureRequestResponse() = """
        {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "projectId": "660e8400-e29b-41d4-a716-446655440001",
            "title": "Test Feature",
            "description": "Test description",
            "status": "Requested",
            "source": "SDK",
            "voteCount": 1,
            "submitterIdentifier": "user_123",
            "createdAt": "2024-01-15T10:30:00Z"
        }
    """.trimIndent()
}
