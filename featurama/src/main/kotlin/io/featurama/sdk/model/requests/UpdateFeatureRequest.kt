package io.featurama.sdk.model.requests

import kotlinx.serialization.Serializable

/**
 * Request body for updating an existing feature request.
 *
 * @property title New title for the feature request. Must be between 1 and 200 characters.
 * @property description New description for the feature request. Max 2000 characters.
 */
@Serializable
internal data class UpdateFeatureRequest(
    val title: String,
    val description: String? = null
)
