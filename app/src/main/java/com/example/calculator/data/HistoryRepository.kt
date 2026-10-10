package com.example.calculator.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: CalculationDao) {

    val allHistory: Flow<List<CalculationHistory>> = dao.getAllHistory()

    fun searchHistory(query: String): Flow<List<CalculationHistory>> {
        return dao.searchHistory(query)
    }

    suspend fun insertCalculation(expression: String, result: String): Long {
        if (expression.isBlank() || result.isBlank()) return -1
        return dao.insert(
            CalculationHistory(
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun toggleBookmark(id: Long, currentBookmark: Boolean) {
        dao.updateBookmark(id, !currentBookmark)
    }
}
