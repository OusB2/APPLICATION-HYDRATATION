package com.example.data

import kotlinx.coroutines.flow.Flow

class WaterRepository(private val waterRecordDao: WaterRecordDao) {
    fun getTodayRecords(startOfDay: Long): Flow<List<WaterRecord>> {
        return waterRecordDao.getTodayRecords(startOfDay)
    }

    suspend fun insert(record: WaterRecord) {
        waterRecordDao.insertRecord(record)
    }

    suspend fun delete(record: WaterRecord) {
        waterRecordDao.deleteRecord(record)
    }

    suspend fun reset() {
        waterRecordDao.deleteAllRecords()
    }
}
