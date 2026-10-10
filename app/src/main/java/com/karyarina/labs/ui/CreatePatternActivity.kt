package com.karyarina.labs.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.karyarina.labs.R
import com.karyarina.labs.data.AppDatabase
import com.karyarina.labs.data.Pattern
import com.karyarina.labs.util.ImageToGridConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CreatePatternActivity : AppCompatActivity() {

    private var originalBitmap: Bitmap? = null
    private var gridBitmap: Bitmap? = null
    private var selectedColors = DEFAULT_COLORS

    private lateinit var editTextName: EditText
    private lateinit var imageViewPreview: ImageView
    private lateinit var textViewWidthValue: TextView
    private lateinit var seekBarWidth: SeekBar
    private lateinit var buttonSave: Button

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            contentResolver.openInputStream(uri)?.use { stream ->
                originalBitmap = BitmapFactory.decodeStream(stream)
            }
            buttonSave.isEnabled = originalBitmap != null
            updatePreview()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pattern)

        editTextName = findViewById(R.id.editTextName)
        imageViewPreview = findViewById(R.id.imageViewPreview)
        textViewWidthValue = findViewById(R.id.textViewWidthValue)
        seekBarWidth = findViewById(R.id.seekBarWidth)
        buttonSave = findViewById(R.id.buttonSave)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val buttonPickImage = findViewById<Button>(R.id.buttonPickImage)
        val chipGroupColors = findViewById<ChipGroup>(R.id.chipGroupColors)

        toolbar.setNavigationOnClickListener { finish() }

        seekBarWidth.max = MAX_WIDTH - MIN_WIDTH
        seekBarWidth.progress = DEFAULT_WIDTH - MIN_WIDTH
        textViewWidthValue.text = DEFAULT_WIDTH.toString()

        buttonPickImage.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        seekBarWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textViewWidthValue.text = (progress + MIN_WIDTH).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                updatePreview()
            }
        })

        chipGroupColors.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            selectedColors = group.findViewById<Chip>(checkedId).text.toString().toInt()
            updatePreview()
        }

        buttonSave.setOnClickListener { savePattern() }
    }

    private fun currentWidth() = seekBarWidth.progress + MIN_WIDTH

    private fun updatePreview() {
        val source = originalBitmap ?: return
        val width = currentWidth()
        val colors = selectedColors
        lifecycleScope.launch {
            val converted = withContext(Dispatchers.Default) {
                ImageToGridConverter.convert(source, width, colors)
            }
            gridBitmap = converted
            imageViewPreview.setImageBitmap(converted)
            (imageViewPreview.drawable as? BitmapDrawable)?.isFilterBitmap = false
        }
    }

    private fun savePattern() {
        val name = editTextName.text.toString().trim()
        if (name.isEmpty()) {
            editTextName.error = getString(R.string.error_name_required)
            return
        }
        val grid = gridBitmap ?: return

        buttonSave.isEnabled = false
        lifecycleScope.launch {
            val gridPath = withContext(Dispatchers.IO) { saveGridToFile(grid) }
            val now = System.currentTimeMillis()
            val pattern = Pattern(
                name = name,
                width = grid.width,
                height = grid.height,
                gridPath = gridPath,
                colorsCount = selectedColors,
                checkedRows = "",
                createdAt = now,
                updatedAt = now
            )
            AppDatabase.getInstance(this@CreatePatternActivity).patternDao().insert(pattern)
            finish()
        }
    }

    private fun saveGridToFile(bitmap: Bitmap): String {
        val dir = File(filesDir, "patterns").apply { mkdirs() }
        val file = File(dir, "pattern_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    companion object {
        private const val MIN_WIDTH = 10
        private const val MAX_WIDTH = 150
        private const val DEFAULT_WIDTH = 50
        private const val DEFAULT_COLORS = 4
    }
}
