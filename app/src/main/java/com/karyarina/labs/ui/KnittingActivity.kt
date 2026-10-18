package com.karyarina.labs.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.karyarina.labs.R
import com.karyarina.labs.data.AppDatabase
import com.karyarina.labs.data.AppPreferences
import com.karyarina.labs.data.Pattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KnittingActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var textViewRowInfo: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var gridView: KnittingGridView

    private var pattern: Pattern? = null
    private var checkedRows = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knitting)

        toolbar = findViewById(R.id.toolbar)
        textViewRowInfo = findViewById(R.id.textViewRowInfo)
        progressBar = findViewById(R.id.progressBar)
        gridView = findViewById(R.id.knittingGridView)

        toolbar.setNavigationOnClickListener { finish() }
        gridView.onRowToggle = { row -> toggleRow(row) }
        gridView.setReversed(AppPreferences.isReverseDirection(this))

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
        progressBar.max = totalRows
        progressBar.progress = checkedRows.size
        gridView.setCurrentRow(currentRow)
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
