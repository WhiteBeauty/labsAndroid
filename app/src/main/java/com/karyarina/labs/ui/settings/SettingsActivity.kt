package com.karyarina.labs.ui.settings

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.materialswitch.MaterialSwitch
import com.karyarina.labs.R
import com.karyarina.labs.data.AppPreferences

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rowTheme = findViewById<android.view.View>(R.id.rowTheme)
        val textViewThemeValue = findViewById<TextView>(R.id.textViewThemeValue)
        val rowDirection = findViewById<android.view.View>(R.id.rowDirection)
        val textViewDirectionValue = findViewById<TextView>(R.id.textViewDirectionValue)
        val switchShowRowNumbers = findViewById<MaterialSwitch>(R.id.switchShowRowNumbers)
        val switchShowGridLines = findViewById<MaterialSwitch>(R.id.switchShowGridLines)
        val textViewAbout = findViewById<TextView>(R.id.textViewAbout)

        updateThemeValue(textViewThemeValue)
        updateDirectionValue(textViewDirectionValue)
        rowTheme.setOnClickListener { showThemeDialog(textViewThemeValue) }
        rowDirection.setOnClickListener { showDirectionDialog(textViewDirectionValue) }

        switchShowRowNumbers.isChecked = AppPreferences.isShowRowNumbers(this)
        switchShowRowNumbers.setOnCheckedChangeListener { _, checked ->
            AppPreferences.setShowRowNumbers(this, checked)
        }

        switchShowGridLines.isChecked = AppPreferences.isShowGridLines(this)
        switchShowGridLines.setOnCheckedChangeListener { _, checked ->
            AppPreferences.setShowGridLines(this, checked)
        }

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        textViewAbout.text = "${getString(R.string.app_name)}\n" +
            getString(R.string.about_version_format, versionName)
    }

    private fun themeLabel(mode: Int): String = when (mode) {
        AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.theme_light)
        AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.theme_dark)
        else -> getString(R.string.theme_system)
    }

    private fun updateThemeValue(textView: TextView) {
        textView.text = themeLabel(AppPreferences.getThemeMode(this))
    }

    private fun showThemeDialog(textView: TextView) {
        val options = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )
        val modes = intArrayOf(
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
        val current = modes.indexOf(AppPreferences.getThemeMode(this)).coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle(R.string.setting_theme)
            .setSingleChoiceItems(options, current) { dialog, which ->
                AppPreferences.setThemeMode(this, modes[which])
                updateThemeValue(textView)
                dialog.dismiss()
            }
            .show()
    }

    private fun updateDirectionValue(textView: TextView) {
        textView.text = if (AppPreferences.isReverseDirection(this)) {
            getString(R.string.direction_bottom_to_top)
        } else {
            getString(R.string.direction_top_to_bottom)
        }
    }

    private fun showDirectionDialog(textView: TextView) {
        val options = arrayOf(
            getString(R.string.direction_top_to_bottom),
            getString(R.string.direction_bottom_to_top)
        )
        val current = if (AppPreferences.isReverseDirection(this)) 1 else 0
        AlertDialog.Builder(this)
            .setTitle(R.string.setting_reading_direction)
            .setSingleChoiceItems(options, current) { dialog, which ->
                AppPreferences.setReverseDirection(this, which == 1)
                updateDirectionValue(textView)
                dialog.dismiss()
            }
            .show()
    }
}
