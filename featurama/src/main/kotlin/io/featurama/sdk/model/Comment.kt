package io.featurama.sdk.model

import io.featurama.sdk.internal.InstantSerializer
import io.featurama.sdk.internal.UuidSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/** Public comment. authorIdentifier is anonymized by the API. */
@Serializable
data class Comment(
    @Serializable(with = UuidSerializer::class) val id: UUID,
    @Serializable(with = UuidSerializer::class) val featureRequestId: UUID,
    val content: String,
    val authorIdentifier: String,
    val authorName: String? = null,
    val authorRole: String = "user",
    val voteCount: Int = 0,
    @Serializable(with = InstantSerializer::class) val createdAt: Instant,
)
