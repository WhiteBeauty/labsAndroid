package com.karyarina.labs.ui.knitting

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.karyarina.labs.R
import com.karyarina.labs.data.AppDatabase
import com.karyarina.labs.data.AppPreferences
import com.karyarina.labs.data.Pattern
import com.karyarina.labs.util.ImageToGridConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class KnittingActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var textViewRowInfo: TextView
    private lateinit var textViewRowsDone: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var gridView: KnittingGridView
    private lateinit var colorLegendContainer: LinearLayout

    private var pattern: Pattern? = null
    private var checkedRows = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knitting)

        toolbar = findViewById(R.id.toolbar)
        textViewRowInfo = findViewById(R.id.textViewRowInfo)
        textViewRowsDone = findViewById(R.id.textViewRowsDone)
        progressBar = findViewById(R.id.progressBar)
        gridView = findViewById(R.id.knittingGridView)
        colorLegendContainer = findViewById(R.id.colorLegendContainer)

        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.menu_knitting)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_more) {
                showPatternMenu()
                true
            } else {
                false
            }
        }
        gridView.onRowToggle = { row -> toggleRow(row) }
        gridView.setReversed(AppPreferences.isReverseDirection(this))
        gridView.setShowRowNumbers(AppPreferences.isShowRowNumbers(this))
        gridView.setShowGridLines(AppPreferences.isShowGridLines(this))

        val patternId = intent.getLongExtra(EXTRA_PATTERN_ID, -1)
        lifecycleScope.launch {
            val loaded = AppDatabase.getInstance(this@KnittingActivity).patternDao().getById(patternId)
                ?: return@launch
            pattern = loaded
            checkedRows = loaded.checkedRows.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toMutableSet()
            toolbar.title = loaded.name

            val bitmap = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(loaded.gridPath) }
            gridView.setData(bitmap, loaded.height, checkedRows)
            updateHeader(loaded.height)
            if (bitmap != null) {
                showColorLegend(bitmap)
            }
        }
    }

    private fun toggleRow(row: Int) {
        if (!checkedRows.remove(row)) {
            checkedRows.add(row)
        }
        gridView.setCheckedRows(checkedRows)
        val totalRows = pattern?.height ?: return
        updateHeader(totalRows)
        savePattern()
    }

    private fun updateHeader(totalRows: Int) {
        val currentRow = (1..totalRows).firstOrNull { it !in checkedRows } ?: totalRows
        textViewRowInfo.text = getString(R.string.knitting_row_info_format, currentRow, totalRows)
        textViewRowsDone.text = getString(R.string.knitting_rows_done_format, checkedRows.size)
        progressBar.max = totalRows
        progressBar.progress = checkedRows.size
        gridView.setCurrentRow(currentRow)
    }

    private fun showColorLegend(bitmap: Bitmap) {
        colorLegendContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        for ((color, count) in ImageToGridConverter.countColors(bitmap)) {
            val item = inflater.inflate(R.layout.item_color_count, colorLegendContainer, false)
            val swatch = item.findViewById<View>(R.id.viewColorSwatch)
            (swatch.background.mutate() as GradientDrawable).setColor(color)
            item.findViewById<TextView>(R.id.textViewColorCount).text = count.toString()
            colorLegendContainer.addView(item)
        }
    }

    private fun showPatternMenu() {
        val current = pattern ?: return
        val actions = arrayOf(
            getString(R.string.action_rename),
            getString(R.string.action_reset_progress),
            getString(R.string.action_delete)
        )
        AlertDialog.Builder(this)
            .setTitle(current.name)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> showRenameDialog(current)
                    1 -> confirmResetProgress(current)
                    2 -> confirmDelete(current)
                }
            }
            .show()
    }

    private fun showRenameDialog(current: Pattern) {
        val input = EditText(this).apply {
            setText(current.name)
            setSelection(current.name.length)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.action_rename)
            .setView(input)
            .setPositiveButton(R.string.button_confirm) { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    toolbar.title = newName
                    updatePattern(current.copy(name = newName, updatedAt = System.currentTimeMillis()))
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private fun confirmResetProgress(current: Pattern) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_reset_message, current.name))
            .setPositiveButton(R.string.button_confirm) { _, _ ->
                checkedRows = mutableSetOf()
                gridView.setCheckedRows(checkedRows)
                updateHeader(current.height)
                updatePattern(current.copy(checkedRows = "", updatedAt = System.currentTimeMillis()))
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private fun confirmDelete(current: Pattern) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_delete_message, current.name))
            .setPositiveButton(R.string.button_confirm) { _, _ -> deletePattern(current) }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updatePattern(updated: Pattern) {
        pattern = updated
        GlobalScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance(applicationContext).patternDao().update(updated)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun deletePattern(current: Pattern) {
        GlobalScope.launch(Dispatchers.IO) {
            File(current.gridPath).delete()
            AppDatabase.getInstance(applicationContext).patternDao().delete(current)
        }
        finish()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun savePattern() {
        val current = pattern ?: return
        val updated = current.copy(
            checkedRows = checkedRows.sorted().joinToString(","),
            updatedAt = System.currentTimeMillis()
        )
        pattern = updated
        GlobalScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance(applicationContext).patternDao().update(updated)
        }
    }

    companion object {
        const val EXTRA_PATTERN_ID = "pattern_id"
    }
}
