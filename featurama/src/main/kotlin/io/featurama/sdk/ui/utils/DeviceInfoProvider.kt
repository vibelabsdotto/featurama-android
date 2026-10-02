package io.featurama.sdk.ui.utils

import android.content.Context
import android.os.Build
import io.featurama.sdk.model.DeviceInfo
import java.util.Locale

/** Permission-free diagnostics only. Never read hardware IDs, contacts or location. */
internal object DeviceInfoProvider {
    @Suppress("DEPRECATION")
    fun collect(context: Context): DeviceInfo {
        val packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
        val build = packageInfo?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode.toString()
            else it.versionCode.toString()
        }
        return DeviceInfo(
            platform = "Android",
            osVersion = Build.VERSION.RELEASE,
            deviceModel = Build.MODEL,
            deviceManufacturer = Build.MANUFACTURER,
            appVersion = packageInfo?.versionName,
            appBuild = build,
            locale = Locale.getDefault().toLanguageTag(),
        )
    }
}
