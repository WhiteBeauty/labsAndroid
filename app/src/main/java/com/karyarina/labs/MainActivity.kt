package com.karyarina.labs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karyarina.labs.data.AppDatabase
import com.karyarina.labs.data.Pattern
import com.karyarina.labs.ui.CreatePatternActivity
import com.karyarina.labs.ui.KnittingActivity
import com.karyarina.labs.ui.PatternAdapter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PatternAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPatterns)
        val emptyState = findViewById<TextView>(R.id.textViewEmptyState)
        val fabAddPattern = findViewById<FloatingActionButton>(R.id.fabAddPattern)

        adapter = PatternAdapter(
            onPatternClick = { pattern ->
                val intent = Intent(this, KnittingActivity::class.java)
                    .putExtra(KnittingActivity.EXTRA_PATTERN_ID, pattern.id)
                startActivity(intent)
            },
            onPatternLongClick = { pattern -> showPatternMenu(pattern) }
        )
        recyclerView.adapter = adapter

        fabAddPattern.setOnClickListener {
            startActivity(Intent(this, CreatePatternActivity::class.java))
        }

        val patternDao = AppDatabase.getInstance(this).patternDao()
        lifecycleScope.launch {
            patternDao.getAll().collect { patterns ->
                adapter.submitList(patterns)
                emptyState.visibility = if (patterns.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showPatternMenu(pattern: Pattern) {
        val actions = arrayOf(
            getString(R.string.action_rename),
            getString(R.string.action_reset_progress),
            getString(R.string.action_delete)
        )
        AlertDialog.Builder(this)
            .setTitle(pattern.name)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> showRenameDialog(pattern)
                    1 -> confirmResetProgress(pattern)
                    2 -> confirmDelete(pattern)
                }
            }
            .show()
    }

    private fun showRenameDialog(pattern: Pattern) {
        val input = EditText(this).apply {
            setText(pattern.name)
            setSelection(pattern.name.length)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.action_rename)
            .setView(input)
            .setPositiveButton(R.string.button_confirm) { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updatePattern(pattern.copy(name = newName, updatedAt = System.currentTimeMillis()))
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private fun confirmResetProgress(pattern: Pattern) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_reset_message, pattern.name))
            .setPositiveButton(R.string.button_confirm) { _, _ ->
                updatePattern(
                    pattern.copy(checkedRows = "", updatedAt = System.currentTimeMillis())
                )
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private fun confirmDelete(pattern: Pattern) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_delete_message, pattern.name))
            .setPositiveButton(R.string.button_confirm) { _, _ -> deletePattern(pattern) }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updatePattern(pattern: Pattern) {
        GlobalScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance(applicationContext).patternDao().update(pattern)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun deletePattern(pattern: Pattern) {
        GlobalScope.launch(Dispatchers.IO) {
            File(pattern.gridPath).delete()
            AppDatabase.getInstance(applicationContext).patternDao().delete(pattern)
        }
    }
}
