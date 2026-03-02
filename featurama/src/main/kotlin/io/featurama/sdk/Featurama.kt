package io.featurama.sdk

import io.featurama.sdk.exception.FeaturamaException
import io.featurama.sdk.model.FeatureRequest
import io.featurama.sdk.model.FeatureRequestList
import io.featurama.sdk.model.ProjectConfig
import java.util.UUID

/**
 * Singleton entry point for the Featurama SDK.
 *
 * Initialize the SDK once using [init] before making any API calls.
 *
 * Example usage:
 * ```kotlin
 * // In your Application.onCreate()
 * Featurama.init("fm_live_xxxxxxxxxxxx") {
 *     defaultUserIdentifier("user_123")
 *     baseUrl("https://your-app.convex.site")
 * }
 *
 * // Later, in a coroutine
 * val requests = Featurama.getFeatureRequests()
 * ```
 */
object Featurama {

    @Volatile
    private var client: FeaturamaClient? = null

    @Volatile
    private var config: FeaturamaConfig? = null

    /**
     * Initializes the Featurama SDK with the given API key.
     *
     * This method should be called once, typically in your Application's `onCreate()`.
     * Subsequent calls will reinitialize the SDK with the new configuration.
     *
     * @param apiKey Your Featurama API key (starts with "fm_live_").
     * @param configure Optional configuration block for additional settings.
     * @throws IllegalArgumentException if the API key is blank.
     */
    @JvmStatic
    @JvmOverloads
    fun init(apiKey: String, configure: FeaturamaConfig.Builder.() -> Unit = {}) {
        val builder = FeaturamaConfig.Builder(apiKey)
        builder.configure()
        val newConfig = builder.build()

        synchronized(this) {
            config = newConfig
            client = FeaturamaClient(newConfig)
        }
    }

    /**
     * Initializes the Featurama SDK with a pre-built configuration.
     *
     * @param config The configuration to use.
     */
    @JvmStatic
    fun init(config: FeaturamaConfig) {
        synchronized(this) {
            this.config = config
            this.client = FeaturamaClient(config)
        }
    }

    /**
     * Sets the user identifier used for votes and submissions.
     *
     * This can be called at any time after initialization to update the user identifier.
     *
     * @param identifier The user identifier (e.g., user ID, device ID).
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    fun setUserIdentifier(identifier: String?) {
        getClient().setUserIdentifier(identifier)
    }

    /**
     * Gets the current user identifier.
     *
     * @return The current user identifier, or null if not set.
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    fun getUserIdentifier(): String? = getClient().getUserIdentifier()

    /**
     * Returns the current configuration.
     *
     * @return The current [FeaturamaConfig].
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    fun getConfig(): FeaturamaConfig =
        config ?: throw IllegalStateException("Featurama SDK has not been initialized. Call Featurama.init() first.")

    /**
     * Returns true if the SDK has been initialized.
     */
    @JvmStatic
    fun isInitialized(): Boolean = client != null

    /**
     * Retrieves a paginated list of feature requests.
     *
     * @param page The page number (1-indexed). Defaults to 1.
     * @param pageSize The number of items per page. Defaults to 20.
     * @return A [FeatureRequestList] containing the feature requests and pagination info.
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    suspend fun getFeatureRequests(
        page: Int = 1,
        pageSize: Int = 20,
        filter: String? = null
    ): FeatureRequestList = getClient().getFeatureRequests(page, pageSize, filter)

    /**
     * Creates a new feature request.
     *
     * @param title The title of the feature request (1-200 characters).
     * @param description Optional description of the feature request (max 2000 characters).
     * @param submitterIdentifier Optional identifier for the submitter.
     * @return The created [FeatureRequest].
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    suspend fun createFeatureRequest(
        title: String,
        description: String? = null,
        submitterIdentifier: String? = null
    ): FeatureRequest = getClient().createFeatureRequest(title, description, submitterIdentifier)

    /**
     * Updates an existing feature request.
     *
     * @param id The ID of the feature request to update.
     * @param title The new title of the feature request (1-200 characters).
     * @param description The new description of the feature request (max 2000 characters).
     * @param submitterIdentifier The submitter identifier for authorization.
     * @return The updated [FeatureRequest].
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    suspend fun updateFeatureRequest(
        id: UUID,
        title: String,
        description: String? = null,
        submitterIdentifier: String? = null
    ): FeatureRequest = getClient().updateFeatureRequest(id, title, description, submitterIdentifier)

    /**
     * Votes for a feature request.
     *
     * @param featureRequestId The ID of the feature request to vote for.
     * @param voterIdentifier The identifier of the voter.
     * @return The updated [FeatureRequest] with the new vote count.
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    suspend fun vote(
        featureRequestId: UUID,
        voterIdentifier: String? = null
    ): FeatureRequest = getClient().vote(featureRequestId, voterIdentifier)

    /**
     * Removes a vote from a feature request.
     *
     * @param featureRequestId The ID of the feature request to remove the vote from.
     * @param voterIdentifier The identifier of the voter.
     * @return The updated [FeatureRequest] with the new vote count.
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    suspend fun removeVote(
        featureRequestId: UUID,
        voterIdentifier: String? = null
    ): FeatureRequest = getClient().removeVote(featureRequestId, voterIdentifier)

    /**
     * Toggles a vote on a feature request.
     *
     * If the user has not voted, adds a vote. If already voted (409 Conflict),
     * removes the vote instead.
     *
     * @param featureRequestId The ID of the feature request to toggle the vote on.
     * @param voterIdentifier The identifier of the voter.
     * @return The updated [FeatureRequest] with the new vote count.
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    suspend fun toggleVote(
        featureRequestId: UUID,
        voterIdentifier: String? = null
    ): FeatureRequest = getClient().toggleVote(featureRequestId, voterIdentifier)

    /**
     * Fetches the project configuration from the API.
     *
     * @return The [ProjectConfig] for the current project.
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    suspend fun getProjectConfig(): ProjectConfig = getClient().getProjectConfig()

    /**
     * Returns the underlying client instance.
     *
     * Use this if you need direct access to the client or want to manage
     * multiple client instances manually.
     *
     * @return The [FeaturamaClient] instance.
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    @JvmStatic
    fun getClient(): FeaturamaClient =
        client ?: throw IllegalStateException("Featurama SDK has not been initialized. Call Featurama.init() first.")

    /**
     * Resets the SDK state. Primarily for testing purposes.
     */
    @JvmStatic
    internal fun reset() {
        synchronized(this) {
            client = null
            config = null
        }
    }
}
