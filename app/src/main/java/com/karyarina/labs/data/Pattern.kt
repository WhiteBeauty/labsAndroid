package com.karyarina.labs.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patterns")
data class Pattern(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val width: Int,
    val height: Int,
    val gridPath: String,
    val colorsCount: Int,
    val checkedRows: String = "",
    val createdAt: Long,
    val updatedAt: Long
)
