package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: CalculationHistory): Long

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}
