package io.featurama.sdk.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the status of a feature request.
 */
@Serializable
enum class FeatureRequestStatus {
    @SerialName("Requested")
    REQUESTED,

    @SerialName("Roadmap")
    ROADMAP,

    @SerialName("InProgress")
    IN_PROGRESS,

    @SerialName("Done")
    DONE,

    @SerialName("Declined")
    DECLINED
}
