package com.example.mobilka_uchot_rashodov.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<Expense>>

    @Insert
    suspend fun insert(e: Expense): Long

    @Delete
    suspend fun delete(e: Expense)
}