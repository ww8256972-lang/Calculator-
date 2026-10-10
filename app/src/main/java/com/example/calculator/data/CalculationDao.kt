package com.example.calculator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {

    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationHistory>>

    @Query("SELECT * FROM calculation_history WHERE expression LIKE '%' || :query || '%' OR result LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<CalculationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CalculationHistory): Long

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()

    @Query("UPDATE calculation_history SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: Long, isBookmarked: Boolean)
}
