package com.example.mobilka_uchot_rashodov.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        topBar = { TopAppBar(title = { Text("Расходы") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickFrom = true }, Modifier.weight(1f)) {
                    Text("С: ${from ?: "—"}")
                }
                OutlinedButton(onClick = { pickTo = true }, Modifier.weight(1f)) {
                    Text("По: ${to ?: "—"}")
                }
                TextButton(onClick = { vm.resetFilter() }) { Text("Сброс") }
            }

            Card(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Итог за период: ${formatRub(total)}", style = MaterialTheme.typography.titleMedium)
                    byCat.forEach { (c, v) -> Text("$c: ${formatRub(v)}") }
                }
            }

            LazyColumn(Modifier.weight(1f)) {
                items(filtered, key = { it.id }) { e ->
                    ListItem(
                        headlineContent = { Text("${formatRub(e.amountKopecks)} • ${e.category}") },
                        supportingContent = {
                            Text("${Instant.ofEpochMilli(e.dateMillis).atZone(ZoneOffset.UTC).toLocalDate()}" +
                                    (e.comment?.let { " — $it" } ?: ""))
                        },
                        trailingContent = {
                            IconButton(onClick = { toDelete = e }) { Icon(Icons.Default.Delete, "Удалить") }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    toDelete?.let { e ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Удалить запись?") },
            text = { Text("${formatRub(e.amountKopecks)} • ${e.category}") },
            confirmButton = { TextButton(onClick = { vm.delete(e); toDelete = null }) { Text("Удалить") } },
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