package com.karyarina.labs.data

import android.app.Application

class StitchGridApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.applySavedTheme(this)
    }
}
