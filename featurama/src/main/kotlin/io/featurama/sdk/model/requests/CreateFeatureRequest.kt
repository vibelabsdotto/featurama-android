package io.featurama.sdk.model.requests

import kotlinx.serialization.Serializable

/**
 * Request body for creating a new feature request.
 *
 * @property title Title of the feature request. Must be between 1 and 200 characters.
 * @property description Detailed description of the feature request. Required by the API. Max 2000 characters.
 * @property submitterIdentifier Identifier for the user submitting the request, required by the API.
 */
@Serializable
internal data class CreateFeatureRequest(
    val title: String,
    val description: String,
    val submitterIdentifier: String? = null,
    val email: String? = null,
    val deviceInfo: io.featurama.sdk.model.DeviceInfo? = null
)
