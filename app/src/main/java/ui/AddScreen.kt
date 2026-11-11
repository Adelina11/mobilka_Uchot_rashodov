package com.example.mobilka_uchot_rashodov.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobilka_uchot_rashodov.R
import com.example.mobilka_uchot_rashodov.data.CATEGORIES
import com.example.mobilka_uchot_rashodov.ui.theme.categoryColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddScreen(vm: ExpenseViewModel, isDark: Boolean, onDone: () -> Unit, onBack: () -> Unit) {
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var category by remember { mutableStateOf(CATEGORIES.first()) }
    var amount by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    // Раздельные ошибки: для поля Даты и для поля Суммы
    var dateError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    val showDecorBg by vm.showDecorBg.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая запись", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            // Фоновый рисунок (как на главном экране)
            if (showDecorBg) {
                Image(
                    painter = painterResource(id = if (isDark) R.drawable.bg_dark else R.drawable.bg_light),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = if (isDark) 0.35f else 0.55f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Поле Даты (ошибка даты выводится именно под этим полем!)
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it; dateError = null },
                    label = { Text("Дата (ГГГГ-ММ-ДД)") },
                    isError = dateError != null,
                    supportingText = dateError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) }
                    },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Выбрать в календаре")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        errorBorderColor = MaterialTheme.colorScheme.primary,
                        errorLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // 2. Красивый выбор категории (цветные плашки вместо выпадающего списка)
                Text(
                    "Выберите категорию:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORIES.forEach { c ->
                        val isSelected = category == c
                        val catColor = categoryColor(c, isDark)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) catColor else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { category = c }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(10.dp).clip(RoundedCornerShape(5.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else catColor)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    c.replaceFirstChar { it.uppercase() },
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 3. Поле Суммы (ошибка суммы выводится строго под этим полем!)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = null },
                    label = { Text("Сумма, ₽") },
                    isError = amountError != null,
                    supportingText = amountError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        errorBorderColor = MaterialTheme.colorScheme.primary,
                        errorLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // 4. Поле Комментария
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий (необязательно)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Кнопка Сохранить (ничем не перекрывается!)
                Button(
                    onClick = {
                        dateError = null
                        amountError = null

                        val parsedDate = try {
                            LocalDate.parse(dateText.trim())
                        } catch (e: DateTimeParseException) {
                            null
                        }

                        var hasError = false
                        if (parsedDate == null) {
                            dateError = "Некорректная дата (введите ГГГГ-ММ-ДД)"
                            hasError = true
                        }
                        if (ExpenseViewModel.parseAmountToKopecks(amount) == null) {
                            amountError = "Введите число строго больше 0 (до 2 знаков после запятой)"
                            hasError = true
                        }

                        if (!hasError) {
                            vm.add(parsedDate, category, amount, comment)
                                .onSuccess { onDone() }
                                .onFailure { e -> amountError = e.message }
                        }
                    },
                    Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Сохранить", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }
        }
    }

    if (showDatePicker) {
        val currentParsed = try { LocalDate.parse(dateText.trim()) } catch (e: Exception) { LocalDate.now() }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = currentParsed.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        dateText = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        dateError = null
                        showDatePicker = false
                    } else {
                        dateError = "Некорректная дата в календаре"
                        showDatePicker = false
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) { DatePicker(state = state) }
    }
}