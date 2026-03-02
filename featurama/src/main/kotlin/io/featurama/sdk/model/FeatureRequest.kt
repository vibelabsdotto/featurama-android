package io.featurama.sdk.model

import io.featurama.sdk.internal.InstantSerializer
import io.featurama.sdk.internal.UuidSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * Represents a feature request in the Featurama system.
 *
 * @property id Unique identifier of the feature request.
 * @property projectId The ID of the project this feature request belongs to.
 * @property title Title of the feature request.
 * @property description Optional detailed description of the feature request.
 * @property status Current status of the feature request.
 * @property source The source from which the request was created (Dashboard or SDK).
 * @property voteCount Total number of votes for this feature request.
 * @property submitterIdentifier Optional identifier of the user who submitted the request.
 * @property createdAt Timestamp when the feature request was created.
 */
@Serializable
data class FeatureRequest(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,

    @Serializable(with = UuidSerializer::class)
    val projectId: UUID,

    val title: String,

    val description: String? = null,

    val status: FeatureRequestStatus,

    val source: FeatureRequestSource,

    val voteCount: Int,

    val submitterIdentifier: String? = null,

    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant
)
