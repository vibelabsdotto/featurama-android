package io.featurama.sample

import android.app.Application
import io.featurama.sdk.Featurama

class FeaturamaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Featurama SDK
        // Replace with your actual API key and Convex deployment URL
        Featurama.init("fm_live_your_api_key_here") {
            // Set your Convex deployment URL
            baseUrl("https://your-app.convex.site")

            // Set a default user identifier
            defaultUserIdentifier("android_user_${System.currentTimeMillis()}")
        }
    }
}
