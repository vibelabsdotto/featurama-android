package io.featurama.sdk.exception

/**
 * Exception thrown when the API returns an error response.
 *
 * @property statusCode HTTP status code returned by the API.
 * @property message Human-readable error message.
 * @property responseBody Raw response body from the API, if available.
 */
open class ApiException(
    val statusCode: Int,
    message: String,
    val responseBody: String? = null
) : FeaturamaException(message)
