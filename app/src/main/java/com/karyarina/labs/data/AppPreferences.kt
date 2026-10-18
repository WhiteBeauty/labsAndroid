package com.karyarina.labs.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object AppPreferences {

    private const val PREFS_NAME = "settings"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_REVERSE_DIRECTION = "reverse_direction"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(context: Context): Int =
        prefs(context).getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun setThemeMode(context: Context, mode: Int) {
        prefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun applySavedTheme(context: Context) {
        AppCompatDelegate.setDefaultNightMode(getThemeMode(context))
    }

    fun isReverseDirection(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REVERSE_DIRECTION, false)

    fun setReverseDirection(context: Context, reversed: Boolean) {
        prefs(context).edit().putBoolean(KEY_REVERSE_DIRECTION, reversed).apply()
    }
}
