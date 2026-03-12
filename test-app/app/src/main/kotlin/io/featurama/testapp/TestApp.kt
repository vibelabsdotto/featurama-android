package io.featurama.testapp

import android.app.Application
import io.featurama.sdk.Featurama

class TestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeSdk()
    }

    companion object {
        fun initializeSdk() {
            Featurama.init(Config.apiKey) {
                baseUrl(Config.baseUrl)
            }
        }
    }
}
