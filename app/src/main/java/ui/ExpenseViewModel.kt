package com.example.mobilka_uchot_rashodov.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilka_uchot_rashodov.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ExpenseViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()

    private val _from = MutableStateFlow<LocalDate?>(null)
    private val _to = MutableStateFlow<LocalDate?>(null)
    val from: StateFlow<LocalDate?> = _from
    val to: StateFlow<LocalDate?> = _to

    val all: StateFlow<List<Expense>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFrom(d: LocalDate?) { _from.value = d }
    fun setTo(d: LocalDate?) { _to.value = d }
    fun resetFilter() { _from.value = null; _to.value = null }

    fun filtered(list: List<Expense>): List<Expense> {
        val f = _from.value; val t = _to.value
        return list.filter {
            val d = Instant.ofEpochMilli(it.dateMillis).atZone(ZoneOffset.UTC).toLocalDate()
            (f == null || !d.isBefore(f)) && (t == null || !d.isAfter(t))
        }
    }

    fun totalKopecks(list: List<Expense>) = list.sumOf { it.amountKopecks }

    fun byCategory(list: List<Expense>): Map<String, Long> =
        CATEGORIES.associateWith { c -> list.filter { it.category == c }.sumOf { it.amountKopecks } }

    // Валидация (п.2, п.3)
    fun add(date: LocalDate?, category: String, amountText: String, comment: String?): Result<Unit> {
        if (date == null) return Result.failure(IllegalArgumentException("Выберите дату"))
        if (category !in CATEGORIES) return Result.failure(IllegalArgumentException("Неизвестная категория"))

        val kop = parseAmountToKopecks(amountText)
            ?: return Result.failure(IllegalArgumentException("Сумма должна быть числом > 0"))

        val millis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        viewModelScope.launch {
            dao.insert(Expense(dateMillis = millis, category = category, amountKopecks = kop, comment = comment?.takeIf { it.isNotBlank() }))
        }
        return Result.success(Unit)
    }

    fun delete(e: Expense) = viewModelScope.launch { dao.delete(e) }

    companion object {
        fun parseAmountToKopecks(input: String): Long? {
            val s = input.trim().replace(',', '.')
            if (s.isEmpty() || s.count { it == '.' } > 1) return null
            val d = s.toDoubleOrNull() ?: return null
            if (!d.isFinite() || d <= 0.0) return null
            return Math.round(d * 100)
        }
    }
}