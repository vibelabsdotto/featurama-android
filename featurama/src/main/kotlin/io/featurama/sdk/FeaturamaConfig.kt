package io.featurama.sdk

/**
 * Configuration options for the Featurama SDK.
 *
 * Use [Builder] to create an instance.
 *
 * @property apiKey The API key for authentication (required).
 * @property baseUrl The base URL of the Featurama API. Defaults to the hosted Featurama API.
 * @property defaultUserIdentifier Default identifier used for votes and submissions.
 * @property connectTimeoutMs Connection timeout in milliseconds.
 * @property readTimeoutMs Read timeout in milliseconds.
 * @property writeTimeoutMs Write timeout in milliseconds.
 */
class FeaturamaConfig private constructor(
    val apiKey: String,
    val baseUrl: String,
    val defaultUserIdentifier: String?,
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
    val writeTimeoutMs: Long
) {
    /**
     * Builder for creating [FeaturamaConfig] instances.
     *
     * @param apiKey The API key for authentication (required).
     */
    class Builder(private val apiKey: String) {
        private var baseUrl: String = DEFAULT_BASE_URL
        private var defaultUserIdentifier: String? = null
        private var connectTimeoutMs: Long = DEFAULT_CONNECT_TIMEOUT_MS
        private var readTimeoutMs: Long = DEFAULT_READ_TIMEOUT_MS
        private var writeTimeoutMs: Long = DEFAULT_WRITE_TIMEOUT_MS

        /**
         * Sets the base URL of the Featurama API.
         *
         * Override this only for a self-hosted or local API (e.g. "https://api.example.com").
         *
         * @param url The base URL (without trailing slash).
         * @return This builder instance.
         */
        fun baseUrl(url: String) = apply {
            this.baseUrl = url.trimEnd('/')
        }

        /**
         * Sets the default user identifier used for votes and submissions.
         *
         * This can be overridden per-request.
         *
         * @param identifier The user identifier (e.g., user ID, device ID).
         * @return This builder instance.
         */
        fun defaultUserIdentifier(identifier: String) = apply {
            this.defaultUserIdentifier = identifier
        }

        /**
         * Sets the connection timeout.
         *
         * @param timeoutMs Timeout in milliseconds.
         * @return This builder instance.
         */
        fun connectTimeout(timeoutMs: Long) = apply {
            require(timeoutMs > 0) { "Connect timeout must be positive" }
            this.connectTimeoutMs = timeoutMs
        }

        /**
         * Sets the read timeout.
         *
         * @param timeoutMs Timeout in milliseconds.
         * @return This builder instance.
         */
        fun readTimeout(timeoutMs: Long) = apply {
            require(timeoutMs > 0) { "Read timeout must be positive" }
            this.readTimeoutMs = timeoutMs
        }

        /**
         * Sets the write timeout.
         *
         * @param timeoutMs Timeout in milliseconds.
         * @return This builder instance.
         */
        fun writeTimeout(timeoutMs: Long) = apply {
            require(timeoutMs > 0) { "Write timeout must be positive" }
            this.writeTimeoutMs = timeoutMs
        }

        /**
         * Builds the [FeaturamaConfig] instance.
         *
         * @return A new [FeaturamaConfig] instance.
         * @throws IllegalArgumentException if the API key is blank.
         */
        fun build(): FeaturamaConfig {
            require(apiKey.isNotBlank()) { "API key must not be blank" }
            require(apiKey.all { it in ' '..'~' }) { "API key must contain only printable ASCII characters" }

            return FeaturamaConfig(
                apiKey = apiKey,
                baseUrl = baseUrl,
                defaultUserIdentifier = defaultUserIdentifier,
                connectTimeoutMs = connectTimeoutMs,
                readTimeoutMs = readTimeoutMs,
                writeTimeoutMs = writeTimeoutMs
            )
        }
    }

    companion object {
        /** Default base URL for the Featurama API. */
        const val DEFAULT_BASE_URL = "https://newapi.featurama.app"

        /** Default connection timeout in milliseconds. */
        const val DEFAULT_CONNECT_TIMEOUT_MS = 30_000L

        /** Default read timeout in milliseconds. */
        const val DEFAULT_READ_TIMEOUT_MS = 30_000L

        /** Default write timeout in milliseconds. */
        const val DEFAULT_WRITE_TIMEOUT_MS = 30_000L
    }
}
