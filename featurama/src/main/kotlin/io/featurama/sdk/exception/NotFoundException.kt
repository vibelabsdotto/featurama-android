package io.featurama.sdk.exception

/**
 * Exception thrown when a requested resource is not found (HTTP 404).
 *
 * @property message Human-readable error message.
 * @property responseBody Raw response body from the API, if available.
 */
class NotFoundException(
    message: String = "Resource not found",
    responseBody: String? = null
) : ApiException(404, message, responseBody)
