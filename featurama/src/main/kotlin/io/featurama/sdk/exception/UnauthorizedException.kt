package io.featurama.sdk.exception

/**
 * Exception thrown when the API key is invalid or missing (HTTP 401).
 *
 * @property message Human-readable error message.
 * @property responseBody Raw response body from the API, if available.
 */
class UnauthorizedException(
    message: String = "Invalid or missing API key",
    responseBody: String? = null
) : ApiException(401, message, responseBody)
