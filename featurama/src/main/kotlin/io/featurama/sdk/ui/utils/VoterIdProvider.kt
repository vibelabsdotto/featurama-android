package io.featurama.sdk.ui.utils

import android.content.Context
import java.util.UUID

internal object VoterIdProvider {
    private const val PREFS_NAME = "featurama_prefs"
    private const val KEY = "featurama_voter_id"

    fun getOrCreate(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var id = prefs.getString(KEY, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY, id).apply()
        }
        return id
    }
}
