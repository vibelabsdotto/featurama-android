package io.featurama.sdk.exception

/**
 * Base exception for all Featurama SDK errors.
 *
 * @property message Human-readable error message.
 * @property cause The underlying cause of this exception, if any.
 */
open class FeaturamaException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)
