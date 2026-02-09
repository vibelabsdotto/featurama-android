package io.featurama.testapp

/**
 * Mutable runtime configuration for the test app.
 * Default base URL uses 10.0.2.2 which maps to the host machine's
 * localhost from the Android emulator.
 */
object Config {
    var apiKey: String = "fm_live_REPLACE_ME"
    var baseUrl: String = "http://10.0.2.2:5001"
}
