package com.example.mobilka_uchot_rashodov.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilka_uchot_rashodov.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ExpenseViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()
    private val prefs = app.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _from = MutableStateFlow<LocalDate?>(null)
    private val _to = MutableStateFlow<LocalDate?>(null)
    val from: StateFlow<LocalDate?> = _from
    val to: StateFlow<LocalDate?> = _to

    // Настройки (сохраняются между запусками)
    // themeMode: 0 - Системная, 1 - Светлая, 2 - Тёмная
    private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 1))
    val themeMode: StateFlow<Int> = _themeMode

    private val _hideZeroCategories = MutableStateFlow(prefs.getBoolean("hide_zero_cat", false))
    val hideZeroCategories: StateFlow<Boolean> = _hideZeroCategories

    private val _showDecorBg = MutableStateFlow(prefs.getBoolean("show_decor_bg", true))
    val showDecorBg: StateFlow<Boolean> = _showDecorBg

    val all: StateFlow<List<Expense>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFrom(d: LocalDate?) { _from.value = d }
    fun setTo(d: LocalDate?) { _to.value = d }
    fun resetFilter() { _from.value = null; _to.value = null }

    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
        prefs.edit().putInt("theme_mode", mode).apply()
    }

    fun setHideZeroCategories(hide: Boolean) {
        _hideZeroCategories.value = hide
        prefs.edit().putBoolean("hide_zero_cat", hide).apply()
    }

    fun setShowDecorBg(show: Boolean) {
        _showDecorBg.value = show
        prefs.edit().putBoolean("show_decor_bg", show).apply()
    }

    fun clearAllExpenses() {
        viewModelScope.launch {
            all.value.forEach { dao.delete(it) }
        }
    }

    fun filtered(list: List<Expense>, f: LocalDate?, t: LocalDate?): List<Expense> {
        return list.filter {
            val d = Instant.ofEpochMilli(it.dateMillis).atZone(ZoneOffset.UTC).toLocalDate()
            (f == null || !d.isBefore(f)) && (t == null || !d.isAfter(t))
        }
    }

    fun totalKopecks(list: List<Expense>) = list.sumOf { it.amountKopecks }

    fun byCategory(list: List<Expense>): Map<String, Long> =
        CATEGORIES.associateWith { c -> list.filter { it.category == c }.sumOf { it.amountKopecks } }

    // Строгая валидация по п. 2 и п. 3 методички
    fun add(date: LocalDate?, category: String, amountText: String, comment: String?): Result<Unit> {
        if (date == null) return Result.failure(IllegalArgumentException("Некорректная дата (используйте формат ГГГГ-ММ-ДД)"))
        if (category !in CATEGORIES) return Result.failure(IllegalArgumentException("Неизвестная категория"))

        val kop = parseAmountToKopecks(amountText)
            ?: return Result.failure(IllegalArgumentException("Ошибка: введите число строго больше 0 (до 2 знаков после запятой)"))

        val millis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        viewModelScope.launch {
            dao.insert(
                Expense(
                    dateMillis = millis,
                    category = category,
                    amountKopecks = kop,
                    comment = comment?.trim()?.takeIf { it.isNotEmpty() }
                )
            )
        }
        return Result.success(Unit)
    }

    fun delete(e: Expense) = viewModelScope.launch { dao.delete(e) }

    companion object {
        // Разрешены только цифры и не более одной точки/запятой (1-2 знака копеек)
        private val AMOUNT_REGEX = Regex("""^\d+([.,]\d{1,2})?$""")

        fun parseAmountToKopecks(input: String): Long? {
            val s = input.trim()
            if (!AMOUNT_REGEX.matches(s)) return null
            return try {
                val bd = BigDecimal(s.replace(',', '.'))
                    .setScale(2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                val kopecks = bd.longValueExact()
                if (kopecks <= 0L) null else kopecks
            } catch (e: Exception) {
                null
            }
        }
    }
}