package com.example.mobilka_uchot_rashodov.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobilka_uchot_rashodov.data.CATEGORIES
import com.example.mobilka_uchot_rashodov.data.Expense
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(vm: ExpenseViewModel, onAdd: () -> Unit) {
    val all by vm.all.collectAsStateWithLifecycle()
    val from by vm.from.collectAsStateWithLifecycle()
    val to by vm.to.collectAsStateWithLifecycle()

    val filtered = vm.filtered(all)
    val total = vm.totalKopecks(filtered)
    val byCat = vm.byCategory(filtered)

    var pickFrom by remember { mutableStateOf(false) }
    var pickTo by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Expense?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои расходы", fontWeight = FontWeight.Bold) },
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
        Column(Modifier.padding(pad).fillMaxSize().padding(horizontal = 8.dp)) {

            // Фильтры
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickFrom = true }, Modifier.weight(1f)) {
                    Text("С: ${from ?: "—"}", fontSize = 12.sp)
                }
                OutlinedButton(onClick = { pickTo = true }, Modifier.weight(1f)) {
                    Text("По: ${to ?: "—"}", fontSize = 12.sp)
                }
                TextButton(onClick = { vm.resetFilter() }) { Text("Сброс") }
            }

            // Карточка с итогом
            Card(
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Итог за период", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        formatRub(total),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Разбивка по категориям
                    byCat.forEach { (c, v) ->
                        if (v > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(c, fontSize = 12.sp)
                                Text(formatRub(v), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Список записей
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { e ->
                    Card(
                        Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Левая часть с иконкой категории (цветной кружок)
                            Box(
                                Modifier.size(40.dp).clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(e.category.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Центральная часть (текст)
                            Column(Modifier.weight(1f)) {
                                Text(e.category.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${Instant.ofEpochMilli(e.dateMillis).atZone(ZoneOffset.UTC).toLocalDate()}" +
                                            (e.comment?.let { " • $it" } ?: ""),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Правая часть (сумма и удаление)
                            Text(formatRub(e.amountKopecks), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { toDelete = e }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог удаления
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

    if (pickFrom) DateBoundaryPicker(from) { vm.setFrom(it); pickFrom = false; }
    if (pickTo) DateBoundaryPicker(to) { vm.setTo(it); pickTo = false; }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateBoundaryPicker(initial: LocalDate?, onPicked: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let {
                    onPicked(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                }
            }) { Text("OK") }
        },
        dismissButton = {}
    ) { DatePicker(state = state) }
}

fun formatRub(kopecks: Long): String =
    "%.2f ₽".format(Locale("ru", "RU"), kopecks / 100.0)