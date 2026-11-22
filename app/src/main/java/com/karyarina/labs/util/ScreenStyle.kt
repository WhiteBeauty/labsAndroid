package com.karyarina.labs.util

import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun AppCompatActivity.applyScreenInsets() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val content = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
    ViewCompat.setOnApplyWindowInsetsListener(content) { view: View, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, keyboard.bottom))
        insets
    }
    val lightTheme = resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_YES
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.isAppearanceLightStatusBars = lightTheme
    controller.isAppearanceLightNavigationBars = lightTheme
    ViewCompat.requestApplyInsets(content)
}
