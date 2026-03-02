package io.featurama.sdk

import io.featurama.sdk.exception.*
import io.featurama.sdk.internal.HttpClientFactory
import io.featurama.sdk.model.FeatureRequest
import io.featurama.sdk.model.FeatureRequestList
import io.featurama.sdk.model.ProjectConfig
import io.featurama.sdk.model.requests.CreateFeatureRequest
import io.featurama.sdk.model.requests.UpdateFeatureRequest
import io.featurama.sdk.model.requests.VoteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder
import java.util.UUID

/**
 * Client for interacting with the Featurama API.
 *
 * This is the main entry point for making API calls. Create an instance using
 * [FeaturamaConfig] or use the [Featurama] singleton for convenience.
 *
 * All methods are suspend functions and should be called from a coroutine scope.
 *
 * @param config The configuration for this client.
 */
class FeaturamaClient(private val config: FeaturamaConfig) {

    private val httpClient: OkHttpClient = HttpClientFactory.create(config)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Volatile
    private var userIdentifier: String? = config.defaultUserIdentifier

    /**
     * Sets the user identifier used for votes and submissions.
     *
     * This overrides the default user identifier set in the configuration.
     *
     * @param identifier The user identifier (e.g., user ID, device ID).
     */
    fun setUserIdentifier(identifier: String?) {
        this.userIdentifier = identifier
    }

    /**
     * Gets the current user identifier.
     *
     * @return The current user identifier, or null if not set.
     */
    fun getUserIdentifier(): String? = userIdentifier

    /**
     * Retrieves a paginated list of feature requests.
     *
     * @param page The page number (1-indexed). Defaults to 1.
     * @param pageSize The number of items per page. Defaults to 20.
     * @return A [FeatureRequestList] containing the feature requests and pagination info.
     * @throws UnauthorizedException if the API key is invalid.
     * @throws NetworkException if a network error occurs.
     * @throws ApiException for other API errors.
     */
    suspend fun getFeatureRequests(
        page: Int = 1,
        pageSize: Int = 20,
        filter: String? = null
    ): FeatureRequestList = withContext(Dispatchers.IO) {
        require(page >= 1) { "Page must be >= 1" }
        require(pageSize in 1..100) { "Page size must be between 1 and 100" }

        var url = "${config.baseUrl}/api/public/requests?page=$page&pageSize=$pageSize"
        if (filter != null) url += "&filter=${encode(filter)}"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        executeRequest(request)
    }

    /**
     * Creates a new feature request.
     *
     * @param title The title of the feature request (1-200 characters).
     * @param description Optional description of the feature request (max 2000 characters).
     * @param submitterIdentifier Optional identifier for the submitter.
     *                            If not provided, uses the configured user identifier.
     * @return The created [FeatureRequest].
     * @throws UnauthorizedException if the API key is invalid.
     * @throws NetworkException if a network error occurs.
     * @throws ApiException for other API errors.
     */
    suspend fun createFeatureRequest(
        title: String,
        description: String? = null,
        submitterIdentifier: String? = null
    ): FeatureRequest = withContext(Dispatchers.IO) {
        require(title.isNotBlank()) { "Title must not be blank" }
        require(title.length <= 200) { "Title must not exceed 200 characters" }
        description?.let {
            require(it.length <= 2000) { "Description must not exceed 2000 characters" }
        }

        val effectiveSubmitter = submitterIdentifier ?: userIdentifier

        val body = CreateFeatureRequest(
            title = title.trim(),
            description = description?.trim(),
            submitterIdentifier = effectiveSubmitter
        )

        val request = Request.Builder()
            .url("${config.baseUrl}/api/public/requests")
            .post(json.encodeToString(CreateFeatureRequest.serializer(), body).toJsonRequestBody())
            .build()

        executeRequest(request)
    }

    /**
     * Updates an existing feature request.
     *
     * Only the original submitter can update a feature request.
     *
     * @param id The ID of the feature request to update.
     * @param title The new title of the feature request (1-200 characters).
     * @param description The new description of the feature request (max 2000 characters).
     * @param submitterIdentifier The submitter identifier for authorization.
     *                            If not provided, uses the configured user identifier.
     * @return The updated [FeatureRequest].
     * @throws UnauthorizedException if the API key is invalid.
     * @throws ForbiddenException if the submitter identifier doesn't match.
     * @throws NotFoundException if the feature request is not found.
     * @throws NetworkException if a network error occurs.
     * @throws ApiException for other API errors.
     */
    suspend fun updateFeatureRequest(
        id: UUID,
        title: String,
        description: String? = null,
        submitterIdentifier: String? = null
    ): FeatureRequest = withContext(Dispatchers.IO) {
        require(title.isNotBlank()) { "Title must not be blank" }
        require(title.length <= 200) { "Title must not exceed 200 characters" }
        description?.let {
            require(it.length <= 2000) { "Description must not exceed 2000 characters" }
        }

        val effectiveSubmitter = submitterIdentifier ?: userIdentifier
        requireNotNull(effectiveSubmitter) { "Submitter identifier is required for updates" }

        val body = UpdateFeatureRequest(
            title = title.trim(),
            description = description?.trim()
        )

        val url = "${config.baseUrl}/api/public/requests/$id?submitterIdentifier=${encode(effectiveSubmitter)}"

        val request = Request.Builder()
            .url(url)
            .put(json.encodeToString(UpdateFeatureRequest.serializer(), body).toJsonRequestBody())
            .build()

        executeRequest(request)
    }

