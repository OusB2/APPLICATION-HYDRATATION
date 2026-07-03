package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterRecordDao {
    @Query("SELECT * FROM water_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<WaterRecord>>

    @Query("SELECT * FROM water_records WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayRecords(startOfDay: Long): Flow<List<WaterRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WaterRecord)

    @Delete
    suspend fun deleteRecord(record: WaterRecord)

    @Query("DELETE FROM water_records")
    suspend fun deleteAllRecords()
}
