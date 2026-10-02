package io.featurama.sample

import android.app.Application
import io.featurama.sdk.Featurama
import java.util.UUID

class FeaturamaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val preferences = getSharedPreferences("featurama_sample", MODE_PRIVATE)
        val identifier = preferences.getString("user", null) ?: UUID.randomUUID().toString().also {
            preferences.edit().putString("user", it).apply()
        }
        // Supply the key and optional local URL through environment variables at build time.
        Featurama.init(BuildConfig.FEATURAMA_API_KEY) {
            baseUrl(BuildConfig.FEATURAMA_BASE_URL)
            defaultUserIdentifier(identifier)
        }
    }
}
