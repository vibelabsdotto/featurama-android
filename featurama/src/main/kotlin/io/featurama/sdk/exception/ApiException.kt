package io.featurama.sdk.exception

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

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
) : FeaturamaException(serverMessage(responseBody) ?: message)

private fun serverMessage(body: String?): String? = try {
    body?.let { Json.parseToJsonElement(it).jsonObject["message"]?.jsonPrimitive?.contentOrNull }
} catch (_: Exception) {
    null
}
