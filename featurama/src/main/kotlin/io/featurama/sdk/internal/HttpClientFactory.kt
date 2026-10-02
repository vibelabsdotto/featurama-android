package io.featurama.sdk.internal

import io.featurama.sdk.FeaturamaConfig
import io.featurama.sdk.api.ApiKeyInterceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Factory for creating configured OkHttpClient instances.
 */
internal object HttpClientFactory {

    /**
     * Creates a new OkHttpClient configured with the provided settings.
     *
     * @param config The Featurama configuration.
     * @return A configured OkHttpClient instance.
     */
    fun create(config: FeaturamaConfig): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(config.apiKey))
            // Project credentials must never be forwarded to a redirect target.
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
            .build()
    }
}
