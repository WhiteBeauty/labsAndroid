package com.karyarina.labs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karyarina.labs.data.AppDatabase
import com.karyarina.labs.ui.CreatePatternActivity
import com.karyarina.labs.ui.KnittingActivity
import com.karyarina.labs.ui.PatternAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PatternAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPatterns)
        val emptyState = findViewById<TextView>(R.id.textViewEmptyState)
        val fabAddPattern = findViewById<FloatingActionButton>(R.id.fabAddPattern)

        adapter = PatternAdapter { pattern ->
            val intent = Intent(this, KnittingActivity::class.java)
                .putExtra(KnittingActivity.EXTRA_PATTERN_ID, pattern.id)
            startActivity(intent)
        }
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
}
