package io.featurama.sdk.exception

/**
 * Exception thrown when the server returns a 5xx error.
 *
 * @property statusCode HTTP status code (500-599).
 * @property message Human-readable error message.
 * @property responseBody Raw response body from the API, if available.
 */
class ServerException(
    statusCode: Int,
    message: String = "Server error occurred",
    responseBody: String? = null
) : ApiException(statusCode, message, responseBody)
