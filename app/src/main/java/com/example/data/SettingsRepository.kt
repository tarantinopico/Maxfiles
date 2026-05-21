package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("max_files_settings", Context.MODE_PRIVATE)

    var useSystemTheme: Boolean
        get() = prefs.getBoolean("useSystemTheme", false)
        set(value) = prefs.edit().putBoolean("useSystemTheme", value).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean("isDarkTheme", true)
        set(value) = prefs.edit().putBoolean("isDarkTheme", value).apply()

    var showHiddenFiles: Boolean
        get() = prefs.getBoolean("showHiddenFiles", true)
        set(value) = prefs.edit().putBoolean("showHiddenFiles", value).apply()

    var highContrastCode: Boolean
        get() = prefs.getBoolean("highContrastCode", true)
        set(value) = prefs.edit().putBoolean("highContrastCode", value).apply()

    var gitToken: String
        get() = prefs.getString("gitToken", "") ?: ""
        set(value) = prefs.edit().putString("gitToken", value).apply()

    var gitUsername: String
        get() = prefs.getString("gitUsername", "") ?: ""
        set(value) = prefs.edit().putString("gitUsername", value).apply()

    var fontSize: Int
        get() = prefs.getInt("fontSize", 14)
        set(value) = prefs.edit().putInt("fontSize", value).apply()

    var sortBy: Int
        get() = prefs.getInt("sortBy", 0) // 0: Name, 1: Size, 2: Date
        set(value) = prefs.edit().putInt("sortBy", value).apply()

    var isGridView: Boolean
        get() = prefs.getBoolean("isGridView", false)
        set(value) = prefs.edit().putBoolean("isGridView", value).apply()
}
