package io.featurama.sdk.exception

/**
 * Exception thrown when access to a resource is forbidden (HTTP 403).
 *
 * @property message Human-readable error message.
 * @property responseBody Raw response body from the API, if available.
 */
class ForbiddenException(
    message: String = "Access forbidden",
    responseBody: String? = null
) : ApiException(403, message, responseBody)
