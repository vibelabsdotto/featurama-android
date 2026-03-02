package io.featurama.sdk.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that adds the API key header to all requests.
 *
 * @property apiKey The API key to include in requests.
 */
internal class ApiKeyInterceptor(
    private val apiKey: String
) : Interceptor {

    companion object {
        private const val HEADER_API_KEY = "X-Api-Key"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .header(HEADER_API_KEY, apiKey)
            .build()

        return chain.proceed(newRequest)
    }
}
