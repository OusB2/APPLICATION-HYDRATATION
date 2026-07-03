package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.WaterRecord
import com.example.data.WaterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class WaterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WaterRepository
    val goalMl: Int = 2000 // 2L

    val todayRecords: StateFlow<List<WaterRecord>>
    val totalWaterMl: StateFlow<Int>
    val progressPercent: StateFlow<Float>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WaterRepository(database.waterRecordDao())
        
        todayRecords = repository.getTodayRecords(getStartOfToday())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            
        totalWaterMl = todayRecords.map { list ->
            list.sumOf { it.amountMl }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        
        progressPercent = totalWaterMl.map { total ->
            (total.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )
    }

    fun addWater(amount: Int = 250) {
        viewModelScope.launch {
            repository.insert(WaterRecord(amountMl = amount))
        }
    }

    fun resetToday() {
        viewModelScope.launch {
            repository.reset()
        }
    }

    fun deleteRecord(record: WaterRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }

    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
