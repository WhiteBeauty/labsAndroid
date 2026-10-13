package com.karyarina.labs.ui

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karyarina.labs.R
import com.karyarina.labs.data.Pattern

class PatternAdapter(
    private val onPatternClick: (Pattern) -> Unit,
    private val onPatternLongClick: (Pattern) -> Unit
) : ListAdapter<Pattern, PatternAdapter.PatternViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatternViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pattern, parent, false)
        return PatternViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatternViewHolder, position: Int) {
        holder.bind(getItem(position), onPatternClick, onPatternLongClick)
    }

    class PatternViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewThumbnail: ImageView = itemView.findViewById(R.id.imageViewThumbnail)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewProgress: TextView = itemView.findViewById(R.id.textViewProgress)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(
            pattern: Pattern,
            onPatternClick: (Pattern) -> Unit,
            onPatternLongClick: (Pattern) -> Unit
        ) {
            textViewName.text = pattern.name

            val checkedCount = pattern.checkedRows.split(",").count { it.isNotBlank() }
            textViewProgress.text = itemView.context.getString(
                R.string.pattern_progress_format,
                checkedCount,
                pattern.height
            )
            progressBar.max = pattern.height
            progressBar.progress = checkedCount

            val thumbnail = BitmapFactory.decodeFile(pattern.gridPath)
            imageViewThumbnail.setImageBitmap(thumbnail)

            itemView.setOnClickListener { onPatternClick(pattern) }
            itemView.setOnLongClickListener {
                onPatternLongClick(pattern)
                true
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pattern>() {
            override fun areItemsTheSame(oldItem: Pattern, newItem: Pattern) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Pattern, newItem: Pattern) =
                oldItem == newItem
        }
    }
}
