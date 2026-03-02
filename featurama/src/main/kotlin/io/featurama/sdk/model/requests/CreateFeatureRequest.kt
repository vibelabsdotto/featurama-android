package io.featurama.sdk.model.requests

import kotlinx.serialization.Serializable

/**
 * Request body for creating a new feature request.
 *
 * @property title Title of the feature request. Must be between 1 and 200 characters.
 * @property description Optional detailed description of the feature request. Max 2000 characters.
 * @property submitterIdentifier Optional identifier for the user submitting the request.
 */
@Serializable
internal data class CreateFeatureRequest(
    val title: String,
    val description: String? = null,
    val submitterIdentifier: String? = null
)
