package io.featurama.sdk.exception

/**
 * Exception thrown when a network error occurs (e.g., no internet connection, timeout).
 *
 * @property message Human-readable error message.
 * @property cause The underlying cause of this exception.
 */
class NetworkException(
    message: String = "A network error occurred",
    cause: Throwable? = null
) : FeaturamaException(message, cause)
