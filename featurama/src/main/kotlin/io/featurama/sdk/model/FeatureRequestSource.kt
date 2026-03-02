package io.featurama.sdk.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the source from which a feature request was created.
 */
@Serializable
enum class FeatureRequestSource {
    @SerialName("Dashboard")
    DASHBOARD,

    @SerialName("SDK")
    SDK
}
