package com.karyarina.labs

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karyarina.labs.data.AppDatabase
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
            Toast.makeText(this, R.string.not_implemented_yet, Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        fabAddPattern.setOnClickListener {
            Toast.makeText(this, R.string.not_implemented_yet, Toast.LENGTH_SHORT).show()
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
