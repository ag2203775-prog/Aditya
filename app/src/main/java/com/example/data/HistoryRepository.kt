package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<CalculationHistory>> = historyDao.getAllHistory()

    suspend fun insert(history: CalculationHistory): Long {
        return historyDao.insert(history)
    }

    suspend fun deleteById(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearAll() {
        historyDao.clearAll()
    }
}
