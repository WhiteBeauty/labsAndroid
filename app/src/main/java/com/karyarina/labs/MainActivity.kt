package com.karyarina.labs

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val editTextSize = findViewById<EditText>(R.id.editTextSize)
        val textViewResult = findViewById<TextView>(R.id.textViewResult)
        val buttonGenerate = findViewById<Button>(R.id.buttonGenerate)
        editTextSize.setText("8")

        buttonGenerate.setOnClickListener {
            var size = editTextSize.text.toString().toIntOrNull()
            if (size == null || size <= 0) {
                size = 8
            }
            if (size > 30) {
                size = 30
            }
            val sourceList = ListNormalizer.generateRandomList(size, 1, 100)
            val normalizedList = ListNormalizer.normalizeByMax(sourceList)
            textViewResult.text = ListNormalizer.buildResultText(sourceList, normalizedList)
        }
    }
}
