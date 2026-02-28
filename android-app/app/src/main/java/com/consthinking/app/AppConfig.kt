package com.consthinking.app

import android.content.Context

object AppConfig {
    private const val PREF = "consthinking_pref"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_USER_ID = "user_id"

    fun save(context: Context, baseUrl: String, userId: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BASE_URL, baseUrl)
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun baseUrl(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_BASE_URL, "http://10.0.2.2:8080")
            .orEmpty()

    fun userId(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_USER_ID, "demo-user")
            .orEmpty()
}
