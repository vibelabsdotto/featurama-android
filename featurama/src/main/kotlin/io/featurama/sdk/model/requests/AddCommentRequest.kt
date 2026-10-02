package io.featurama.sdk.model.requests

import kotlinx.serialization.Serializable

@Serializable
internal data class AddCommentRequest(
    val content: String,
    val authorIdentifier: String,
    val authorName: String? = null,
)
