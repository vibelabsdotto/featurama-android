package io.featurama.sdk.exception

/**
 * Exception thrown when there is a conflict with the current state (HTTP 409).
 *
 * This is typically thrown when attempting to vote on a feature request
 * that the user has already voted on.
 *
 * @property message Human-readable error message.
 * @property responseBody Raw response body from the API, if available.
 */
class ConflictException(
    message: String = "Conflict: resource already exists or action already taken",
    responseBody: String? = null
) : ApiException(409, message, responseBody)
