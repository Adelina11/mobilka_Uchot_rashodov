package com.example.mobilka_uchot_rashodov.data

import androidx.room.Entity
import androidx.room.PrimaryKey

val CATEGORIES = listOf("продукты", "транспорт", "жильё", "развлечения", "прочее")

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,        // полночь выбранной даты
    val category: String,
    val amountKopecks: Long,     // сумма в копейках (без потери точности)
    val comment: String? = null
)