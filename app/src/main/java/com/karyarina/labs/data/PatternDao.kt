package com.karyarina.labs.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {

    @Query("SELECT * FROM patterns ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Pattern>>

    @Query("SELECT * FROM patterns WHERE id = :patternId")
    suspend fun getById(patternId: Long): Pattern?

    @Insert
    suspend fun insert(pattern: Pattern): Long

    @Update
    suspend fun update(pattern: Pattern)

    @Delete
    suspend fun delete(pattern: Pattern)
}