    /**
     * Votes for a feature request.
     *
     * Each user can only vote once per feature request. Attempting to vote
     * again will throw a [ConflictException].
     *
     * @param featureRequestId The ID of the feature request to vote for.
     * @param voterIdentifier The identifier of the voter.
     *                        If not provided, uses the configured user identifier.
     * @return The updated [FeatureRequest] with the new vote count.
     * @throws UnauthorizedException if the API key is invalid.
     * @throws NotFoundException if the feature request is not found.
     * @throws ConflictException if the user has already voted for this request.
     * @throws NetworkException if a network error occurs.
     * @throws ApiException for other API errors.
     */
    suspend fun vote(
        featureRequestId: UUID,
        voterIdentifier: String? = null
    ): FeatureRequest = withContext(Dispatchers.IO) {
        val effectiveVoter = voterIdentifier ?: userIdentifier
        requireNotNull(effectiveVoter) { "Voter identifier is required" }

        val body = VoteRequest(voterIdentifier = effectiveVoter)

        val request = Request.Builder()
            .url("${config.baseUrl}/api/public/requests/$featureRequestId/vote")
            .post(json.encodeToString(VoteRequest.serializer(), body).toJsonRequestBody())
            .build()

        executeRequest(request)
    }

    /**
     * Removes a vote from a feature request.
     *
     * @param featureRequestId The ID of the feature request to remove the vote from.
     * @param voterIdentifier The identifier of the voter.
     *                        If not provided, uses the configured user identifier.
     * @return The updated [FeatureRequest] with the new vote count.
     * @throws UnauthorizedException if the API key is invalid.
     * @throws NotFoundException if the feature request is not found or the user hasn't voted.
     * @throws NetworkException if a network error occurs.
     * @throws ApiException for other API errors.
     */
    suspend fun removeVote(
        featureRequestId: UUID,
        voterIdentifier: String? = null
    ): FeatureRequest = withContext(Dispatchers.IO) {
        val effectiveVoter = voterIdentifier ?: userIdentifier
        requireNotNull(effectiveVoter) { "Voter identifier is required" }

        val body = VoteRequest(voterIdentifier = effectiveVoter)

        val request = Request.Builder()
            .url("${config.baseUrl}/api/public/requests/$featureRequestId/vote")
            .delete(json.encodeToString(VoteRequest.serializer(), body).toJsonRequestBody())
            .build()

        executeRequest(request)
    }

    /**
     * Toggles a vote on a feature request.
     *
     * If the user has not voted, adds a vote. If already voted (409 Conflict),
     * removes the vote instead.
     */
    suspend fun toggleVote(
        featureRequestId: UUID,
        voterIdentifier: String? = null
    ): FeatureRequest {
        return try {
            vote(featureRequestId, voterIdentifier)
        } catch (e: ConflictException) {
            removeVote(featureRequestId, voterIdentifier)
        }
    }

    /**
     * Fetches the project configuration (branding settings, email collection mode).
     *
     * @return The [ProjectConfig] for the current project.
     * @throws UnauthorizedException if the API key is invalid.
     * @throws NetworkException if a network error occurs.
     * @throws ApiException for other API errors.
     */
    suspend fun getProjectConfig(): ProjectConfig = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${config.baseUrl}/api/public/config")
            .get()
            .build()

        executeRequest(request)
    }

    private suspend inline fun <reified T> executeRequest(request: Request): T {
        try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                throw when (response.code) {
                    401 -> UnauthorizedException(responseBody = responseBody)
                    403 -> ForbiddenException(responseBody = responseBody)
                    404 -> NotFoundException(responseBody = responseBody)
                    409 -> ConflictException(responseBody = responseBody)
                    in 500..599 -> ServerException(
                        statusCode = response.code,
                        message = "Server error: ${response.code}",
                        responseBody = responseBody
                    )
                    else -> ApiException(
                        statusCode = response.code,
                        message = "API error: ${response.code}",
                        responseBody = responseBody
                    )
                }
            }

            return responseBody?.let {
                json.decodeFromString(it)
            } ?: throw FeaturamaException("Empty response body")
        } catch (e: IOException) {
            throw NetworkException(cause = e)
        } catch (e: FeaturamaException) {
            throw e
        } catch (e: Exception) {
            throw FeaturamaException("Unexpected error: ${e.message}", e)
        }
    }

    private fun String.toJsonRequestBody() =
        toRequestBody("application/json; charset=utf-8".toMediaType())

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8")
}
