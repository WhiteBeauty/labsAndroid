package com.karyarina.labs.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object AppPreferences {

    private const val PREFS_NAME = "settings"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_REVERSE_DIRECTION = "reverse_direction"
    private const val KEY_SHOW_ROW_NUMBERS = "show_row_numbers"
    private const val KEY_SHOW_GRID_LINES = "show_grid_lines"

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
        prefs(context).getBoolean(KEY_REVERSE_DIRECTION, true)

    fun setReverseDirection(context: Context, reversed: Boolean) {
        prefs(context).edit().putBoolean(KEY_REVERSE_DIRECTION, reversed).apply()
    }

    fun isShowRowNumbers(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_ROW_NUMBERS, true)

    fun setShowRowNumbers(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_ROW_NUMBERS, show).apply()
    }

    fun isShowGridLines(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_GRID_LINES, true)

    fun setShowGridLines(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_GRID_LINES, show).apply()
    }
}
