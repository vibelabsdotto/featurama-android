package io.featurama.sdk.model.requests

import kotlinx.serialization.Serializable

/**
 * Request body for voting or removing a vote on a feature request.
 *
 * @property voterIdentifier Unique identifier for the voter. This is used to prevent duplicate votes.
 */
@Serializable
internal data class VoteRequest(
    val voterIdentifier: String
)
