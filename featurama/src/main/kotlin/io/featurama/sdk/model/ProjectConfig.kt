package io.featurama.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectConfig(
    val branding: Branding,
    val emailCollection: String = "none"
) {
    @Serializable
    data class Branding(
        val showBranding: Boolean
    )
}
