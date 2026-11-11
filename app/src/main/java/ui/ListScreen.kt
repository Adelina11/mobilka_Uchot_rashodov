package com.example.mobilka_uchot_rashodov.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobilka_uchot_rashodov.R
import com.example.mobilka_uchot_rashodov.data.Expense
import com.example.mobilka_uchot_rashodov.ui.theme.categoryColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RU_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(vm: ExpenseViewModel, isDark: Boolean, onAdd: () -> Unit) {
    val all by vm.all.collectAsStateWithLifecycle()
    val from by vm.from.collectAsStateWithLifecycle()
    val to by vm.to.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val hideZeroCat by vm.hideZeroCategories.collectAsStateWithLifecycle()
    val showDecorBg by vm.showDecorBg.collectAsStateWithLifecycle()

    val filtered = vm.filtered(all, from, to)
    val total = vm.totalKopecks(filtered)
    val byCat = vm.byCategory(filtered)

    var pickFrom by remember { mutableStateOf(false) }
    var pickTo by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Expense?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Учёт расходов", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            if (showDecorBg) {
                Image(
                    painter = painterResource(id = if (isDark) R.drawable.bg_dark else R.drawable.bg_light),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = if (isDark) 0.35f else 0.55f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {

                // Красивая панель фильтра по датам (с иконками календаря без дефисов)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { pickFrom = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        )
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(from?.let { "С ${it.format(RU_DATE)}" } ?: "Начало", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { pickTo = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        )
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(to?.let { "По ${it.format(RU_DATE)}" } ?: "Конец", fontSize = 12.sp)
                    }
                    if (from != null || to != null) {
                        TextButton(
                            onClick = { vm.resetFilter() },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                            )
                        ) {
                            Text("Сброс", fontSize = 12.sp)
                        }
                    }
                }

                if (from != null && to != null && from!!.isAfter(to)) {
                    Text(
                        "Внимание: начальная дата позже конечной",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Карточка с итогом и разбивкой по категориям
                Card(
                    Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Итог за выбранный период",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            formatRub(total),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))

                        byCat.forEach { (c, v) ->
                            if (!hideZeroCat || v > 0L) {
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        c.replaceFirstChar { it.uppercase() },
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        formatRub(v),
                                        fontSize = 13.sp,
                                        fontWeight = if (v > 0) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Список записей
                if (filtered.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Записей за выбранный период нет",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filtered, key = { it.id }) { e ->
                            Card(
                                Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                                )
                            ) {
                                Row(
                                    Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier.size(42.dp).clip(RoundedCornerShape(21.dp))
                                            .background(categoryColor(e.category, isDark)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            e.category.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            e.category.replaceFirstChar { it.uppercase() },
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val dateFormatted = Instant.ofEpochMilli(e.dateMillis)
                                            .atZone(ZoneOffset.UTC).toLocalDate().format(RU_DATE)
                                        Text(
                                            dateFormatted + (e.comment?.let { " • $it" } ?: ""),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        formatRub(e.amountKopecks),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(onClick = { toDelete = e }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Удалить",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { e ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Удалить запись?") },
            text = { Text("${formatRub(e.amountKopecks)} • ${e.category}") },
            confirmButton = {
                TextButton(onClick = { vm.delete(e); toDelete = null }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Отмена") } }
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Настройки приложения", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Тема оформления:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = themeMode == 1,
                            onClick = { vm.setThemeMode(1) },
                            label = { Text("Светлая", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = themeMode == 2,
                            onClick = { vm.setThemeMode(2) },
                            label = { Text("Тёмная", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = themeMode == 0,
                            onClick = { vm.setThemeMode(0) },
                            label = { Text("Авто", fontSize = 12.sp) }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Скрывать нулевые категории", fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Switch(checked = hideZeroCat, onCheckedChange = { vm.setHideZeroCategories(it) })
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Фоновый рисунок", fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Switch(checked = showDecorBg, onCheckedChange = { vm.setShowDecorBg(it) })
                    }

                    if (all.isNotEmpty()) {
                        HorizontalDivider()
                        OutlinedButton(
                            onClick = { vm.clearAllExpenses(); showSettings = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Очистить все записи (${all.size})")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) { Text("Готово") }
            }
        )
    }

    if (pickFrom) DateBoundaryPicker(from, onDismiss = { pickFrom = false }) { vm.setFrom(it); pickFrom = false }
    if (pickTo) DateBoundaryPicker(to, onDismiss = { pickTo = false }) { vm.setTo(it); pickTo = false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateBoundaryPicker(initial: LocalDate?, onDismiss: () -> Unit, onPicked: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) {
                    onPicked(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                } else {
                    onDismiss()
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    ) { DatePicker(state = state) }
}

fun formatRub(kopecks: Long): String =
    "%.2f ₽".format(Locale("ru", "RU"), kopecks / 100.0)