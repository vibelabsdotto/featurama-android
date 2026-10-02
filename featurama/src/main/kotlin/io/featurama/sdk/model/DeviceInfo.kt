package io.featurama.sdk.model

import kotlinx.serialization.Serializable

/** Optional diagnostics supplied by the host. Contains no hardware identifiers. */
@Serializable
data class DeviceInfo(
    val platform: String? = null,
    val osVersion: String? = null,
    val deviceModel: String? = null,
    val deviceManufacturer: String? = null,
    val deviceType: String? = null,
    val appVersion: String? = null,
    val appBuild: String? = null,
    val locale: String? = null,
    val screenWidth: Int? = null,
    val screenHeight: Int? = null,
    val screenScale: Double? = null,
)
